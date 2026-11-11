import { useState } from 'react';
import { RATING_STARS } from '../../constants';
import { getErrorMessage } from '../../utils/errors';
import { onActivateKey } from '../../utils/keyboard';
import FormMessage from '../FormMessage';

/** Star rating for a completed ride. `onSubmit(stars)` returns a promise. */
export default function RatingCard({ onSubmit }) {
  const [rating, setRating] = useState(0);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const submit = async () => {
    setError('');
    if (!rating) { setError('Choose a rating first.'); return; }
    setSubmitting(true);
    try {
      await onSubmit(rating);
    } catch (e) {
      setError(getErrorMessage(e, 'Could not submit rating. Please try again.'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="card">
      <h3 id="rating-heading">Rate Your Driver</h3>
      <div role="radiogroup" aria-labelledby="rating-heading">
        {RATING_STARS.map(s => (
          <span key={s} className="stars"
            role="radio"
            aria-checked={s === rating}
            aria-label={`${s} star${s > 1 ? 's' : ''}`}
            tabIndex={0}
            style={{ opacity: s <= rating ? 1 : 0.3 }}
            onClick={() => setRating(s)}
            onKeyDown={onActivateKey(() => setRating(s))}>★</span>
        ))}
      </div>
      <button className="btn" style={{ marginTop: 10 }} onClick={submit} disabled={submitting}>
        {submitting ? 'Submitting...' : 'Submit Rating'}
      </button>
      <FormMessage error={error} />
    </div>
  );
}
