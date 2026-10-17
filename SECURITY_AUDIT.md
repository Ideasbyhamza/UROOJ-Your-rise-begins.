# UROOJ Security Review

**Review date:** 2026-10-17  
**Scope:** Static inspection of `index.html`, `UROOJ.html`, `privacy.html`, `terms.html`, `SETUP_GOOGLE_DRIVE.md`, and `google-apps-script-feedback.gs`. This is not a penetration test, dynamic test, or formal security certification.

## Summary

The first automated scan did not identify obvious private keys or GitHub access tokens embedded in the main HTML. The app is a client-side web app with local browser storage and optional Google OAuth/Drive access.

## Findings

### Medium — User-generated content rendered with `innerHTML`
The main HTML contains multiple `.innerHTML =` assignments. Each must be checked to confirm user-controlled values are escaped before insertion. User-controlled fields include reflections, task names, custom reminders/challenges, and feedback. A single unsafe insertion could permit DOM-based cross-site scripting (XSS) in the app's origin.

**Recommended fix:** Prefer `textContent` and DOM element creation for plain text. Where rich markup is required, use a small reviewed escaping helper or a vetted sanitizer. Add tests with payloads such as `<img src=x onerror=alert(1)>` and `<script>`.

### Medium — Local data is not protected from other users of the same browser
Progress is stored in `localStorage`. This is normal for a local-first app, but data may be visible to someone with access to the unlocked browser/device. Browser storage is not a secure vault.

**Recommended fix:** Clearly explain shared-device risk, provide an in-app lock option if appropriate, and offer data export/delete controls. Do not store passwords, OAuth access tokens, or highly sensitive secrets in localStorage.

### Medium — Google OAuth / Drive flow needs production verification
The app requests Google sign-in and Drive access. OAuth client configuration, authorized origins, consent-screen status, token lifecycle, and Drive permissions must be verified in Google Cloud. Do not add a client secret to browser code. Use the narrowest scope that supports the backup feature.

**Recommended fix:** Review the OAuth client and consent configuration in Google Cloud; test sign-in, cancellation, sign-out, token expiry, and backup/restore on the deployed origin. Rotate credentials if any secret was ever committed.

### Medium — Feedback service is not configured in the frontend
The frontend contains `PASTE_APPS_SCRIPT_WEB_APP_URL_HERE`. Feedback email delivery is therefore not connected until a real Apps Script Web App URL is configured and deployed.

**Recommended fix:** Deploy the script deliberately, restrict and rate-limit submissions, validate all fields server-side, and do not treat client-side validation or a honeypot as authentication. Avoid returning internal exception details.

### Low/Medium — No explicit Content Security Policy or Subresource Integrity found
The main HTML did not include a Content Security Policy meta/header or SRI attributes in the inspected version. A restrictive policy can reduce XSS impact, but it must be designed around the app's inline JavaScript/styles and Google Identity Services.

**Recommended fix:** Prefer a server-set CSP header where possible. Start in report-only mode, inventory required Google endpoints, remove unnecessary inline handlers, then enforce a tested policy. Add SRI only to stable third-party assets that support it.

### Low — External links and third-party dependency surface
The app loads Google Identity Services from Google's domain and makes requests to Google APIs. This is expected for optional sign-in/backup, but users should understand that these features communicate with Google.

**Recommended fix:** Keep dependencies minimal, review changes before updates, and test the no-Google mode separately.

### Feedback mailer notes
The Apps Script has a basic project-wide daily cap and a honeypot. The cap is not per-user and the endpoint is public if deployed as a public Web App. Confirm the script's recipient and deployment access before enabling it. Never expose OAuth credentials or app secrets in the public repository.

## APK wrapper security

The Android wrapper in this repository loads the HTTPS-hosted website in a WebView and intentionally exposes **no JavaScript-to-native bridge**. It enables JavaScript because the web app requires it, disables file/content access and mixed content, enables Safe Browsing where supported, and sends off-site navigation to the external browser.

The wrapper is a network-dependent shell, not a fully offline native conversion. Google may block OAuth sign-in inside embedded WebViews; test Google sign-in on the actual APK. If it is blocked, use a supported browser-based OAuth flow or a properly configured Trusted Web Activity with domain verification rather than weakening WebView security.

## Before public release

- [ ] Review every `innerHTML` assignment and escape or replace unsafe ones.
- [ ] Test Google OAuth and Drive backup on the production domain.
- [ ] Configure the feedback endpoint or remove/label the unavailable submission feature.
- [ ] Add a tested CSP.
- [ ] Test on Android: launch, offline/network errors, back navigation, external links, sign-in, and storage persistence.
- [ ] Keep the APK unsigned for local testing or configure a secure release-signing process using GitHub Actions secrets. Never commit keystores or passwords.
