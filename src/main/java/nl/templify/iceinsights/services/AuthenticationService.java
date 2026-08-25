package nl.templify.iceinsights.services;

import jakarta.servlet.http.HttpServletResponse;
import nl.templify.iceinsights.dto.auth.AuthResponse;
import nl.templify.iceinsights.dto.auth.LoginRequest;
import nl.templify.iceinsights.dto.auth.RegisterRequest;
import nl.templify.iceinsights.exceptions.UsernameAlreadyExistsException;

public interface AuthenticationService {
    AuthResponse register(RegisterRequest request) throws UsernameAlreadyExistsException;
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void initiatePasswordReset(String email);
    void resetPassword(String token, String newPassword);
    void logout(HttpServletResponse response);
}