import { VEHICLES } from '../../constants';

/** Clickable vehicle cards used on the booking screen. */
export default function VehiclePicker({ value, onChange }) {
  return (
    <div className="vehicles" style={{ marginTop: 8, marginBottom: 16 }}>
      {VEHICLES.map(v => (
        <div key={v.id}
          className={`vehicle-card ${value === v.id ? 'selected' : ''}`}
          onClick={() => onChange(v.id)}>
          <div>{v.label}</div>
        </div>
      ))}
    </div>
  );
}
