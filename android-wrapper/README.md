# UROOJ Android wrapper

This project packages the hosted UROOJ web app in a minimal Android WebView shell. It does not bundle the web app's HTML offline and requires internet access.

## Build

A GitHub Actions workflow builds a debug APK and uploads it as an artifact. Open **Actions → Build UROOJ Android APK → latest successful run → Artifacts** to download it.

## Verify before release

1. Confirm `APP_URL` in `app/src/main/java/com/urooj/app/MainActivity.java` points to the actual live GitHub Pages URL.
2. Install the debug APK and test the app on a real Android phone.
3. Test Google sign-in and Drive backup. Google may block OAuth inside embedded WebViews; if so, use a supported external-browser OAuth flow or a verified Trusted Web Activity.
4. Debug APKs are for testing. A Play Store release requires a proper signing key, privacy/data-safety review, and release build configuration.

The app does not expose a JavaScript-to-native bridge. JavaScript is enabled only because the hosted web app requires it. File/content access and cleartext HTTP are disabled.
