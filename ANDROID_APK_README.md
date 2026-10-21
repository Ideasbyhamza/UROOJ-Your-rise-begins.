# UROOJ Android APK

The Android wrapper opens the current UROOJ GitHub Pages app in an Android WebView, with JavaScript, DOM storage, cookies, navigation/back support, and HTTPS-only content loading enabled.

## Build
1. Open the repository's **Actions** tab.
2. Select **Build UROOJ Android APK**.
3. Choose **Run workflow**.
4. After the run succeeds, open the run and download the **UROOJ-Android-APK** artifact ZIP.
5. Extract it to get `app-debug.apk`, then install it on Android.

This workflow produces a debug-signed APK suitable for testing and direct sharing. It is not a Play Store release build. The app loads the hosted website, so internet access is required and changes to the hosted website appear in the app. Browser-based Google sign-in may behave differently inside WebView and must be tested on a real device before describing the APK as fully verified.
