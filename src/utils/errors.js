// Converts an API error into a short message that is safe to show users.
// Only plain-string bodies and { error } objects from our own API are shown;
// anything else (HTML pages, stack traces, framework error objects) falls back.
const MAX_MESSAGE_LENGTH = 200;

export function getErrorMessage(error, fallback) {
  const data = error?.response?.data;
  const message = typeof data === 'string' ? data : data?.error;
  if (typeof message === 'string' && message.trim() && message.length <= MAX_MESSAGE_LENGTH && !message.includes('<')) {
    return message;
  }
  return fallback;
}
