/**
 * UROOJ Feedback & Complaints mail handler.
 * Deploy as a Web App that executes as the owner and is accessible to anyone.
 *
 * Security note: this is a public endpoint. It validates input, uses a honeypot,
 * and applies a small global daily send limit, but it is not a substitute for CAPTCHA.
 */
const FEEDBACK_RECIPIENT = 'hamzamadnipersonal@gmail.com';
const DAILY_SEND_LIMIT = 30;
const COUNTER_KEY = 'urooj_feedback_daily_counter';

function doPost(e) {
  try {
    const p = (e && e.parameter) || {};
    // Hidden honeypot field used by the UROOJ form.
    if (String(p.website || '').trim()) {
      return json_({ ok: true });
    }

    const allowedTypes = ['Suggestion', 'Complaint', 'Bug report / Glitch', 'Feature request', 'Other'];
    const type = String(p.type || '').trim();
    const message = String(p.message || '').trim();
    const date = String(p.date || '').trim();

    if (!allowedTypes.includes(type) || message.length < 3 || message.length > 5000) {
      return json_({ ok: false, error: 'Invalid feedback submission.' });
    }

    // Avoid an accidentally unbounded mail-sending endpoint.
    const lock = LockService.getScriptLock();
    lock.waitLock(5000);
    try {
      const props = PropertiesService.getScriptProperties();
      const today = Utilities.formatDate(new Date(), 'Asia/Kolkata', 'yyyy-MM-dd');
      let counter = {};
      try { counter = JSON.parse(props.getProperty(COUNTER_KEY) || '{}'); } catch (_) {}
      if (counter.date !== today) counter = { date: today, count: 0 };
      if (Number(counter.count || 0) >= DAILY_SEND_LIMIT) {
        return json_({ ok: false, error: 'Daily feedback limit reached. Please try again tomorrow.' });
      }
      counter.count = Number(counter.count || 0) + 1;
      props.setProperty(COUNTER_KEY, JSON.stringify(counter));
    } finally {
      lock.releaseLock();
    }

    const subject = '[UROOJ ' + type + '] Feedback from app';
    const body = [
      'A new UROOJ feedback submission was received.',
      '',
      'Type: ' + type,
      'App date: ' + date.slice(0, 40),
      'Received: ' + new Date().toISOString(),
      '',
      'Message:',
      message
    ].join('\n');

    MailApp.sendEmail({
      to: FEEDBACK_RECIPIENT,
      subject: subject,
      body: body,
      name: 'UROOJ Feedback'
    });
    return json_({ ok: true });
  } catch (err) {
    console.error('UROOJ feedback mail failed: ' + err);
    return json_({ ok: false, error: 'Could not send feedback.' });
  }
}

function doGet() {
  return ContentService
    .createTextOutput('UROOJ feedback endpoint is running. Submit feedback from the app.')
    .setMimeType(ContentService.MimeType.TEXT);
}

function json_(value) {
  return ContentService
    .createTextOutput(JSON.stringify(value))
    .setMimeType(ContentService.MimeType.JSON);
}
