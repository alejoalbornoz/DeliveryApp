package org.deliveryapp.auth_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.deliveryapp.auth_service.dto.request.LoginRequestDTO;
import org.deliveryapp.auth_service.dto.request.RefreshTokenRequestDTO;
import org.deliveryapp.auth_service.dto.request.RegisterRequestDTO;
import org.deliveryapp.auth_service.dto.response.AuthResponseDTO;
import org.deliveryapp.auth_service.dto.response.TokenRefreshResponseDTO;
import org.deliveryapp.auth_service.dto.response.UserResponseDTO;
import org.deliveryapp.auth_service.exception.UserAlreadyExistsException;
import org.deliveryapp.auth_service.model.RefreshToken;
import org.deliveryapp.auth_service.model.User;
import org.deliveryapp.auth_service.repository.IUserRepository;
import org.deliveryapp.auth_service.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService{

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getEmail());

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        return AuthResponseDTO.of(accessToken, refreshToken.getToken(), toUserResponse(savedUser));
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        // AuthenticationManager validates credentials and throws on failure
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("User logged in: {}", user.getEmail());
        return AuthResponseDTO.of(accessToken, refreshToken.getToken(), toUserResponse(user));
    }

    @Override
    @Transactional
    public TokenRefreshResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        RefreshToken validated = refreshTokenService.validateRefreshToken(request.getRefreshToken());

        User user = validated.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);

        // Token rotation: delete old, issue new refresh token
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return TokenRefreshResponseDTO.of(newAccessToken, newRefreshToken.getToken());
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.validateRefreshToken(refreshToken);
        User user = refreshTokenService.validateRefreshToken(refreshToken).getUser();
        refreshTokenService.deleteByUser(user);
        log.info("User logged out: {}", user.getEmail());
    }

    private UserResponseDTO toUserResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

}
