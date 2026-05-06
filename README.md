# AI Image Generate APP

AI Image Generate 的 Android / iOS 移动端壳应用。这个仓库不重写前端页面，而是用 Capacitor WebView 内嵌线上站点：

```text
https://ai-image.ailinyu.dpdns.org/
```

线上 Web 端更新后，App 打开时会自动使用最新页面；本仓库主要负责移动端打包、安装包发布、原生桥接能力和使用说明。

## 核心说明

- App 内嵌站点：`https://ai-image.ailinyu.dpdns.org/`
- 技术方案：`Capacitor + WebView`
- 支持平台：Android APK、iOS 未签名 IPA
- 进入应用需要输入 **空间密码**，由用户自行设置。
- **输入相同空间密码，就进入同一个云端任务空间。**
- **输入不同空间密码，云端任务互相隔离。**
- 已取消单独的 Worker 访问密码，不再需要填写旧版固定密码。
- 空间密码不会明文保存；浏览器 / App 本地只保存不可逆派生后的访问令牌，Worker / D1 只保存归属 hash。

## 下载与安装

安装包在 GitHub Release 或 Actions Artifacts 中：

```text
https://github.com/y08lin4/AI-Image-generate-APP/releases
```

### Android

推荐下载：

```text
AI-Image-generate-release-signed.apk
```

安装方法：

1. 在手机上下载 APK。
2. 如果系统提示，允许浏览器或文件管理器「安装未知来源应用」。
3. 点击 APK 安装。
4. 打开 `AI Image Generate`。

如果只是测试，也可以下载 debug 版本：

```text
AI-Image-generate-debug.apk
```

### iOS

iOS 产物是未签名 IPA：

```text
AI-Image-generate-ios-unsigned.ipa
```

未签名 IPA 不能直接安装，需要自己用签名工具安装，例如：

- AltStore
- Sideloadly
- 爱思助手
- TrollStore
- ESign / Scarlet

大致流程：

1. 下载 `AI-Image-generate-ios-unsigned.ipa`。
2. 用自己的 Apple ID、开发者证书或自签证书签名。
3. 安装到 iPhone / iPad。
4. 首次打开时按系统提示信任证书。

## 首次使用流程

1. 安装并打开 App。
2. 页面会要求输入 **空间密码**。
3. 空间密码由你自己设置，必须是 **10 位以上的复杂密码**。
4. 输入后进入对应空间，再到「设置」里填写 API 配置。
5. 选择生成模式、模型、比例、分辨率和张数。
6. 输入提示词，开始生成图片。
7. 长时间任务建议在请求方式里选择 `Worker 后台任务`。
8. 如果 App 被切到后台，回到前台后会自动同步当前空间下的云端任务，也可以手动点击「同步云端任务」。

## 空间密码是什么

空间密码可以理解为这个应用的身份令牌，也可以理解为云端任务空间的钥匙。

规则很简单：

- 你输入 `密码 A`，就进入 `空间 A`。
- 别的设备也输入同一个 `密码 A`，也会进入同一个 `空间 A`，可以同步同一批云端后台任务。
- 你输入 `密码 B`，就进入另一个独立空间，看不到 `空间 A` 的任务。
- 如果多人使用了同一个空间密码，他们会看到同一个云端任务空间。
- 如果不想和别人混在一起，请设置只有自己知道的高强度空间密码。

重要提醒：

- 空间密码不是平台分配的账号密码，而是用户自行设置的私密令牌。
- 服务端不保存明文空间密码，所以忘记后无法找回原空间。
- 换手机、换浏览器或重新安装 App 后，只要输入原来的空间密码，就能进入原来的云端任务空间。
- 如果输入了新密码，会进入一个新的空空间，这不是数据丢失。

## 空间密码强度要求

空间密码必须至少 10 位，并且不能太简单。

会被拒绝的弱密码类型包括：

- 长度少于 10 位。
- 全部是同一个字符，例如：`1111111111`、`aaaaaaaaaa`。
- 连续数字或倒序数字，例如：`1234567890`、`0123456789`、`9876543210`。
- 连续字母，例如：`abcdefghij`、`zyxwvutsrq`。
- 键盘顺序，例如：`qwertyuiop`、`asdfghjkl`、`zxcvbnm123`。
- 重复片段，例如：`1234512345`、`abcabcabcabc`。
- 常见弱词，例如：`password123`、`adminadmin1`、`welcome123`。
- 明显日期或年份组合，例如：`2024010100`、`1990010112`。
- 字符类型过于单一，例如只包含数字、只包含小写字母。

推荐写法：

