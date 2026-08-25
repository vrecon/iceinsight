package nl.templify.iceinsights.services.impl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.templify.iceinsights.domain.PasswordResetToken;
import nl.templify.iceinsights.domain.RefreshToken;
import nl.templify.iceinsights.domain.User;
import nl.templify.iceinsights.dto.auth.AuthResponse;
import nl.templify.iceinsights.dto.auth.LoginRequest;
import nl.templify.iceinsights.dto.auth.RegisterRequest;
import nl.templify.iceinsights.exceptions.InvalidPasswordResetTokenException;
import nl.templify.iceinsights.exceptions.TokenRefreshException;
import nl.templify.iceinsights.exceptions.UsernameAlreadyExistsException;
import nl.templify.iceinsights.mapper.UserMapper;
import nl.templify.iceinsights.repositories.PasswordResetTokenRepository;
import nl.templify.iceinsights.repositories.RefreshTokenRepository;
import nl.templify.iceinsights.repositories.UserRepository;
import nl.templify.iceinsights.services.AuthenticationService;
import nl.templify.iceinsights.services.JwtService;
import nl.templify.iceinsights.services.RefreshTokenService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenDurationMs;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        var user = User.builder()
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);
        // Genereer en sla refresh token op
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());
        setRefreshTokenCookie(response, refreshToken.getToken());

        return AuthResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken.getToken())
                .user(userMapper.toDto(user))
                .build();
    }

    @Transactional
    @Override
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
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
        setRefreshTokenCookie(response, refreshToken.getToken());

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
    @Transactional
    public void initiatePasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUser(user);

            String tokenValue = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(tokenValue)
                    .user(user)
                    .expiryDate(LocalDateTime.now().plusHours(1))
                    .build();
            passwordResetTokenRepository.save(resetToken);

            if (StringUtils.hasText(mailHost)) {
                sendPasswordResetEmail(email, tokenValue);
            } else {
                log.debug("Mail host not configured; password reset token for {}: {}", email, tokenValue);
            }
        });
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidPasswordResetTokenException("Invalid or expired reset token"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidPasswordResetTokenException("Invalid or expired reset token");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
        refreshTokenRepository.deleteByUser(user);
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

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refresh_token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) TimeUnit.MILLISECONDS.toSeconds(refreshTokenDurationMs));
        response.addCookie(cookie);
    }

    private void sendPasswordResetEmail(String email, String tokenValue) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.debug("JavaMailSender not available; password reset token for {}: {}", email, tokenValue);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Reset your IceInsights password");
            message.setText("Reset your password using this link: "
                    + frontendBaseUrl + "/reset-password?token=" + tokenValue
                    + "\nThis link expires in 1 hour.");
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send password reset email to {}", email, e);
            log.debug("Password reset token for {}: {}", email, tokenValue);
        }
    }

}
