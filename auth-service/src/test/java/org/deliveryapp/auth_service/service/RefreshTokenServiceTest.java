package org.deliveryapp.auth_service.service;

import org.deliveryapp.auth_service.exception.TokenRefreshException;
import org.deliveryapp.auth_service.model.RefreshToken;
import org.deliveryapp.auth_service.model.User;

import org.deliveryapp.auth_service.repository.IRefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService Unit Tests")
class RefreshTokenServiceTest {

    @Mock
    private IRefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        // Inject @Value field since Mockito doesn't process @Value
        ReflectionTestUtils.setField(refreshTokenService,
                "refreshTokenExpirationMs", 604800000L);

        sampleUser = User.builder()
                .id(1L)
                .name("Juan Test")
                .email("juan@test.com")
                .role(org.deliveryapp.model.enums.Role.ROLE_CUSTOMER)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("createRefreshToken: should delete old token and create a new one")
    void createRefreshToken_shouldRotateToken() {
        // Given
        RefreshToken newToken = RefreshToken.builder()
                .id(1L)
                .user(sampleUser)
                .token("new-uuid")
                .expiryDate(Instant.now().plusSeconds(604800))
                .build();

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(newToken);

        // When
        RefreshToken result = refreshTokenService.createRefreshToken(sampleUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("new-uuid");

        // Verify old token was deleted first (token rotation)
        verify(refreshTokenRepository, times(1)).deleteByUser(sampleUser);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("validateRefreshToken: should return token when valid and not expired")
    void validateRefreshToken_shouldReturnToken_whenValid() {
        // Given
        RefreshToken validToken = RefreshToken.builder()
                .id(1L)
                .user(sampleUser)
                .token("valid-token")
                .expiryDate(Instant.now().plusSeconds(3600)) // expires in 1 hour
                .build();

        when(refreshTokenRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(validToken));

        // When
        RefreshToken result = refreshTokenService.validateRefreshToken("valid-token");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("valid-token");
    }

    @Test
    @DisplayName("validateRefreshToken: should throw and delete when token is expired")
    void validateRefreshToken_shouldThrowAndDelete_whenExpired() {
        // Given
        RefreshToken expiredToken = RefreshToken.builder()
                .id(1L)
                .user(sampleUser)
                .token("expired-token")
                .expiryDate(Instant.now().minusSeconds(3600)) // expired 1 hour ago
                .build();

        when(refreshTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(expiredToken));

        // When / Then
        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("expired-token"))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("expired");

        // Expired token must be deleted from DB
        verify(refreshTokenRepository, times(1)).delete(expiredToken);
    }

    @Test
    @DisplayName("validateRefreshToken: should throw when token not found")
    void validateRefreshToken_shouldThrow_whenTokenNotFound() {
        // Given
        when(refreshTokenRepository.findByToken("unknown-token"))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("unknown-token"))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("not found");
    }
}