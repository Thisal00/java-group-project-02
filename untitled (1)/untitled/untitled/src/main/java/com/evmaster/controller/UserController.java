package com.evmaster.controller;

import com.evmaster.model.User;
import com.evmaster.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // CURRENT LOGGED USER
    @GetMapping("/me")
    public User getMe(Authentication authentication) {

        if (authentication == null) {
            throw new RuntimeException("Not logged in");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email).orElseThrow();
    }
}