- 至少 12-16 位。
- 同时包含大写字母、小写字母、数字和符号。
- 不要使用生日、手机号、姓名拼音、常见单词或平台默认密码。
- 可以使用自己能记住的长短语加符号，例如类似 `River!Photo_2026X` 这种结构。

> 上面的示例只用于说明格式，不建议直接照抄使用。

## API 设置方法

进入空间后，打开页面里的「设置」，填写以下内容：

### API URL

填写上游接口根地址，例如：

```text
https://api.openai.com/v1
```

如果你使用第三方兼容接口，也填写它的 `/v1` 根地址。不要把完整接口路径填进去，例如不要填写 `/v1/images/generations`。

### API Key

填写你的上游 API Key。

说明：

- API Key 保存在 App WebView 本地存储里。
- Worker 不会把 API Key 写入 D1。
- 后台任务执行时，API Key 会被传给 Cloudflare Workflow 实例用于本次任务；任务状态落库时不会保存 API Key。
- 换设备后需要在新设备上重新填写 API Key。

### 模型

图片模型按页面支持项填写，常用示例：

```text
gpt-image-2
```

### 请求方式

页面支持三种请求方式：

#### Worker 流式代理

```text
App/WebView -> Worker -> 上游图片接口
```

适合大多数情况，也是常规推荐模式。

特点：

- 可以绕过上游 CORS 限制。
- 生成过程中会通过 SSE 保活。
- 多图生成时，哪张先完成就先显示哪张。
- 需要先输入空间密码。

#### Worker 后台任务

```text
App/WebView -> Worker -> Cloudflare Workflows -> 上游图片接口 -> PiXhost/D1 -> App/WebView
```

App 端长时间生成推荐使用这个模式。

特点：

- 适合 100 秒以上、容易断流的长任务。
- App 切后台后，任务仍可由 Cloudflare Workflows 继续执行。
- 回到前台后会自动同步当前空间下的未完成任务。
- 也可以手动点击「同步云端任务」。
- 云端任务按空间密码隔离，只有相同空间密码才能同步同一个空间的任务。

#### 浏览器直连

```text
App/WebView -> 上游图片接口
```

只有在上游接口支持浏览器 CORS 时才建议使用。

特点：

- 链路最短，API Key 不经过 Worker。
- 如果出现 `Failed to fetch` 或 CORS 报错，请切换到 Worker 模式。
- HTTPS 页面无法直连 HTTP 接口。

## 生图使用方法

1. 输入空间密码进入应用。
2. 打开「设置」，填写 API URL、API Key 和模型。
3. 选择请求方式：
   - 日常使用：`Worker 流式代理`
   - App 长任务：`Worker 后台任务`
   - 上游支持 CORS：可尝试 `浏览器直连`
4. 选择文生图或图生图。
5. 填写提示词。
6. 选择比例、分辨率、生成张数和并发数。
7. 点击生成。
8. 生成完成后可以下载、复制图片、复制 URL、放大预览、作为图生图参考图。
9. 如果开启 PiXhost 图床上传，成功后可以复制图床直链。

## 后台任务与云端同步

后台任务主要用于解决 App / WebView 切后台后容易断流的问题。

推荐场景：

- 4K 图片。
- 多张图片。
- 预计生成时间超过 100 秒。
- 手机可能锁屏或切到其他 App。

同步规则：

- 云端任务按空间密码隔离。
- 输入相同空间密码的设备可以同步同一个空间的后台任务。
- 输入不同空间密码的设备不会互相看到任务。
- 如果发现同步到了别人的任务，说明双方使用了同一个空间密码，请立即更换为更复杂、只有自己知道的新空间密码。
- 如果忘记原空间密码，无法恢复原空间，只能使用新密码进入新空间。

App 前台恢复时会自动触发同步：

- Android / iOS 切后台后再回来，会尝试同步未完成任务。
- 页面获得焦点时会尝试刷新任务状态。
- 也可以手动点击「同步云端任务」。

## 原生能力

App 内注入了原生桥接，用来改善移动端体验：

- 保存图片到相册。
- 复制图片 URL。
- 复制图片本身。
- 保留 WebView 本地存储配置。
- App 回到前台后触发网页同步后台任务。
- 图床图片展示、下载、复制时可通过 Worker 图片代理处理，减少 WebView 直链跳转和 CORS 问题。

## 项目信息

| 项目 | 内容 |
| --- | --- |
| App 名称 | `AI Image Generate` |
| App ID | `com.ailinyu.aiimagegenerate` |
| 内嵌地址 | `https://ai-image.ailinyu.dpdns.org/` |
| Web 目录 | `www` |
| 技术方案 | `Capacitor + WebView` |

