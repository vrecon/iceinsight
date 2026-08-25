package nl.templify.iceinsights.services.impl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import nl.templify.iceinsights.domain.User;
import nl.templify.iceinsights.domain.RefreshToken;
import nl.templify.iceinsights.dto.auth.AuthResponse;
import nl.templify.iceinsights.dto.auth.LoginRequest;
import nl.templify.iceinsights.dto.auth.RegisterRequest;
import nl.templify.iceinsights.exceptions.TokenRefreshException;
import nl.templify.iceinsights.exceptions.UsernameAlreadyExistsException;
import nl.templify.iceinsights.mapper.UserMapper;
import nl.templify.iceinsights.repositories.RefreshTokenRepository;
import nl.templify.iceinsights.repositories.UserRepository;
import nl.templify.iceinsights.services.AuthenticationService;
import nl.templify.iceinsights.services.JwtService;
import nl.templify.iceinsights.services.RefreshTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) throws UsernameAlreadyExistsException {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        var user = User.builder()
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);
        // Genereer en sla refresh token op
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        return AuthResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken.getToken())
                .user(userMapper.toDto(user))
                .build();
    }

@Transactional
    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String jwtToken = jwtService.generateToken(user);
        // Genereer en sla refresh token op
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        return AuthResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken.getToken())
                .user(userMapper.toDto(user))
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(token -> {
                    User user = token.getUser();
                    String jwtToken = jwtService.generateToken(user);
                    return AuthResponse.builder()
                            .token(jwtToken)
                            .refreshToken(refreshToken)  // We hergebruiken dezelfde refresh token
                            .user(userMapper.toDto(user))
                            .build();
                })
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));
    }

    @Override
    public void initiatePasswordReset(String email) {

    }

    @Override
    public void resetPassword(String token, String newPassword) {

    }

    @Override
    public void logout(HttpServletResponse response) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userRepository.findByUsername(username)
                .ifPresent(user -> refreshTokenRepository.deleteByUser(user));

        // Clear security context
        SecurityContextHolder.clearContext();

        // Clear cookies if you're using them
        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);
    }

}