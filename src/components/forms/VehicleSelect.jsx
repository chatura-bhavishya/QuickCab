import { VEHICLES } from '../../constants';

/** <select> of the supported vehicle types. */
export default function VehicleSelect(props) {
  return (
    <select {...props}>
      {VEHICLES.map(v => <option key={v.id} value={v.id}>{v.label}</option>)}
    </select>
  );
}
