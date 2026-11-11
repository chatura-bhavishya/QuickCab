import { useState } from 'react';
import { RATING_STARS } from '../../constants';
import { getErrorMessage } from '../../utils/errors';

/** Star rating for a completed ride. `onSubmit(stars)` returns a promise. */
export default function RatingCard({ onSubmit }) {
  const [rating, setRating] = useState(0);
  const [error, setError] = useState('');

  const submit = async () => {
    setError('');
    if (!rating) return;
    try {
      await onSubmit(rating);
    } catch (e) {
      setError(getErrorMessage(e, 'Could not submit rating. Please try again.'));
    }
  };

  return (
    <div className="card">
      <h3>Rate Your Driver</h3>
      <div>
        {RATING_STARS.map(s => (
          <span key={s} className="stars"
            style={{ opacity: s <= rating ? 1 : 0.3 }}
            onClick={() => setRating(s)}>★</span>
        ))}
      </div>
      <button className="btn" style={{ marginTop: 10 }} onClick={submit}>Submit Rating</button>
      {error && <div className="error">{error}</div>}
    </div>
  );
}
