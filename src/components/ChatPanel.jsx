import { useId, useState } from 'react';
import { LIMITS } from '../constants';
import { getErrorMessage } from '../utils/errors';
import { validateChatMessage } from '../utils/validation';
import FormMessage from './FormMessage';

/**
 * Chat card shared by the rider and driver screens.
 * `ownSender` ('USER' or 'DRIVER') decides which bubbles are "mine".
 * `onSend(text)` must return a promise; it rejects if the message failed.
 */
export default function ChatPanel({ title, messages, ownSender, onSend }) {
  const [draft, setDraft] = useState('');
  const [error, setError] = useState('');
  const [sending, setSending] = useState(false);
  const inputId = useId();

  const send = async () => {
    setError('');
    if (!draft.trim()) return;
    const validationError = validateChatMessage(draft);
    if (validationError) { setError(validationError); return; }
    setSending(true);
    try {
      await onSend(draft);
      setDraft('');
    } catch (e) {
      setError(getErrorMessage(e, 'Message could not be sent.'));
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="card">
      <h3>{title}</h3>
      <div className="chat-box" role="log" aria-live="polite" aria-label={title}>
        {messages.map(c => (
          <div key={c.id} className={`chat-msg ${c.sender === ownSender ? 'me' : 'them'}`}>
            {c.message}
          </div>
        ))}
        {messages.length === 0 && <p className="muted">No messages yet.</p>}
      </div>
      <div className="chat-input">
        <label htmlFor={inputId} className="sr-only">Message</label>
        <input id={inputId} value={draft} onChange={e => setDraft(e.target.value)}
          placeholder="Type a message..." maxLength={LIMITS.CHAT_MESSAGE_MAX} />
        <button className="btn" onClick={send} disabled={sending}>Send</button>
      </div>
      <FormMessage error={error} />
    </div>
  );
}
