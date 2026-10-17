# UROOJ Android wrapper

Version 1.0.2 bundles the web app HTML inside the APK, so the main app screen no longer depends on GitHub Pages being online to open. The app uses AndroidX WebViewAssetLoader and the HTTPS-like `appassets.androidplatform.net` origin. It does not expose a JavaScript-to-native bridge.

## Build and download

GitHub Actions builds and validates the debug APK. Open **Actions → Build UROOJ Android APK → latest successful run → Artifacts** and download `UROOJ-debug-apk-v1.0.2`.

## First launch and Google sign-in

- A first-run tutorial walks through Home, Digital Tasbeeh, Today's Task, Zikr & Muhasaba, Challenges, Settings, and backups. It can be replayed from Home → About UROOJ.
- Google OAuth is intentionally disabled in the bundled app origin. For Google Drive backup, open the hosted website in Chrome and connect Google there. The app remains usable locally without Google.
- Online content/links may still require internet. The core HTML is bundled so startup is not blocked by GitHub Pages availability.

## Test before release

Install the debug APK on a real Android phone. Check first launch, tutorial navigation, local save persistence, dark mode, external links, and the no-Google flow. Debug builds are for testing, not Play Store release.
