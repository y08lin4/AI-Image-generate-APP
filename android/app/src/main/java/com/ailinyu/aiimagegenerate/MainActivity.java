package com.ailinyu.aiimagegenerate;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.getcapacitor.BridgeActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Locale;

public class MainActivity extends BridgeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBridge().getWebView().addJavascriptInterface(new AIImageAppBridge(this), "AIImageApp");
        injectWebFallbacks();
    }

    private void injectWebFallbacks() {
        final int[] count = {0};
        Runnable injector = new Runnable() {
            @Override
            public void run() {
                if (getBridge() != null && getBridge().getWebView() != null) {
                    getBridge().getWebView().evaluateJavascript(WEB_FALLBACK_SCRIPT, null);
                    count[0] += 1;
                    if (count[0] < 40) {
                        getBridge().getWebView().postDelayed(this, 1500);
                    }
                }
            }
        };
        getBridge().getWebView().postDelayed(injector, 800);
    }

    private static final String WEB_FALLBACK_SCRIPT =
        "(function(){"
            + "if(window.__AI_IMAGE_APP_PATCHED__||!window.AIImageApp)return;"
            + "window.__AI_IMAGE_APP_PATCHED__=true;"
            + "var native=window.AIImageApp;"
            + "function nativeResult(value){"
            + "  var text=String(value||'');"
            + "  if(text==='ok'||text.indexOf('ok:')===0)return Promise.resolve();"
            + "  return Promise.reject(new Error(text.replace(/^error:/,'')||'App 原生操作失败'));"
            + "}"
            + "function installClipboard(){"
            + "  var original=navigator.clipboard||{};"
            + "  var patched={};"
            + "  patched.writeText=function(text){return nativeResult(native.copyText(String(text||'')));};"
            + "  patched.write=function(items){"
            + "    try{"
            + "      var item=items&&items[0];"
            + "      if(!item)return Promise.reject(new Error('没有可复制的图片'));"
            + "      var types=item.types||[];"
            + "      var type=types.find(function(value){return /^image\\//.test(value);});"
            + "      if(!type)return Promise.reject(new Error('仅支持复制图片'));"
            + "      return item.getType(type).then(function(blob){"
            + "        return new Promise(function(resolve,reject){"
            + "          var reader=new FileReader();"
            + "          reader.onload=function(){nativeResult(native.copyImage(String(reader.result||''),'ai-image.png')).then(resolve,reject);};"
            + "          reader.onerror=function(){reject(reader.error||new Error('读取图片失败'));};"
            + "          reader.readAsDataURL(blob);"
            + "        });"
            + "      });"
            + "    }catch(error){return Promise.reject(error);}"
            + "  };"
            + "  try{Object.defineProperty(navigator,'clipboard',{value:Object.assign({},original,patched),configurable:true});}"
            + "  catch(error){try{original.writeText=patched.writeText;original.write=patched.write;}catch(ignore){}}"
            + "}"
            + "function blobToDataUrl(blob){"
            + "  return new Promise(function(resolve,reject){"
            + "    var reader=new FileReader();"
            + "    reader.onload=function(){resolve(String(reader.result||''));};"
            + "    reader.onerror=function(){reject(reader.error||new Error('读取图片失败'));};"
            + "    reader.readAsDataURL(blob);"
            + "  });"
            + "}"
            + "function saveDataUrl(dataUrl,fileName){"
            + "  var result=String(native.saveImage(dataUrl,fileName||'ai-image.png')||'');"
            + "  if(result.indexOf('error:')===0)throw new Error(result.replace(/^error:/,''));"
            + "}"
            + "function installDownload(){"
            + "  document.addEventListener('click',function(event){"
            + "    var target=event.target&&event.target.closest?event.target.closest('a[download]'):null;"
            + "    if(!target)return;"
            + "    var href=String(target.href||'');"
            + "    if(!href)return;"
            + "    event.preventDefault();event.stopPropagation();"
            + "    if(href.indexOf('data:image/')===0){"
            + "      try{saveDataUrl(href,target.download||'ai-image.png');}"
            + "      catch(error){alert('保存失败：'+(error&&error.message?error.message:error));}"
            + "      return;"
            + "    }"
            + "    fetch(href,{cache:'no-store'}).then(function(response){"
            + "      if(!response.ok)throw new Error('HTTP '+response.status);"
            + "      return response.blob();"
            + "    }).then(function(blob){"
            + "      if(blob.type&&!/^image\\//.test(blob.type))throw new Error('下载内容不是图片');"
            + "      return blobToDataUrl(blob);"
            + "    }).then(function(dataUrl){"
            + "      saveDataUrl(dataUrl,target.download||'ai-image.png');"
            + "    }).catch(function(error){alert('保存失败：'+(error&&error.message?error.message:error));});"
            + "  },true);"
            + "}"
            + "installClipboard();installDownload();"
        + "})();";

    public static class AIImageAppBridge {
        private final Context context;

        AIImageAppBridge(Context context) {
            this.context = context.getApplicationContext();
        }

        @JavascriptInterface
        public String copyText(String text) {
            try {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard == null) return "error:clipboard unavailable";
                clipboard.setPrimaryClip(ClipData.newPlainText("LyAI生图工作台", text == null ? "" : text));
                showToast("已复制");
                return "ok";
            } catch (Exception e) {
                return "error:" + e.getMessage();
            }
        }

        @JavascriptInterface
        public String copyImage(String dataUrl, String fileName) {
            try {
                ImagePayload image = parseDataUrl(dataUrl);
                File dir = new File(context.getCacheDir(), "clipboard-images");
                if (!dir.exists() && !dir.mkdirs()) return "error:create cache directory failed";

                File file = new File(dir, sanitizeFileName(fileName, image.mime));
                try (FileOutputStream output = new FileOutputStream(file)) {
                    output.write(image.bytes);
                }

                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard == null) return "error:clipboard unavailable";
                clipboard.setPrimaryClip(ClipData.newUri(context.getContentResolver(), "LyAI生图工作台", uri));
                showToast("图片已复制");
                return "ok:" + uri;
            } catch (Exception e) {
                return "error:" + e.getMessage();
            }
        }

        @JavascriptInterface
        public String saveImage(String dataUrl, String fileName) {
            try {
                ImagePayload image = parseDataUrl(dataUrl);
                String safeName = sanitizeFileName(fileName, image.mime);
                Uri uri;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentResolver resolver = context.getContentResolver();
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, safeName);
                    values.put(MediaStore.Images.Media.MIME_TYPE, image.mime);
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LyAI生图工作台");
                    values.put(MediaStore.Images.Media.IS_PENDING, 1);
                    uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    if (uri == null) return "error:create media item failed";

                    try (OutputStream output = resolver.openOutputStream(uri)) {
                        if (output == null) return "error:open media output failed";
                        output.write(image.bytes);
                    }

                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    resolver.update(uri, values, null, null);
                } else {
                    File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    if (dir == null) return "error:external pictures directory unavailable";
                    File appDir = new File(dir, "LyAI生图工作台");
                    if (!appDir.exists() && !appDir.mkdirs()) return "error:create pictures directory failed";
                    File file = new File(appDir, safeName);
                    try (FileOutputStream output = new FileOutputStream(file)) {
                        output.write(image.bytes);
                    }
                    uri = Uri.fromFile(file);
                }

                showToast("图片已保存到相册");
                return "ok:" + uri;
            } catch (Exception e) {
                return "error:" + e.getMessage();
            }
        }

        private void showToast(String text) {
            android.os.Handler handler = new android.os.Handler(context.getMainLooper());
            handler.post(() -> Toast.makeText(context, text, Toast.LENGTH_SHORT).show());
        }

        private static ImagePayload parseDataUrl(String dataUrl) {
            if (dataUrl == null || !dataUrl.startsWith("data:")) {
                throw new IllegalArgumentException("invalid data URL");
            }

            int comma = dataUrl.indexOf(',');
            if (comma < 0) throw new IllegalArgumentException("invalid data URL");

            String header = dataUrl.substring(5, comma);
            String[] parts = header.split(";");
            String mime = parts.length > 0 && parts[0].contains("/") ? parts[0] : "image/png";
            String payload = dataUrl.substring(comma + 1);
            byte[] bytes = Base64.decode(payload, Base64.DEFAULT);
            return new ImagePayload(mime, bytes);
        }

        private static String sanitizeFileName(String value, String mime) {
            String ext = extensionFromMime(mime);
            String raw = value == null || value.trim().isEmpty() ? "ai-image." + ext : value.trim();
            String safe = raw.replaceAll("[\\\\/:*?\"<>|]+", "-").replaceAll("\\s+", "-");
            if (safe.length() > 96) safe = safe.substring(0, 96);
            String lower = safe.toLowerCase(Locale.ROOT);
            if (!lower.endsWith(".png") && !lower.endsWith(".jpg") && !lower.endsWith(".jpeg") && !lower.endsWith(".gif") && !lower.endsWith(".webp")) {
                safe = safe + "." + ext;
            }
            return safe;
        }

        private static String extensionFromMime(String mime) {
            if ("image/jpeg".equalsIgnoreCase(mime)) return "jpg";
            if ("image/gif".equalsIgnoreCase(mime)) return "gif";
            if ("image/webp".equalsIgnoreCase(mime)) return "webp";
            return "png";
        }

        private static class ImagePayload {
            final String mime;
            final byte[] bytes;

            ImagePayload(String mime, byte[] bytes) {
                this.mime = mime;
                this.bytes = bytes;
            }
        }
    }
}
