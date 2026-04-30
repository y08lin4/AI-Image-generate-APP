# AI Image Generate APK

这是 AI Image Generate 的安卓壳应用项目：使用 Capacitor 内嵌 WebView，直接打开线上 Web 站点。

- 内嵌地址：`https://ai-image.ailinyu.dpdns.org/`
- App ID：`com.ailinyu.aiimagegenerate`
- App 名称：`AI Image Generate`

## 方案说明

本项目不重写前端页面，只把现有 Web 站点打包进 Android App：

```text
Android App -> Capacitor WebView -> https://ai-image.ailinyu.dpdns.org/
```

这样 Web 端更新后，App 下次打开即可使用最新页面。

## 开发环境

需要安装：

- Node.js 20+
- JDK 17 或 21
- Android Studio
- Android SDK / Platform Tools

> 当前项目已经包含 Capacitor 配置和 Android 工程；如果本机没有 Android SDK，可以先用 Android Studio 打开 `android/` 后按提示安装 SDK。

## 常用命令

安装依赖：

```bash
npm install
```

同步 Capacitor 配置到 Android：

```bash
npm run sync
```

用 Android Studio 打开工程：

```bash
npm run open
```

构建 debug APK：

```bash
npm run android:build:debug
```

构建产物通常在：

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

## 修改内嵌 Web 地址

编辑 `capacitor.config.json`：

```json
"server": {
  "url": "https://ai-image.ailinyu.dpdns.org/",
  "cleartext": false,
  "androidScheme": "https"
}
```

修改后执行：

```bash
npm run sync
```

## 注意事项

- 建议使用 HTTPS 地址；当前配置不允许明文 HTTP。
- API Key / URL / Worker 密码仍然保存在 WebView 本地存储里。
- WebView 的本地历史和手机浏览器历史不是同一份。
- 图片下载、剪贴板、文件选择建议在真机上完整测试。
- 长时间生图时，请尽量保持 App 在前台，避免被系统后台限制中断。

## GitHub Actions 自动打包

仓库内置 `.github/workflows/build-apk.yml`，可以在 GitHub 页面手动运行：

```text
Actions -> Build Android APK -> Run workflow
```

运行完成后，在 workflow 的 Artifacts 里下载：

```text
ai-image-generate-debug-apk
```

其中包含 debug APK：

```text
app-debug.apk
```
