import { VEHICLES } from '../../constants';
import { onActivateKey } from '../../utils/keyboard';

/** Clickable vehicle cards used on the booking screen (a keyboard-accessible radio group). */
export default function VehiclePicker({ value, onChange, labelledBy }) {
  return (
    <div className="vehicles" role="radiogroup" aria-labelledby={labelledBy}
      style={{ marginTop: 8, marginBottom: 16 }}>
      {VEHICLES.map(v => (
        <div key={v.id}
          role="radio"
          aria-checked={value === v.id}
          tabIndex={0}
          className={`vehicle-card ${value === v.id ? 'selected' : ''}`}
          onClick={() => onChange(v.id)}
          onKeyDown={onActivateKey(() => onChange(v.id))}>
          <div>{v.label}</div>
        </div>
      ))}
    </div>
  );
}
