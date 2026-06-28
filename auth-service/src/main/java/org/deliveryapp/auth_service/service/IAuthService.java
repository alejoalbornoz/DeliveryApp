package org.deliveryapp.service;

import org.deliveryapp.dto.request.LoginRequestDTO;
import org.deliveryapp.dto.request.RefreshTokenRequestDTO;
import org.deliveryapp.dto.request.RegisterRequestDTO;
import org.deliveryapp.dto.response.AuthResponseDTO;
import org.deliveryapp.dto.response.TokenRefreshResponseDTO;

public interface IAuthService {

    AuthResponseDTO register(RegisterRequestDTO request);

    AuthResponseDTO login (LoginRequestDTO request);

    TokenRefreshResponseDTO refreshToken(RefreshTokenRequestDTO request);

    void logout(String refreshToken);

}
