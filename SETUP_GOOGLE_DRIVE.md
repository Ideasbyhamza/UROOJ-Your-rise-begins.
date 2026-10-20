# UROOJ — Google Sign-in & Google Drive setup

UROOJ includes a browser-based Google OAuth flow and can back up app state to a JSON file named `urooj-data.json` in the signed-in user's Google Drive. **This setup is not complete until you configure your own Google OAuth Client ID and test the consent flow.** The repository does not contain an OAuth client secret.

## 1. Create/configure a Google Cloud project

1. Open [Google Cloud Credentials](https://console.cloud.google.com/apis/credentials) and choose or create a project.
2. Configure the OAuth consent screen / Google Auth Platform branding, audience, and contact information.
3. Enable the **Google Drive API** for that project.
4. Create an OAuth client with application type **Web application**.
5. Add your hosted site's *origin* under **Authorized JavaScript origins**. For GitHub Pages, an origin normally looks like `https://OWNER.github.io` (scheme + hostname only; no repository path). Use the exact hostname shown in your browser after opening the live UROOJ site.
6. If Google Auth Platform puts your app in Testing, add the Google accounts you will use as test users. Follow Google's publishing/verification requirements before inviting other users.

Official guides:
- [Create a Google API client ID](https://developers.google.com/identity/gsi/web/guides/get-google-api-clientid)
- [Google Drive API JavaScript quickstart](https://developers.google.com/workspace/drive/api/quickstart/js)
- [Drive API OAuth scopes](https://developers.google.com/workspace/drive/api/guides/api-specific-auth)

## 2. Save the Client ID in UROOJ

1. Open the hosted UROOJ website.
2. On the login screen, choose **Continue without Google**.
3. Open **Settings → Google Account**.
4. Paste the OAuth **Client ID**. It should end in `.apps.googleusercontent.com`. Save it, then press **Connect Google** (or return to the login screen and choose **Sign in with Google**).
5. Review the Google permission screen and approve the requested access.

A Client ID identifies the browser app and is not a client secret. **Never put a client secret, service-account key, or private API credential into this HTML file.** With this setup, the Client ID is saved in browser storage; you may need to enter it once per device until it is embedded in the public app configuration.

## 3. How sync behaves

- UROOJ asks Google for the `drive.file` scope and basic account identity. This is intended to limit the app to files it creates or files explicitly opened/shared with it.
- The app stores its backup in a JSON file named `urooj-data.json` in the signed-in account's Drive.
- When you make changes while signed in, UROOJ schedules a Drive sync. You can also use **Sync now** in Settings.
- When signing into Google on another device, UROOJ validates the Drive backup before restoring it. If the local app is empty/reset but Drive contains meaningful saved progress, the Drive copy is restored automatically. A meaningful local state will not overwrite an empty Drive backup. If both copies contain different meaningful progress, UROOJ asks which copy to keep. Empty/reset local state is not allowed to overwrite meaningful cloud progress during automatic sync.
- **Full reset:** while Google is connected, the in-app Full reset attempts to delete the `urooj-data.json` Drive backup before clearing local data. If a previous sync is recorded but you are signed out, reconnect the same Google account first. If Drive deletion fails, the reset is cancelled and local data is kept. Manually clearing browser storage or signing out does not delete the Drive file.
- **Google Drive backup is not end-to-end encrypted by this app.** Your UROOJ data may include private journal/reflection text. Review the app's requested scopes and Google account carefully. Sign out on shared devices.

## 4. Important limitations

- This is a static, client-side app: it does not have a secure server-side user database or server session. Google sign-in grants access to Drive; it is not a substitute for server-enforced authorization.
- Data remains in browser storage when offline or before Google sign-in is configured.
- A GitHub Pages deployment does not automatically create a Google OAuth client or enable the Drive API.
- Test on the actual HTTPS Pages URL, not by opening the HTML from a local file.

## 5. Connect Feedback & Complaints to Gmail

The feedback form is prepared to email submissions to **hamzamadnipersonal@gmail.com** using Google Apps Script. The web app must be deployed once from your Google account; GitHub Pages cannot authorize Gmail sending on your behalf.

1. Open [Google Apps Script](https://script.google.com/) while signed in to **hamzamadnipersonal@gmail.com** and create a new project named **UROOJ Feedback Mail**.
2. In this repository, open [`feedback/Code.gs`](https://github.com/Ideasbyhamza/UROOJ-Your-rise-begins./blob/main/feedback/Code.gs), copy the complete code, and replace the contents of the Apps Script editor with it.
3. Click **Save**. Select **Deploy → New deployment**.
4. Click the type/gear control, choose **Web app**, set **Execute as** to **Me**, and set access to **Anyone** so app visitors can submit feedback without signing into your Google account. Deploy.
5. Google will ask you to authorize the script to send email. Review and approve it. Copy the deployed Web app URL; it must end in `/exec`.
6. Send that `/exec` URL to the UROOJ maintainer so it can be connected to the app's `feedbackEndpoint` and published. Until the URL is connected, the app will save feedback locally and offer an email-draft link instead of automatically sending it.
7. Test by submitting a harmless test message and confirm it arrives in the Gmail inbox (also check Spam).

The endpoint sends plain-text messages to the fixed recipient above, validates the category and message length, includes a hidden honeypot field, and limits sending to 30 messages per day. Because it is publicly accessible, these are basic safeguards rather than strong anti-spam protection. Do not use it to collect passwords or sensitive personal/medical information. The browser cannot independently confirm delivery, so verify the test email in Gmail.
