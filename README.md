# LyAI 生图工作台 APP

这是 **LyAI 生图工作台** 的 Android / iOS 移动端 WebView 壳应用。APP 不内置 Go 后端，也不复制网页前端逻辑，而是通过 Capacitor WebView 打开线上站点：

```text
https://ai-image.ailinyu.de/
```

后端仍运行在你的服务器上。网页端更新后，APP 通常不需要重新发版；用户重新打开 APP 就会加载最新线上页面。

## 核心信息

| 项目 | 内容 |
| --- | --- |
| APP 名称 | `LyAI 生图工作台` |
| APP ID | `com.ailinyu.aiimagegenerate` |
| 内嵌站点 | `https://ai-image.ailinyu.de/` |
| 技术方案 | `Capacitor + WebView` |
| Android 产物 | APK |
| iOS 产物 | 未签名 IPA |

进入 APP 后，用户使用的就是线上 LyAI 工作台流程：空间登录、设置 codex-key、设置 Banana Key、提交生图任务、查看队列、使用提示词助手、预览结果图、下载图片、复制图片、作为参考图等。

## 下载与安装

安装包会放在 GitHub Release 或 GitHub Actions Artifacts：

```text
https://github.com/y08lin4/AI-Image-generate-APP/releases
```

### Android

常用文件：

```text
AI-Image-generate-release-signed.apk
AI-Image-generate-debug.apk
```

安装步骤：

1. 在手机上下载 APK。
2. 如果系统提示，请允许浏览器或文件管理器“安装未知来源应用”。
3. 点击 APK 安装。
4. 打开 `LyAI 生图工作台`。

### iOS

常用文件：

```text
AI-Image-generate-ios-unsigned.ipa
```

这是未签名 IPA，不能直接安装。需要使用 AltStore、Sideloadly、TrollStore、ESign、Scarlet、Xcode 等工具自行签名后安装。

## 使用方式

1. 打开 APP。
2. APP 会加载 `https://ai-image.ailinyu.de/`。
3. 输入空间密码进入个人空间。
4. 到“设置”里填写 `codex-key`、Banana 分组 Key、默认数量、默认并发、图床等配置。
5. 在“生成”页填写提示词，也可以使用提示词助手生成或修改提示词。
6. 选择模型、模式、规格、质量、格式、数量和并发。
7. 提交任务后到“结果”页查看图片，到“队列”页查看历史任务和异常任务。
8. 结果图支持预览、下载、复制图片、复制链接、作为参考图、上传图床。

## 本地打包

准备环境：

- Node.js
- npm
- Android：JDK + Android Studio 或 Android SDK
- iOS：macOS + Xcode

安装依赖：

```bash
npm install
```

同步 Android 工程：

```bash
npm run android:sync
```

构建 Android debug APK：

```bash
npm run android:build:debug
```

构建 Android release APK：

```bash
npm run android:build:release
```

同步 iOS 工程：

```bash
npm run ios:sync
```

打开 iOS 工程：

```bash
npm run ios:open
```

## 修改内嵌站点

如果以后要修改 APP 打开的线上地址，编辑 `capacitor.config.json`：

```json
{
  "server": {
    "url": "https://ai-image.ailinyu.de/"
  }
}
```

修改后重新同步原生工程：

```bash
npm run android:sync
npm run ios:sync
```

然后重新打包，或推送到 `main` 分支触发 GitHub Actions 自动打包。

## GitHub Actions 自动打包

仓库包含两个 workflow：

```text
Build Android APK
Build iOS IPA
```

触发方式：

- 推送到 `main` 分支会自动触发。
- 也可以在 GitHub Actions 页面手动运行。

Android workflow 输出：

```text
AI-Image-generate-debug.apk
AI-Image-generate-release-signed.apk
```

iOS workflow 输出：

```text
AI-Image-generate-ios-unsigned.ipa
```

## 注意事项

- 这个仓库只是 APP 壳，核心功能由线上 LyAI Web 站点和服务器后端提供。
- APP ID 暂时保持 `com.ailinyu.aiimagegenerate`，避免影响已安装用户升级、签名和包名兼容。
- APP WebView 的本地存储和手机浏览器不是同一份；空间、Key、设置需要在 APP 内按页面流程填写。
- 如果 `https://ai-image.ailinyu.de/` 无法访问，APP 也无法进入工作台。
