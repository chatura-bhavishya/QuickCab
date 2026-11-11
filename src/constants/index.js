// Shared constants used across pages.

export const STORAGE_KEYS = {
  SESSION: 'quickcab.session',
  // Keys used by older versions (no token); removed on startup.
  LEGACY: ['user', 'driver']
};

// Account roles (must match backend Role.java).
export const ROLES = {
  USER: 'USER',
  DRIVER: 'DRIVER'
};

export const LOGIN_PATHS = {
  [ROLES.USER]: '/user/login',
  [ROLES.DRIVER]: '/driver/login'
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

// Map defaults (OpenStreetMap tiles need the attribution shown on the map).
export const MAP = {
  DEFAULT_CENTER: [12.9716, 77.5946], // Bengaluru
  DEFAULT_ZOOM: 12,
  TILE_URL: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
  ATTRIBUTION: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
  PICKUP_COLOR: '#16a34a',
  DROP_COLOR: '#dc2626',
  ROUTE_COLOR: '#0f3d6b'
};

export const PIN_TARGETS = { PICKUP: 'pickup', DROP: 'drop' };

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

// Status groups used to decide what each screen shows.
export const OTP_VISIBLE_STATUSES = [RIDE_STATUS.ACCEPTED, RIDE_STATUS.ON_THE_WAY, RIDE_STATUS.ARRIVED];
export const RIDER_CANCELLABLE_STATUSES = [RIDE_STATUS.PENDING, ...OTP_VISIBLE_STATUSES];
export const DRIVER_CANCELLABLE_STATUSES = [RIDE_STATUS.ACCEPTED, RIDE_STATUS.ON_THE_WAY, RIDE_STATUS.ARRIVED];
export const DRIVER_ACTIVE_STATUSES = [...DRIVER_CANCELLABLE_STATUSES, RIDE_STATUS.STARTED];

// Input limits (must match backend ValidationRules.java).
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
