# UROOJ Security Review

**Review date:** 2026-10-20  
**Scope:** Static inspection of `index.html`, `UROOJ.html`, `privacy.html`, `terms.html`, `SETUP_GOOGLE_DRIVE.md`, and `google-apps-script-feedback.gs`. This is not a penetration test, dynamic test, or formal security certification.

## Summary

This is a **static code review, not a penetration test or security certification**. The app is a client-side web app with local browser storage and optional Google OAuth/Drive access. A scan of the current main page found no uses of `eval()`, `new Function()`, `document.write()`, or `insertAdjacentHTML()`; it found 38 `.innerHTML` assignments that still require contextual review. The Google OAuth client ID is public configuration, not a client secret. No access-token storage in localStorage was found by the targeted scan.

### Change made 2026-10-20 — Full reset and Drive backup

The main hosted page's **Full reset all app data** now attempts to delete the UROOJ Drive backup when Google is connected, before clearing local/session storage. It blocks reset when a previous successful sync is recorded but no Google token is active, asks the user to reconnect, and preserves local data if the Drive delete request fails. It also refuses to start while a Drive sync is in progress. This relies on the browser having a valid Google authorization and the Drive API being available; the deployed flow still needs real-account testing. Clearing browser data manually or signing out does not delete Drive data.

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

### Low/Medium — No explicit Content Security Policy or Subresource Integrity found — **Partially addressed 2026-10-19**
A `Content-Security-Policy` `<meta>` tag has been added to `index.html`, `UROOJ.html`, and their Android-wrapper asset copies:
`default-src 'self'; script-src 'self' 'unsafe-inline' https://accounts.google.com; style-src 'self' 'unsafe-inline'; img-src 'self' data: https://*.googleusercontent.com; font-src 'self' data:; connect-src 'self' https://www.googleapis.com https://accounts.google.com https://script.google.com; frame-src https://accounts.google.com; base-uri 'self'; form-action 'self'; object-src 'none';`

This restricts `connect-src`, `img-src`, `frame-src`, `base-uri`, `form-action`, and `object-src` to only the origins the app actually needs (Google Identity Services, Google APIs, and a deployed Apps Script feedback endpoint), which blocks a successful XSS payload from exfiltrating data to an attacker-controlled domain, loading an attacker script from an external host, or hijacking forms/frames on the page.

**Residual gap:** `script-src` and `style-src` still include `'unsafe-inline'`, because the app is a single static HTML file with all JavaScript and CSS inline and no build/template step to generate per-request nonces (GitHub Pages serves static files only). This means a successful DOM-based XSS injection point (see the finding above) could still execute inline `<script>` content. Closing this fully requires extracting the inline `<script>`/`<style>` blocks into external `.js`/`.css` files and switching `script-src`/`style-src` to `'self'` only (no `'unsafe-inline'`) — a larger refactor not done in this pass. `frame-ancestors` and `report-uri`/`report-to` are not included because browsers ignore them when a CSP is delivered via `<meta>` rather than an HTTP header; GitHub Pages does not support custom response headers for this repo's deployment.

**Recommended follow-up:** Extract inline scripts/styles to external files and drop `'unsafe-inline'` once that refactor is done. If moving to a host that supports custom headers, deliver the CSP as a real `Content-Security-Policy` header and add `frame-ancestors 'none'`.

### Low — External links and third-party dependency surface
The app loads Google Identity Services from Google's domain and makes requests to Google APIs. This is expected for optional sign-in/backup, but users should understand that these features communicate with Google.

**Recommended fix:** Keep dependencies minimal, review changes before updates, and test the no-Google mode separately.

### Feedback mailer notes
The Apps Script has a basic project-wide daily cap and a honeypot. The cap is not per-user and the endpoint is public if deployed as a public Web App. Confirm the script's recipient and deployment access before enabling it. Never expose OAuth credentials or app secrets in the public repository.

## APK wrapper security

The Android wrapper in this repository loads the HTTPS-hosted website in a WebView and intentionally exposes **no JavaScript-to-native bridge**. It enables JavaScript because the web app requires it, disables file/content access and mixed content, enables Safe Browsing where supported, and sends off-site navigation to the external browser.

The wrapper is a network-dependent shell, not a fully offline native conversion. Google may block OAuth sign-in inside embedded WebViews; test Google sign-in on the actual APK. If it is blocked, use a supported browser-based OAuth flow or a properly configured Trusted Web Activity with domain verification rather than weakening WebView security.

## Before public release

- [ ] Review every `innerHTML` assignment and escape or replace unsafe ones (38 assignments remain; targeted static scan is not proof that all are safe).
- [ ] Test Google OAuth, Drive backup/restore, and Full reset cloud deletion on the production domain with a real test account.
- [ ] Configure the feedback endpoint or remove/label the unavailable submission feature.
- [ ] Add a tested CSP.
- [ ] Test on Android: launch, offline/network errors, back navigation, external links, sign-in, and storage persistence.
- [ ] Keep the APK unsigned for local testing or configure a secure release-signing process using GitHub Actions secrets. Never commit keystores or passwords.
