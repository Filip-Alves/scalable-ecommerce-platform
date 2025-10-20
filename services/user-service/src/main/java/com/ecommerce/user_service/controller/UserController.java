package com.ecommerce.user_service.controller;


import com.ecommerce.user_service.dto.RegisterUserRequest;
import com.ecommerce.user_service.dto.UserResponse;
import com.ecommerce.user_service.model.User;
import com.ecommerce.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterUserRequest userRequest) {
        User createdUser = userService.registerUser(userRequest);

        UserResponse response = new UserResponse(
                createdUser.getId(),
                createdUser.getFirstName(),
                createdUser.getLastName(),
                createdUser.getEmail()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
