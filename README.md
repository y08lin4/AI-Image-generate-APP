# AI Image Generate APK

AI Image Generate 的安卓壳应用。它不重写前端，只用 Capacitor WebView 内嵌线上站点：

```text
https://ai-image.ailinyu.dpdns.org/
```

打开 App 后就是移动端生图页面，Web 端更新后 App 也会自动使用最新版本。

## 下载

测试包在 Release 里：

```text
https://github.com/y08lin4/AI-Image-generate-APK/releases/tag/v0.1.0-debug
```

当前 APK：

```text
AI-Image-generate-debug.apk
```

> 这是 debug 测试包，适合自己安装测试；正式分发前建议再做 release 签名版。

## 项目信息

- App 名称：`AI Image Generate`
- App ID：`com.ailinyu.aiimagegenerate`
- 内嵌地址：`https://ai-image.ailinyu.dpdns.org/`
- 技术方案：`Capacitor + Android WebView`

## 本地打包

需要安装 Node.js、JDK、Android Studio / Android SDK。

```bash
npm install
npm run sync
npm run android:build:debug
```

APK 输出位置：

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

也可以用 Android Studio 打开：

```bash
npm run open
```

## 自动打包

仓库已配置 GitHub Actions：

```text
Actions -> Build Android APK -> Run workflow
```

完成后在 Artifacts 下载 debug APK。

## 备注

- API Key、API URL、Worker 密码保存在 App WebView 本地存储里。
- WebView 历史和手机浏览器历史不是同一份。
- 长时间生图时建议保持 App 在前台。
