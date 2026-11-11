// Shared constants used across pages.

export const STORAGE_KEYS = {
  USER: 'user',
  DRIVER: 'driver'
};

export const VEHICLES = [
  { id: 'car', label: 'Car' },
  { id: 'bike', label: 'Bike' },
  { id: 'auto', label: 'Auto' }
];

export const DEFAULT_VEHICLE_TYPE = 'car';

/** How often ride status / chat / pending requests are refreshed. */
export const POLL_INTERVAL_MS = 3000;

/** Delay before redirecting to login after a successful registration. */
export const REDIRECT_DELAY_MS = 1200;

export const RATING_STARS = [1, 2, 3, 4, 5];

// Ride statuses (must match backend RideStatus.java).
export const RIDE_STATUS = {
  PENDING: 'PENDING',
  ACCEPTED: 'ACCEPTED',
  ON_THE_WAY: 'ON_THE_WAY',
  ARRIVED: 'ARRIVED',
  STARTED: 'STARTED',
  COMPLETED: 'COMPLETED',
  CANCELLED_BY_USER: 'CANCELLED_BY_USER',
  CANCELLED_BY_DRIVER: 'CANCELLED_BY_DRIVER'
};

// Input limits (must match backend InputValidator.java).
export const LIMITS = {
  USERNAME_MIN: 3,
  USERNAME_MAX: 30,
  PASSWORD_MIN: 6,
  PASSWORD_MAX: 72,
  EMAIL_MAX: 254,
  CONTACT_MAX: 20,
  VEHICLE_NUMBER_MAX: 20,
  LOCATION_MAX: 200,
  CHAT_MESSAGE_MAX: 255,
  OTP_LENGTH: 4
};
