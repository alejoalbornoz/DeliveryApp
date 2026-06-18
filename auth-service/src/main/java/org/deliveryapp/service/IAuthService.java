package org.deliveryapp.service;

public interface IAuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login (LoginRequest request);



}
