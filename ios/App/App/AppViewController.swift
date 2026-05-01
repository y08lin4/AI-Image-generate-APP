import UIKit
import WebKit
import Capacitor
import Photos

class AppViewController: CAPBridgeViewController, WKScriptMessageHandler {
    override func capacitorDidLoad() {
        super.capacitorDidLoad()

        let userContentController = webView?.configuration.userContentController
        userContentController?.addUserScript(WKUserScript(
            source: Self.bridgeScript,
            injectionTime: .atDocumentStart,
            forMainFrameOnly: false
        ))
        userContentController?.add(self, name: "AIImageApp")
    }

    deinit {
        webView?.configuration.userContentController.removeScriptMessageHandler(forName: "AIImageApp")
    }

    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        guard message.name == "AIImageApp",
              let body = message.body as? [String: Any],
              let id = body["id"] as? String,
              let method = body["method"] as? String,
              let args = body["args"] as? [String] else {
            return
        }

        switch method {
        case "copyText":
            copyText(args.first ?? "", id: id)
        case "copyImage":
            copyImage(dataUrl: args.first ?? "", fileName: args.dropFirst().first ?? "ai-image.png", id: id)
        case "saveImage":
            saveImage(dataUrl: args.first ?? "", fileName: args.dropFirst().first ?? "ai-image.png", id: id)
        default:
            return
        }
    }

    private func copyText(_ text: String, id: String) {
        DispatchQueue.main.async {
            UIPasteboard.general.string = text
            self.respond(id: id, ok: true, value: "ok")
            self.showToast("已复制")
        }
    }

    private func copyImage(dataUrl: String, fileName _: String, id: String) {
        do {
            let image = try decodeImage(dataUrl)
            DispatchQueue.main.async {
                UIPasteboard.general.image = image
                self.respond(id: id, ok: true, value: "ok")
                self.showToast("图片已复制")
            }
        } catch {
            respond(id: id, ok: false, value: error.localizedDescription)
            showToast("复制失败：\(error.localizedDescription)")
        }
    }

    private func saveImage(dataUrl: String, fileName _: String, id: String) {
        do {
            let image = try decodeImage(dataUrl)
            requestPhotoPermission { allowed in
                guard allowed else {
                    self.respond(id: id, ok: false, value: "没有相册权限")
                    self.showToast("保存失败：没有相册权限")
                    return
                }

                PHPhotoLibrary.shared().performChanges({
                    PHAssetChangeRequest.creationRequestForAsset(from: image)
                }) { success, error in
                    if success {
                        self.respond(id: id, ok: true, value: "ok")
                        self.showToast("图片已保存到相册")
                    } else {
                        let message = error?.localizedDescription ?? "未知错误"
                        self.respond(id: id, ok: false, value: message)
                        self.showToast("保存失败：\(message)")
                    }
                }
            }
        } catch {
            respond(id: id, ok: false, value: error.localizedDescription)
            showToast("保存失败：\(error.localizedDescription)")
        }
    }

    private func requestPhotoPermission(_ completion: @escaping (Bool) -> Void) {
        if #available(iOS 14, *) {
            PHPhotoLibrary.requestAuthorization(for: .addOnly) { status in
                completion(status == .authorized || status == .limited)
            }
        } else {
            PHPhotoLibrary.requestAuthorization { status in
                completion(status == .authorized)
            }
        }
    }

    private func decodeImage(_ dataUrl: String) throws -> UIImage {
        guard dataUrl.hasPrefix("data:"),
              let commaIndex = dataUrl.firstIndex(of: ",") else {
            throw BridgeError.invalidDataUrl
        }

        let payload = String(dataUrl[dataUrl.index(after: commaIndex)...])
        guard let data = Data(base64Encoded: payload, options: [.ignoreUnknownCharacters]),
              let image = UIImage(data: data) else {
            throw BridgeError.invalidImage
        }

        return image
    }

    private func showToast(_ message: String) {
        DispatchQueue.main.async {
            guard self.presentedViewController == nil else { return }
            let alert = UIAlertController(title: nil, message: message, preferredStyle: .alert)
            self.present(alert, animated: true)
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.2) {
                alert.dismiss(animated: true)
            }
        }
    }

    private func respond(id: String, ok: Bool, value: String) {
        let script = "window.__AIImageAppResolve && window.__AIImageAppResolve(\(jsStringLiteral(id)), \(ok ? "true" : "false"), \(jsStringLiteral(value)));"
        DispatchQueue.main.async {
            self.webView?.evaluateJavaScript(script, completionHandler: nil)
        }
    }

    private func jsStringLiteral(_ value: String) -> String {
        let data = try? JSONSerialization.data(withJSONObject: [value], options: [])
        let json = String(data: data ?? Data("[\"\"]".utf8), encoding: .utf8) ?? "[\"\"]"
        return String(json.dropFirst().dropLast())
    }

    private enum BridgeError: LocalizedError {
        case invalidDataUrl
        case invalidImage

        var errorDescription: String? {
            switch self {
            case .invalidDataUrl:
                return "图片数据无效"
            case .invalidImage:
                return "图片解码失败"
            }
        }
    }

    private static let bridgeScript = """
    (function () {
      if (window.AIImageApp || !window.webkit || !window.webkit.messageHandlers || !window.webkit.messageHandlers.AIImageApp) {
        return;
      }

      function post(method, args) {
        return new Promise(function(resolve, reject) {
          var id = String(Date.now()) + '_' + String(nextId += 1);
          pending[id] = { resolve: resolve, reject: reject };
          try {
            window.webkit.messageHandlers.AIImageApp.postMessage({
              id: id,
              method: method,
              args: Array.prototype.slice.call(args || []).map(function (value) {
                return String(value || '');
              })
            });
          } catch (error) {
            delete pending[id];
            reject(new Error(error && error.message ? error.message : String(error || 'iOS 原生操作失败')));
          }
        });
      }

      var pending = {};
      var nextId = 0;
      window.__AIImageAppResolve = function (id, ok, value) {
        var item = pending[id];
        if (!item) {
          return;
        }
        delete pending[id];
        if (ok) {
          item.resolve(String(value || 'ok'));
        } else {
          item.reject(new Error(String(value || 'iOS 原生操作失败')));
        }
      };

      window.AIImageApp = {
        copyText: function (text) {
          return post('copyText', [text]);
        },
        copyImage: function (dataUrl, fileName) {
          return post('copyImage', [dataUrl, fileName || 'ai-image.png']);
        },
        saveImage: function (dataUrl, fileName) {
          return post('saveImage', [dataUrl, fileName || 'ai-image.png']);
        }
      };
    })();
    """
}
