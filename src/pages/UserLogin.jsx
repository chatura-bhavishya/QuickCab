import LoginForm from '../components/LoginForm';
import { ROLES } from '../constants';

export default function UserLogin() {
  return (
    <LoginForm role={ROLES.USER} title="User Login" redirectTo="/book"
      registerPrompt="New here?" registerPath="/user/register" />
  );
}
