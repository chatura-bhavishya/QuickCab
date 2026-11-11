/** The assigned driver's details, shown to the rider. */
export default function DriverDetailsCard({ driver }) {
  return (
    <div className="card">
      <h3>Driver Details</h3>
      <p><b>Name:</b> {driver.username}</p>
      <p><b>Contact:</b> {driver.contact}</p>
      <p><b>Vehicle:</b> {driver.vehicleType} — {driver.vehicleNumber}</p>
      <p><b>Rating:</b> {driver.rating} ({driver.ratingCount} ratings)</p>
    </div>
  );
}
