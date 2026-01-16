package com.evmaster.service;

import com.evmaster.model.User;
import com.evmaster.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for retrieving user information by ID or Email.
 * Essential for providing context to security-gated controllers.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Finds a user by their email address.
     */
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Saves or updates a user object.
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}