package com.savagescreen.backend.service;

import com.savagescreen.backend.dto.AuthResponse;
import com.savagescreen.backend.dto.LoginRequest;
import com.savagescreen.backend.dto.RegisterRequest;
import com.savagescreen.backend.dto.UserProfile;
import com.savagescreen.backend.model.AuthSession;
import com.savagescreen.backend.model.UserAccount;
import com.savagescreen.backend.repository.AuthSessionRepository;
import com.savagescreen.backend.repository.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class AuthService {
    private static final int SESSION_LIFETIME_DAYS = 14;
    private final UserAccountRepository userRepository;
    private final AuthSessionRepository sessionRepository;
    private final PasswordHasher passwordHasher;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserAccountRepository userRepository, AuthSessionRepository sessionRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().strip().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists.");
        }
        String salt = passwordHasher.newSalt();
        UserAccount user = userRepository.save(new UserAccount(email, request.displayName().strip(), salt,
                passwordHasher.hash(request.password(), salt)));
        return createSession(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.email().strip().toLowerCase();
        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect."));
        if (!passwordHasher.matches(request.password(), user.getPasswordSalt(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect.");
        }
        return createSession(user);
    }

    @Transactional(readOnly = true)
    public UserAccount requireUser(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ") || authorization.length() <= 7) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in to perform this action.");
        }
        String token = authorization.substring(7).strip();
        return sessionRepository.findByTokenHashAndExpiresAtAfter(hashToken(token), LocalDateTime.now())
                .map(AuthSession::getUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Your session is invalid or expired. Please sign in again."));
    }

    @Transactional
    public void logout(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ") || authorization.length() <= 7) {
            return;
        }
        sessionRepository.deleteByTokenHash(hashToken(authorization.substring(7).strip()));
    }

    private AuthResponse createSession(UserAccount user) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessionRepository.save(new AuthSession(hashToken(token), LocalDateTime.now().plusDays(SESSION_LIFETIME_DAYS), user));
        return new AuthResponse(token, profile(user));
    }

    public UserProfile profile(UserAccount user) {
        return new UserProfile(user.getId(), user.getEmail(), user.getDisplayName());
    }

    private String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Session token hashing is unavailable", exception);
        }
    }
}
