package org.deliveryapp.auth_service.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private UserResponseDTO user;

    // Convenience factory so callers don't have to set tokenType manually
    public static AuthResponseDTO of(String accessToken, String refreshToken, UserResponseDTO user) {
        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(user)
                .build();
    }
}
