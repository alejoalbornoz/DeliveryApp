package org.deliveryapp.auth_service.service;


import org.deliveryapp.auth_service.model.RefreshToken;
import org.deliveryapp.auth_service.model.User;

public interface IRefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken validateRefreshToken(String token);

    void deleteByUser(User user);
}
