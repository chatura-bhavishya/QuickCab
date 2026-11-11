/**
 * A labelled form control using the app's .form-group layout.
 * Renders an <input> by default; pass children to render another control.
 */
export default function FormField({ id, label, children, ...inputProps }) {
  return (
    <div className="form-group">
      <label htmlFor={id}>{label}</label>
      {children || <input id={id} {...inputProps} />}
    </div>
  );
}
