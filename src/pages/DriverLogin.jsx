import LoginForm from '../components/LoginForm';
import { ROLES } from '../constants';

export default function DriverLogin() {
  return (
    <LoginForm role={ROLES.DRIVER} title="Driver Login" redirectTo="/driver/dashboard"
      registerPrompt="New driver?" registerPath="/driver/register" />
  );
}
