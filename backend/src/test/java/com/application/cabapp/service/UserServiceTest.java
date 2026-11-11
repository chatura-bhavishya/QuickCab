package com.application.cabapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.application.cabapp.dto.request.LoginRequest;
import com.application.cabapp.dto.request.UserRegisterRequest;
import com.application.cabapp.dto.request.UserUpdateRequest;
import com.application.cabapp.exception.ApiException;
import com.application.cabapp.model.User;
import com.application.cabapp.repository.UserRepository;
import com.application.cabapp.security.AuthPrincipal;
import com.application.cabapp.security.JwtService;
import com.application.cabapp.security.PasswordService;
import com.application.cabapp.security.Role;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock JwtService jwtService;
    @Spy PasswordService passwordService = new PasswordService();
    @InjectMocks UserService userService;

    private static User storedUser(String password) {
        User u = new User();
        u.setId(7L);
        u.setUsername("rider1");
        u.setEmail("rider@example.com");
        u.setContact("9876543210");
        u.setPassword(password);
        return u;
    }

    @Test
    void registerHashesThePassword() {
        when(userRepository.existsByUsername("rider1")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.register(new UserRegisterRequest("rider1", "rider@example.com", "secret12", "9876543210"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getPassword()).isNotEqualTo("secret12");
        assertThat(passwordService.matches("secret12", saved.getValue().getPassword())).isTrue();
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(userRepository.existsByUsername("rider1")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(
                new UserRegisterRequest("rider1", "rider@example.com", "secret12", "9876543210")))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT);
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginReturnsTokenForCorrectPassword() {
        User existing = storedUser(passwordService.hash("secret12"));
        when(userRepository.findByUsername("rider1")).thenReturn(Optional.of(existing));
        when(jwtService.generateToken(new AuthPrincipal(7L, Role.USER))).thenReturn("signed-token");

        var response = userService.login(new LoginRequest("rider1", "secret12"));

        assertThat(response.token()).isEqualTo("signed-token");
        assertThat(response.account().username()).isEqualTo("rider1");
    }

    @Test
    void loginRejectsWrongPasswordAndUnknownUser() {
        User existing = storedUser(passwordService.hash("secret12"));
        when(userRepository.findByUsername("rider1")).thenReturn(Optional.of(existing));
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(new LoginRequest("rider1", "nope")))
                .isInstanceOf(ApiException.class).hasMessage("Invalid username or password");
        assertThatThrownBy(() -> userService.login(new LoginRequest("ghost", "secret12")))
                .isInstanceOf(ApiException.class).hasMessage("Invalid username or password");
    }

    @Test
    void loginUpgradesLegacyPlaintextPassword() {
        User legacy = storedUser("old-plain");
        when(userRepository.findByUsername("rider1")).thenReturn(Optional.of(legacy));

        userService.login(new LoginRequest("rider1", "old-plain"));

        assertThat(legacy.getPassword()).startsWith("$2");
        assertThat(passwordService.matches("old-plain", legacy.getPassword())).isTrue();
    }

    @Test
    void driverTokenCannotUseRiderProfileEndpoints() {
        AuthPrincipal driver = new AuthPrincipal(7L, Role.DRIVER);

        assertThatThrownBy(() -> userService.getCurrent(driver))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void updateKeepsPasswordWhenBlank() {
        User user = storedUser("$2a$10$existinghashexistinghashexistinghashexistinghashexist");
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        userService.updateCurrent(new AuthPrincipal(7L, Role.USER),
                new UserUpdateRequest("new@example.com", "9876500000", ""));

        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getPassword()).startsWith("$2a$10$existinghash");
    }
}
