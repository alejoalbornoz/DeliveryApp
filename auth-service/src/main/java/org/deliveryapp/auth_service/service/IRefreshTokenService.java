package org.deliveryapp.service;

import org.deliveryapp.model.RefreshToken;
import org.deliveryapp.model.User;

public interface IRefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken validateRefreshToken(String token);

    void deleteByUser(User user);
}
