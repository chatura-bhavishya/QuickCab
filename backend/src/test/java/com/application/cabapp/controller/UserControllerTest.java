package com.application.cabapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.application.cabapp.config.SecurityConfig;
import com.application.cabapp.dto.request.LoginRequest;
import com.application.cabapp.dto.response.AuthResponse;
import com.application.cabapp.dto.response.UserResponse;
import com.application.cabapp.exception.ApiException;
import com.application.cabapp.security.AuthPrincipal;
import com.application.cabapp.security.JwtService;
import com.application.cabapp.security.Role;
import com.application.cabapp.service.UserService;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtService.class})
class UserControllerTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @MockBean UserService userService;

    @Test
    void loginIsPublicAndReturnsToken() throws Exception {
        when(userService.login(any(LoginRequest.class))).thenReturn(
                new AuthResponse<>("jwt", 3600, new UserResponse(1L, "rider1", "r@example.com", "9876543210")));

        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"rider1\",\"password\":\"secret12\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt"))
                .andExpect(jsonPath("$.account.username").value("rider1"))
                .andExpect(jsonPath("$.account.password").doesNotExist());
    }

    @Test
    void wrongCredentialsReturn401() throws Exception {
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(ApiException.unauthorized("Invalid username or password"));

        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"rider1\",\"password\":\"bad\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    @Test
    void registrationValidatesInput() throws Exception {
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a b\",\"email\":\"nope\",\"password\":\"123\",\"contact\":\"12\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").value("Enter a valid email address"))
                .andExpect(jsonPath("$.fieldErrors.contact").value("Enter a valid contact number"))
                .andExpect(jsonPath("$.fieldErrors.password").exists())
                .andExpect(jsonPath("$.fieldErrors.username").exists());
        verify(userService, never()).register(any());
    }

    @Test
    void profileNeedsAToken() throws Exception {
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void profileUsesTheTokenSubject() throws Exception {
        AuthPrincipal rider = new AuthPrincipal(7L, Role.USER);
        when(userService.getCurrent(rider)).thenReturn(new UserResponse(7L, "rider7", "r7@example.com", "9876543210"));

        mvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.generateToken(rider)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }
}
