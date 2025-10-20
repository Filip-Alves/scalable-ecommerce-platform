package com.ecommerce.user_service.service;

import com.ecommerce.user_service.dto.RegisterUserRequest;
import com.ecommerce.user_service.model.User;
import com.ecommerce.user_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(RegisterUserRequest userRequest) {
        if (userRepository.findByEmail(userRequest.email()).isPresent()) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        User newUser = new User(
                userRequest.firstName(),
                userRequest.lastName(),
                userRequest.email(),
                passwordEncoder.encode(userRequest.password())
        );

        return userRepository.save(newUser);
    }
}
