package org.flyaway.auth.application;

import lombok.RequiredArgsConstructor;
import org.flyaway.auth.dto.LoginRequest;
import org.flyaway.auth.dto.TokenResponse;
import org.flyaway.config.JwtService;
import org.flyaway.exception.ResourceNotFoundException;
import org.flyaway.exception.UnauthorizedException;
import org.flyaway.user.domain.User;
import org.flyaway.user.infrastructure.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No user registered with that email"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Incorrect password");
        }

        return new TokenResponse(jwtService.generateToken(user.getId(), user.getEmail()));
    }
}
