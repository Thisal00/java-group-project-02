package com.evmaster.service;

import com.evmaster.model.User;
import com.evmaster.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

  
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
