package org.deliveryapp.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.deliveryapp.auth_service.dto.request.LoginRequestDTO;
import org.deliveryapp.auth_service.dto.request.RefreshTokenRequestDTO;
import org.deliveryapp.auth_service.dto.request.RegisterRequestDTO;
import org.deliveryapp.auth_service.dto.response.AuthResponseDTO;
import org.deliveryapp.auth_service.dto.response.TokenRefreshResponseDTO;
import org.deliveryapp.auth_service.dto.response.UserResponseDTO;
import org.deliveryapp.auth_service.exception.UserAlreadyExistsException;
import org.deliveryapp.auth_service.model.enums.Role;
import org.deliveryapp.auth_service.security.JwtAuthenticationFilter;
import org.deliveryapp.auth_service.security.JwtTokenProvider;
import org.deliveryapp.auth_service.security.UserDetailsServiceImpl;
import org.deliveryapp.auth_service.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AuthController.class)
@Import(AuthController.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;


    // ═══════════════════════════════════════════
    //  POST /api/v1/auth/register
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("register: should return 201 with tokens when request is valid")
    void register_shouldReturn201_whenRequestIsValid() throws Exception {
        // Given
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Juan Test", "juan@test.com", "password123", Role.ROLE_CUSTOMER);

        UserResponseDTO userResponse = UserResponseDTO.builder()
                .id(1L)
                .name("Juan Test")
                .email("juan@test.com")
                .role(Role.ROLE_CUSTOMER)
                .build();

        AuthResponseDTO authResponse = AuthResponseDTO.of(
                "access-token", "refresh-token-uuid", userResponse);

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(authResponse);

        // When / Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-uuid"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("juan@test.com"));

        verify(authService, times(1)).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("register: should return 409 when email already exists")
    void register_shouldReturn409_whenEmailAlreadyExists() throws Exception {
        // Given
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Juan Test", "juan@test.com", "password123", Role.ROLE_CUSTOMER);

        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new UserAlreadyExistsException("juan@test.com"));

        // When / Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("register: should return 400 when required fields are missing")
    void register_shouldReturn400_whenFieldsAreMissing() throws Exception {
        // Given - request with no email
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Juan Test", "", "password123", Role.ROLE_CUSTOMER);

        // When / Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify service was never called — validation blocked it
        verify(authService, never()).register(any());
    }

    // ═══════════════════════════════════════════
    //  POST /api/v1/auth/login
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("login: should return 200 with tokens when credentials are valid")
    void login_shouldReturn200_whenCredentialsAreValid() throws Exception {
        // Given
        LoginRequestDTO request = new LoginRequestDTO("juan@test.com", "password123");

        UserResponseDTO userResponse = UserResponseDTO.builder()
                .id(1L)
                .name("Juan Test")
                .email("juan@test.com")
                .role(Role.ROLE_CUSTOMER)
                .build();

        AuthResponseDTO authResponse = AuthResponseDTO.of(
                "access-token", "refresh-token-uuid", userResponse);

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(authResponse);

        // When / Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.user.email").value("juan@test.com"));
    }

    @Test
    @DisplayName("login: should return 400 when email is missing")
    void login_shouldReturn400_whenEmailIsMissing() throws Exception {
        // Given - missing email
        LoginRequestDTO request = new LoginRequestDTO("", "password123");

        // When / Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    // ═══════════════════════════════════════════
    //  POST /api/v1/auth/refresh
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("refresh: should return 200 with new tokens")
    void refresh_shouldReturn200_withNewTokens() throws Exception {
        // Given
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("old-refresh-token");

        TokenRefreshResponseDTO response = TokenRefreshResponseDTO.of(
                "new-access-token", "new-refresh-token");

        when(authService.refreshToken(any(RefreshTokenRequestDTO.class))).thenReturn(response);

        // When / Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    // ═══════════════════════════════════════════
    //  POST /api/v1/auth/logout
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("logout: should return 204 when refresh token is valid")
    void logout_shouldReturn204() throws Exception {
        // Given
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("refresh-token-uuid");

        doNothing().when(authService).logout(anyString());

        // When / Then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authService, times(1)).logout("refresh-token-uuid");
    }
}