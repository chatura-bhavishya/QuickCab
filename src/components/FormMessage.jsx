/**
 * Error / success text under a form. Uses live regions so screen readers
 * announce the message when it appears.
 */
export default function FormMessage({ error, success }) {
  return (
    <>
      {error && <div className="error" role="alert">{error}</div>}
      {success && <div className="success" role="status">{success}</div>}
    </>
  );
}
