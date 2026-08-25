package nl.templify.iceinsights.services;

import jakarta.servlet.http.HttpServletResponse;
import nl.templify.iceinsights.dto.auth.AuthResponse;
import nl.templify.iceinsights.dto.auth.LoginRequest;
import nl.templify.iceinsights.dto.auth.RegisterRequest;

public interface AuthenticationService {
    AuthResponse register(RegisterRequest request, HttpServletResponse response);
    AuthResponse login(LoginRequest request, HttpServletResponse response);
    AuthResponse refreshToken(String refreshToken);
    void initiatePasswordReset(String email);
    void resetPassword(String token, String newPassword);
    void logout(HttpServletResponse response);
}