配置文件：

```text
capacitor.config.json
```

如果以后要改内嵌站点地址，修改 `capacitor.config.json` 里的：

```json
{
  "server": {
    "url": "https://ai-image.ailinyu.dpdns.org/"
  }
}
```

修改后重新执行对应平台同步和打包命令。

## 本地打包

### 准备环境

通用要求：

- Node.js
- npm

Android 额外需要：

- JDK
- Android Studio 或 Android SDK

iOS 额外需要：

- macOS
- Xcode

安装依赖：

```bash
npm install
```

### Android 本地打包

同步 Android 工程：

```bash
npm run android:sync
```

构建 debug APK：

```bash
npm run android:build:debug
```

构建 release APK：

```bash
npm run android:build:release
```

输出位置：

```text
android/app/build/outputs/apk/debug/app-debug.apk
android/app/build/outputs/apk/release/app-release.apk
```

说明：

- 如果本地没有配置 keystore，release 包可能是未签名或不可直接分发的包。
- 正式分发建议使用 GitHub Actions 中配置好的签名流程，或在本地配置自己的 keystore。

### iOS 本地打包

同步 iOS 工程：

```bash
npm run ios:sync
```

打开 Xcode：

```bash
npm run ios:open
```

然后在 Xcode 里选择证书、设备和构建方式。

## GitHub Actions 自动打包

仓库已配置两个 workflow：

```text
Actions -> Build Android APK
Actions -> Build iOS IPA
```

触发方式：

- 推送到 `main` 分支会自动触发。
- 也可以在 GitHub Actions 页面手动点击 `workflow_dispatch` 运行。

### Android Actions 产物

Android workflow 会输出：

- `ai-image-generate-release-signed-apk`
- `ai-image-generate-debug-apk`

对应文件通常是：

- `AI-Image-generate-release-signed.apk`
- `AI-Image-generate-debug.apk`

Android release 签名依赖仓库 Secrets：

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

### iOS Actions 产物

iOS workflow 会输出：

- `ai-image-generate-ios-unsigned-ipa`

对应文件：

```text
AI-Image-generate-ios-unsigned.ipa
```

这是未签名 IPA，需要用户自行签名安装。

## 常见问题

### 现在还需要填写旧版 Worker 固定密码吗？

不需要。

新版已经取消单独的 Worker 访问密码，改为使用空间密码派生出的访问令牌。你只需要输入自己设置的空间密码。

### 为什么同步云端任务时会看到别人的任务？

云端任务空间由空间密码决定。

如果两个人输入了同一个空间密码，就会进入同一个空间，也会同步同一批云端任务。请使用更复杂、更私密的空间密码，不要使用 `1234567890`、`1111111111`、`password123` 这类弱密码。

### 忘记空间密码怎么办？

无法找回。

服务端不保存明文空间密码，也无法反推出原密码。忘记后只能设置一个新的空间密码进入新空间。

### 换手机后怎么同步原来的云端任务？

在新手机或新浏览器里输入原来的空间密码，就会进入同一个云端任务空间。

注意：API Key 是保存在本机 WebView 本地存储里的，换设备后需要重新填写 API Key。

### 为什么我输入新密码后任务不见了？

因为新密码对应新空间。请确认是否输入了原来的空间密码。

### iOS IPA 为什么不能直接安装？

因为仓库提供的是未签名 IPA。iOS 系统要求 App 必须签名后才能安装，所以需要使用 AltStore、Sideloadly、爱思助手、TrollStore、ESign / Scarlet 等工具自行签名。

### Android 安装时提示风险怎么办？

这是因为 APK 不是从应用商店安装。请确认下载来源是本仓库 Release 或 Actions 产物，再按系统提示允许安装未知来源应用。

### App 里配置会和手机浏览器同步吗？

不会。

App WebView 的本地存储和手机浏览器不是同一份。空间密码可以让云端任务空间一致，但 API Key、API URL 等本地配置需要分别填写。

## 备注

- App 是 WebView 壳应用，核心功能由线上 Web 站点提供。
- Web 端更新后，App 通常不需要重新发版即可使用新页面。
- API URL、API Key、模型、请求方式等配置保存在 App WebView 本地存储。
- 空间密码明文不会保存在 App、浏览器、Worker 或 D1 中。
- 长时间生图建议使用 `Worker 后台任务`；流式代理模式在系统强制挂起 WebView 时仍可能断流。
- iOS 未签名 IPA 不能直接安装，必须先签名。
