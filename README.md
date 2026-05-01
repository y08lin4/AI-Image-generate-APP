# AI Image Generate App

AI Image Generate 的移动端壳应用，不重写前端，只用 Capacitor WebView 内嵌线上站点：

```text
https://ai-image.ailinyu.dpdns.org/
```

Web 端更新后，App 打开时会自动使用最新页面。

## 支持平台

- Android：输出 APK，可直接安装。
- iOS：输出未签名 IPA，用户自行自签安装。

## 下载

安装包在 Release 或 Actions Artifacts 里：

```text
https://github.com/y08lin4/AI-Image-generate-APP/releases
```

Android 推荐下载：

```text
AI-Image-generate-release-signed.apk
```

iOS 自签版产物：

```text
AI-Image-generate-ios-unsigned.ipa
```

## iOS 自签说明

iOS 的 IPA 默认不内置证书，需要用户自己用自签工具安装，例如：

- AltStore
- Sideloadly
- 爱思助手
- TrollStore
- ESign / Scarlet 等

流程大致是：

1. 下载 `AI-Image-generate-ios-unsigned.ipa`
2. 用自己的 Apple ID 或自签证书签名
3. 安装到 iPhone / iPad

## 项目信息

- App 名称：`AI Image Generate`
- App ID：`com.ailinyu.aiimagegenerate`
- 内嵌地址：`https://ai-image.ailinyu.dpdns.org/`
- 技术方案：`Capacitor + WebView`

## 原生能力

App 内注入了原生桥接，用于改善移动端体验：

- 保存图片到相册
- 复制 URL
- 复制图片
- 保留 WebView 本地存储配置

## 本地打包

### Android

需要 Node.js、JDK、Android Studio / Android SDK。

```bash
npm install
npm run android:sync
npm run android:build:debug
npm run android:build:release
```

APK 输出位置：

```text
android/app/build/outputs/apk/debug/app-debug.apk
android/app/build/outputs/apk/release/app-release.apk
```

### iOS

iOS 本地打包需要 macOS + Xcode：

```bash
npm install
npm run ios:sync
npm run ios:open
```

然后在 Xcode 里构建或签名。

## GitHub Actions 自动打包

仓库已配置两个 workflow：

```text
Actions -> Build Android APK
Actions -> Build iOS IPA
```

Android 会输出：

- `ai-image-generate-release-signed-apk`
- `ai-image-generate-debug-apk`

iOS 会输出：

- `ai-image-generate-ios-unsigned-ipa`

## 备注

- API Key、API URL、Worker 密码保存在 App WebView 本地存储里。
- App WebView 历史和手机浏览器历史不是同一份。
- 长时间生图时建议保持 App 在前台。
- iOS 未签名 IPA 不能直接安装，必须先自签。
