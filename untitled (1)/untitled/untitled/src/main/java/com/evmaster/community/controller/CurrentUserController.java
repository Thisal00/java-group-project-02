package com.evmaster.community.controller;

import com.evmaster.model.User;
import com.evmaster.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
public class CurrentUserController {

    private final UserRepository userRepository;

    public CurrentUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
//===================== GET CURRENT USER =====================
    @GetMapping
    public User getCurrentUser(Authentication authentication) {
        String email = authentication.getName(); // logged email
        return userRepository.findByEmail(email).orElseThrow();
    }
}
