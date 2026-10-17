/**
 * UROOJ feedback mailer — deploy as a Google Apps Script Web App.
 * The script sends feedback to the owner's inbox; it does not expose Gmail credentials.
 */
const FEEDBACK_RECIPIENT = 'hamzamadnipersonal@gmail.com';
const MAX_TEXT_LENGTH = 1500;
const MAX_REQUESTS_PER_DAY = 100;

function doPost(e) {
  try {
    const p = (e && e.parameter) || {};
    // Quietly accept honeypot submissions without sending mail.
    if (String(p.website || '').trim()) return json_({ ok: true });

    const type = String(p.type || 'Other').trim().slice(0, 60);
    const message = String(p.message || '').trim().slice(0, MAX_TEXT_LENGTH);
    const date = String(p.date || '').trim().slice(0, 40);

    if (!message) return json_({ ok: false, error: 'Feedback is empty.' });
    if (message.length > MAX_TEXT_LENGTH) return json_({ ok: false, error: 'Feedback is too long.' });

    // Basic project-wide daily cap to help limit abuse of the public endpoint.
    const props = PropertiesService.getScriptProperties();
    const day = Utilities.formatDate(new Date(), 'Etc/UTC', 'yyyy-MM-dd');
    const key = 'feedback_count_' + day;
    const lock = LockService.getScriptLock();
    lock.waitLock(5000);
    let count = Number(props.getProperty(key) || 0);
    if (count >= MAX_REQUESTS_PER_DAY) {
      lock.releaseLock();
      return json_({ ok: false, error: 'Daily feedback limit reached.' });
    }
    props.setProperty(key, String(count + 1));
    lock.releaseLock();

    const safeType = type.replace(/[\\r\\n]+/g, ' ');
    const subject = '[UROOJ Feedback] ' + safeType;
    const body = [
      'New feedback received from UROOJ.',
      '',
      'Type: ' + safeType,
      'Date shown by app: ' + date,
      'Received at: ' + new Date().toISOString(),
      '',
      'Feedback:',
      message,
      '',
      'Note: submissions are user-provided text.'
    ].join('\\n');

    MailApp.sendEmail({
      to: FEEDBACK_RECIPIENT,
      subject: subject,
      body: body,
      name: 'UROOJ Feedback'
    });
    return json_({ ok: true });
  } catch (err) {
    return json_({ ok: false, error: 'Could not send feedback.' });
  }
}

function doGet() {
  return json_({ service: 'UROOJ feedback mailer', ready: true, method: 'POST' });
}

function json_(value) {
  return ContentService.createTextOutput(JSON.stringify(value))
    .setMimeType(ContentService.MimeType.JSON);
}
