
package com.vertexone.prod.identity.service;

import com.vertexone.prod.identity.dto.request.UserRegistrationRequest;
import com.vertexone.prod.identity.dto.response.UserResponse;
import com.vertexone.prod.identity.entity.User;
import com.vertexone.prod.identity.repository.UserRepository;
import com.vertexone.prod.common.exception.DuplicateResourceException;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse registerUser(UserRegistrationRequest request) {

        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(
                    "An account with this email already exists"
            );
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {

        User user = userRepository.findByEmail(
                email.trim().toLowerCase(Locale.ROOT)
        ).orElseThrow(() -> new com.vertexone.prod.common.exception.ResourceNotFoundException(
                "User not found"
        ));

        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}
