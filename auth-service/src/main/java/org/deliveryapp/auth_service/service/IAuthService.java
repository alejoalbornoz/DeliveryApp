package org.deliveryapp.auth_service.service;


import org.deliveryapp.auth_service.dto.request.LoginRequestDTO;
import org.deliveryapp.auth_service.dto.request.RefreshTokenRequestDTO;
import org.deliveryapp.auth_service.dto.request.RegisterRequestDTO;
import org.deliveryapp.auth_service.dto.response.AuthResponseDTO;
import org.deliveryapp.auth_service.dto.response.TokenRefreshResponseDTO;

public interface IAuthService {

    AuthResponseDTO register(RegisterRequestDTO request);

    AuthResponseDTO login (LoginRequestDTO request);

    TokenRefreshResponseDTO refreshToken(RefreshTokenRequestDTO request);

    void logout(String refreshToken);

}
