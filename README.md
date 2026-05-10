# LyAI生图工作台 APP

This repository packages LyAI生图工作台 as a Capacitor Android / iOS WebView app.

The app does not include the Go backend and does not duplicate the web frontend. It opens the online LyAI workbench:

```text
https://ai-image.ailinyu.de/
```

The backend still runs on your server. When the web site is updated, users usually do not need to reinstall the app; reopening the app loads the latest online page.

## Core information

| Item | Value |
| --- | --- |
| App name | `LyAI生图工作台` |
| App ID | `com.ailinyu.aiimagegenerate` |
| Embedded site | `https://ai-image.ailinyu.de/` |
| Tech stack | `Capacitor + WebView` |
| Android output | APK |
| iOS output | unsigned IPA |

Users enter the same LyAI web workflow inside the app: space login, settings, codex-key, Banana key, generation, queue, prompt assistant, result preview, download and image reuse.

## Install

Artifacts are available in GitHub Releases or GitHub Actions artifacts:

```text
https://github.com/y08lin4/AI-Image-generate-APP/releases
```

Android artifacts:

```text
AI-Image-generate-release-signed.apk
AI-Image-generate-debug.apk
```

iOS artifact:

```text
AI-Image-generate-ios-unsigned.ipa
```

The iOS IPA is unsigned. Sign it with AltStore, Sideloadly, TrollStore, ESign, Scarlet, Xcode, or another signing tool before installation.

## Local build

Install dependencies:

```bash
npm install
```

Sync Android:

```bash
npm run android:sync
```

Build Android debug APK:

```bash
npm run android:build:debug
```

Build Android release APK:

```bash
npm run android:build:release
```

Sync iOS:

```bash
npm run ios:sync
```

Open iOS project:

```bash
npm run ios:open
```

## Change embedded site

Edit `capacitor.config.json`:

```json
{
  "server": {
    "url": "https://ai-image.ailinyu.de/"
  }
}
```

Then sync native projects:

```bash
npm run android:sync
npm run ios:sync
```

## GitHub Actions

The repository contains two workflows:

```text
Build Android APK
Build iOS IPA
```

They run automatically on pushes to `main`, and can also be started manually with `workflow_dispatch`.

## Notes

- This is a WebView shell app; the online LyAI web site and server provide the real features.
- The App ID is intentionally kept as `com.ailinyu.aiimagegenerate` for upgrade/signing compatibility.
- WebView local storage is separate from the phone browser. Users may need to fill keys/settings again inside the app.
- If `https://ai-image.ailinyu.de/` is unavailable, the app cannot enter the workbench.
