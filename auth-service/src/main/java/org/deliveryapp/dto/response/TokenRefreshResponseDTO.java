package org.deliveryapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRefreshResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String tokenType;

    public static TokenRefreshResponseDTO of(String accessToken, String refreshToken) {
        return TokenRefreshResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }
}
