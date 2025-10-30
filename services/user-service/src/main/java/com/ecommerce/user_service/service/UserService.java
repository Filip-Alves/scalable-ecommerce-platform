package com.ecommerce.user_service.service;

import com.ecommerce.user_service.dto.LoginRequest;
import com.ecommerce.user_service.dto.LoginResponse;
import com.ecommerce.user_service.dto.RegisterUserRequest;
import com.ecommerce.user_service.dto.ValidateResponse;
import com.ecommerce.user_service.model.User;
import com.ecommerce.user_service.repository.UserRepository;
import com.ecommerce.user_service.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

    public LoginResponse login(LoginRequest loginRequest) {
        User connectedUser = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe invalide"));
        if (!passwordEncoder.matches(loginRequest.password(), connectedUser.getPassword())) {
            throw new RuntimeException("Email ou mot de passe invalide");
        }

        return new LoginResponse(jwtService.generateToken(connectedUser));
    }

    public boolean validate(String token) {
        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        return jwtService.isTokenValid(token, user);
    }

    public ValidateResponse validateAndGetUser(String token) {
        try {
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));
            return new ValidateResponse(user.getId(), user.getEmail());
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }

}
