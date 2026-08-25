package nl.templify.iceinsights.services.impl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import nl.templify.iceinsights.domain.PasswordResetToken;
import nl.templify.iceinsights.domain.RefreshToken;
import nl.templify.iceinsights.domain.User;
import nl.templify.iceinsights.dto.UserDto;
import nl.templify.iceinsights.dto.auth.AuthResponse;
import nl.templify.iceinsights.dto.auth.LoginRequest;
import nl.templify.iceinsights.dto.auth.RegisterRequest;
import nl.templify.iceinsights.exceptions.InvalidPasswordResetTokenException;
import nl.templify.iceinsights.exceptions.UsernameAlreadyExistsException;
import nl.templify.iceinsights.mapper.UserMapper;
import nl.templify.iceinsights.repositories.PasswordResetTokenRepository;
import nl.templify.iceinsights.repositories.RefreshTokenRepository;
import nl.templify.iceinsights.repositories.UserRepository;
import nl.templify.iceinsights.services.JwtService;
import nl.templify.iceinsights.services.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserMapper userMapper;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;
    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "refreshTokenDurationMs", 604800000L);
        ReflectionTestUtils.setField(authenticationService, "mailHost", "");
        ReflectionTestUtils.setField(authenticationService, "frontendBaseUrl", "http://localhost:3000");
    }

    private RegisterRequest registerRequest() {
        return RegisterRequest.builder()
                .firstName("Jan")
                .lastName("Jansen")
                .username("janjansen")
                .email("jan@example.com")
                .password("secret123!")
                .build();
    }

    @Test
    void register_encodesPasswordAndSavesEmail() {
        RegisterRequest request = registerRequest();
        when(userRepository.findByUsername("janjansen")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123!")).thenReturn("encoded-hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken("janjansen"))
                .thenReturn(RefreshToken.builder().token("refresh-token").build());
        when(userMapper.toDto(any(User.class))).thenReturn(
                UserDto.builder().username("janjansen").email("jan@example.com").build());

        AuthResponse response = authenticationService.register(request, httpServletResponse);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("encoded-hash", saved.getPassword());
        assertEquals("jan@example.com", saved.getEmail());
        assertEquals("janjansen", saved.getUsername());
        assertNull(response.getUser().getPassword());
        assertEquals("jwt-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(httpServletResponse).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();
        assertEquals("refresh_token", cookie.getName());
        assertEquals("refresh-token", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertEquals("/", cookie.getPath());
        assertEquals(604800, cookie.getMaxAge());
    }

    @Test
    void register_duplicateUsername_throwsConflict() {
        when(userRepository.findByUsername("janjansen")).thenReturn(Optional.of(User.builder().id(1L).build()));

        UsernameAlreadyExistsException ex = assertThrows(
                UsernameAlreadyExistsException.class,
                () -> authenticationService.register(registerRequest(), httpServletResponse));
        assertEquals("Username already exists", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsTokensWithoutPassword() {
        LoginRequest request = LoginRequest.builder()
                .username("janjansen")
                .password("secret123!")
                .build();
        User user = User.builder()
                .id(1L)
                .username("janjansen")
                .email("jan@example.com")
                .password("encoded-hash")
                .build();
        when(userRepository.findByUsername("janjansen")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken("janjansen"))
                .thenReturn(RefreshToken.builder().token("refresh-token").build());
        when(userMapper.toDto(user)).thenReturn(
                UserDto.builder().username("janjansen").email("jan@example.com").build());

        AuthResponse response = authenticationService.login(request, httpServletResponse);

        verify(authenticationManager).authenticate(any());
        assertEquals("jwt-token", response.getToken());
        assertNull(response.getUser().getPassword());
        verify(httpServletResponse).addCookie(argThat(cookie ->
                "refresh_token".equals(cookie.getName()) && cookie.isHttpOnly()));
    }

    @Test
    void forgotPassword_knownEmail_createsTokenAndDoesNotFailWithoutMail() {
        User user = User.builder().id(1L).username("janjansen").email("jan@example.com").build();
        when(userRepository.findByEmail("jan@example.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> authenticationService.initiatePasswordReset("jan@example.com"));

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).deleteByUser(user);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        PasswordResetToken saved = tokenCaptor.getValue();
        assertEquals(user, saved.getUser());
        assertNotNull(saved.getToken());
        assertTrue(saved.getExpiryDate().isAfter(LocalDateTime.now().plusMinutes(50)));
        verify(mailSenderProvider, never()).getIfAvailable();
    }

    @Test
    void forgotPassword_unknownEmail_succeedsWithoutCreatingToken() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authenticationService.initiatePasswordReset("unknown@example.com"));

        verify(passwordResetTokenRepository, never()).save(any());
        verify(passwordResetTokenRepository, never()).deleteByUser(any());
    }

    @Test
    void resetPassword_success_updatesPasswordAndDeletesTokens() {
        User user = User.builder().id(1L).username("janjansen").password("old-hash").build();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token("valid-token")
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();
        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("newpass1!")).thenReturn("new-hash");

        authenticationService.resetPassword("valid-token", "newpass1!");

        assertEquals("new-hash", user.getPassword());
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).delete(resetToken);
        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void resetPassword_expiredToken_throwsBadRequest() {
        User user = User.builder().id(1L).username("janjansen").password("old-hash").build();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token("expired-token")
                .user(user)
                .expiryDate(LocalDateTime.now().minusMinutes(1))
                .build();
        when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(resetToken));

        InvalidPasswordResetTokenException ex = assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> authenticationService.resetPassword("expired-token", "newpass1!"));
        assertEquals("Invalid or expired reset token", ex.getMessage());
        verify(userRepository, never()).save(any());
        verify(passwordResetTokenRepository).delete(resetToken);
    }

    @Test
    void resetPassword_unknownToken_throwsBadRequest() {
        when(passwordResetTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThrows(InvalidPasswordResetTokenException.class,
                () -> authenticationService.resetPassword("missing", "newpass1!"));
        verify(userRepository, never()).save(any());
    }
}
