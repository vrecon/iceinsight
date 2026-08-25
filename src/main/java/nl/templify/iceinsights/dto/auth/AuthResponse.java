package nl.templify.iceinsights.dto.auth;

import lombok.Builder;
import lombok.Data;
import nl.templify.iceinsights.dto.UserDto;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
    private UserDto user;
}