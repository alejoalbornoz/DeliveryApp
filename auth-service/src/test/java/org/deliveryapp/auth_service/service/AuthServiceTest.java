package org.deliveryapp.auth_service.service;


import org.deliveryapp.auth_service.dto.request.LoginRequestDTO;
import org.deliveryapp.auth_service.dto.request.RegisterRequestDTO;
import org.deliveryapp.auth_service.dto.response.AuthResponseDTO;
import org.deliveryapp.auth_service.exception.UserAlreadyExistsException;
import org.deliveryapp.auth_service.model.RefreshToken;
import org.deliveryapp.auth_service.model.User;


import org.deliveryapp.auth_service.model.enums.Role;
import org.deliveryapp.auth_service.repository.IUserRepository;
import org.deliveryapp.auth_service.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private RefreshToken sampleRefreshToken;
    private UserDetails sampleUserDetails;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .name("Juan Test")
                .email("juan@test.com")
                .password("hashedPassword")
                .role(Role.ROLE_CUSTOMER)
                .enabled(true)
                .build();

        sampleRefreshToken = RefreshToken.builder()
                .id(1L)
                .user(sampleUser)
                .token("refresh-token-uuid")
                .expiryDate(Instant.now().plusSeconds(604800))
                .build();

        sampleUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username("juan@test.com")
                .password("hashedPassword")
                .authorities(List.of())
                .build();
    }

    // ═══════════════════════════════════════════
    //  register()
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("register: should create user and return tokens when email is new")
    void register_shouldReturnTokens_whenEmailIsNew() {
        // Given
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Juan Test", "juan@test.com", "password123", Role.ROLE_CUSTOMER);

        when(userRepository.existsByEmail("juan@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(userDetailsService.loadUserByUsername("juan@test.com")).thenReturn(sampleUserDetails);
        when(jwtTokenProvider.generateAccessToken(sampleUserDetails)).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(sampleUser)).thenReturn(sampleRefreshToken);

        // When
        AuthResponseDTO response = authService.register(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-uuid");
        assertThat(response.getUser().getEmail()).isEqualTo("juan@test.com");

        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    @DisplayName("register: should throw UserAlreadyExistsException when email is taken")
    void register_shouldThrow_whenEmailAlreadyExists() {
        // Given
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Juan Test", "juan@test.com", "password123", Role.ROLE_CUSTOMER);

        when(userRepository.existsByEmail("juan@test.com")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("juan@test.com");

        // Verify save was never called
        verify(userRepository, never()).save(any(User.class));
    }

    // ═══════════════════════════════════════════
    //  login()
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("login: should return tokens when credentials are valid")
    void login_shouldReturnTokens_whenCredentialsAreValid() {
        // Given
        LoginRequestDTO request = new LoginRequestDTO("juan@test.com", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // authenticate() returns Authentication, null is fine for this test
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(sampleUser));
        when(userDetailsService.loadUserByUsername("juan@test.com")).thenReturn(sampleUserDetails);
        when(jwtTokenProvider.generateAccessToken(sampleUserDetails)).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(sampleUser)).thenReturn(sampleRefreshToken);

        // When
        AuthResponseDTO response = authService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");

        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    @DisplayName("login: should call AuthenticationManager which throws on bad credentials")
    void login_shouldPropagate_whenAuthenticationFails() {
        // Given
        LoginRequestDTO request = new LoginRequestDTO("juan@test.com", "wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        // When / Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class);

        verify(userRepository, never()).findByEmail(anyString());
    }

    // ═══════════════════════════════════════════
    //  logout()
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("logout: should delete refresh token for the user")
    void logout_shouldDeleteRefreshToken() {
        // Given
        when(refreshTokenService.validateRefreshToken("refresh-token-uuid"))
                .thenReturn(sampleRefreshToken);

        // When
        authService.logout("refresh-token-uuid");

        // Then
        verify(refreshTokenService, times(1)).deleteByUser(sampleUser);
    }
}