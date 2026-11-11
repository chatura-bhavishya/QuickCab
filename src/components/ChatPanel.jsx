import { useState } from 'react';
import { LIMITS } from '../constants';
import { getErrorMessage } from '../utils/errors';
import { validateChatMessage } from '../utils/validation';

/**
 * Chat card shared by the rider and driver screens.
 * `ownSender` ('USER' or 'DRIVER') decides which bubbles are "mine".
 * `onSend(text)` must return a promise; it rejects if the message failed.
 */
export default function ChatPanel({ title, messages, ownSender, onSend }) {
  const [draft, setDraft] = useState('');
  const [error, setError] = useState('');

  const send = async () => {
    setError('');
    if (!draft.trim()) return;
    const validationError = validateChatMessage(draft);
    if (validationError) { setError(validationError); return; }
    try {
      await onSend(draft);
      setDraft('');
    } catch (e) {
      setError(getErrorMessage(e, 'Message could not be sent.'));
    }
  };

  return (
    <div className="card">
      <h3>{title}</h3>
      <div className="chat-box">
        {messages.map(c => (
          <div key={c.id} className={`chat-msg ${c.sender === ownSender ? 'me' : 'them'}`}>
            {c.message}
          </div>
        ))}
        {messages.length === 0 && <p className="muted">No messages yet.</p>}
      </div>
      <div className="chat-input">
        <input value={draft} onChange={e => setDraft(e.target.value)} placeholder="Type a message..."
          maxLength={LIMITS.CHAT_MESSAGE_MAX} />
        <button className="btn" onClick={send}>Send</button>
      </div>
      {error && <div className="error">{error}</div>}
    </div>
  );
}
