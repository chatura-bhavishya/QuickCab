// Axios instance shared by all services. Attaches the JWT to every request
// and logs the user out if the server says the token is no longer valid.
import axios from 'axios';
import { LOGIN_PATHS } from '../constants';
import { clearSession, getSession } from './session';

const DEFAULT_API_BASE_URL = 'http://localhost:8080/api';
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || DEFAULT_API_BASE_URL;
const HTTP_UNAUTHORIZED = 401;

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' }
});

apiClient.interceptors.request.use(config => {
  const token = getSession()?.token;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

apiClient.interceptors.response.use(
  response => response,
  error => {
    const isLoginAttempt = error.config?.url?.endsWith('/login');
    const session = getSession();
    // Expired/invalid token: end the session and send the person to log in again.
    if (error.response?.status === HTTP_UNAUTHORIZED && !isLoginAttempt && session) {
      clearSession();
      window.location.assign(LOGIN_PATHS[session.role] || '/');
    }
    return Promise.reject(error);
  }
);

export default apiClient;
