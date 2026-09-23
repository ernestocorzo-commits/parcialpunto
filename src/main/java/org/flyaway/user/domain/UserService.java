package org.flyaway.user.domain;

import lombok.RequiredArgsConstructor;
import org.flyaway.exception.ConflictException;
import org.flyaway.exception.ResourceNotFoundException;
import org.flyaway.user.dto.RegisterUserRequest;
import org.flyaway.user.dto.UserIdResponse;
import org.flyaway.user.infrastructure.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserIdResponse register(RegisterUserRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered");
        }

        User user = User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .build();

        return new UserIdResponse(userRepository.save(user).getId());
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
