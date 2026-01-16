package com.evmaster.controller;

import com.evmaster.model.User;
import com.evmaster.model.User.UserType;
import com.evmaster.repository.UserRepository;
import com.evmaster.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthenticationManager authenticationManager;

    // ------------- SIGNUP ----------------
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> signupRequest) {
        String email = signupRequest.get("email");
        String password = signupRequest.get("password");
        String nic = signupRequest.get("nic");
        String fullName = signupRequest.get("fullName");
        String phoneNumber = signupRequest.get("phoneNumber");
        String userTypeStr = signupRequest.get("userType");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body("Email and password required");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already used");
        }

        try {
            UserType userType = UserType.valueOf(userTypeStr.toUpperCase());
            User newUser = new User(email,
                    passwordEncoder.encode(password),
                    nic,
                    fullName,
                    phoneNumber,
                    userType);
            userRepository.save(newUser);
            return ResponseEntity.ok("User registered");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid user type");
        }
    }

    // ------------- LOGIN (manual) ----------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            // you can return user info if needed
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Map<String,Object> resp = new HashMap<>();
                resp.put("email", user.getEmail());
                resp.put("id", user.getId());
                resp.put("userType", user.getUserType());
                return ResponseEntity.ok(resp);
            }
            return ResponseEntity.ok("OK");
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bad credentials");
        }
    }

    // ------------- FORGOT / VERIFY / RESET (same as your earlier code) --------------
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            return new ResponseEntity<>("User with this email not found.", HttpStatus.NOT_FOUND);
        }
        User user = userOptional.get();
        String otp = generateOtp();
        user.setResetOtp(otp);
        user.setOtpIssued(true);
        userRepository.save(user);
        boolean sent = emailService.sendSimpleMail(email, "EV Master OTP", "Your OTP: " + otp);
        if (sent) return ResponseEntity.ok("OTP sent");
        // rollback
        user.setResetOtp(null);
        user.setOtpIssued(false);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send OTP");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        User user = userOptional.get();
        if (user.getResetOtp()!= null && user.getResetOtp().equals(otp) && Boolean.TRUE.equals(user.getOtpIssued())) {
            user.setResetOtp(null);
            userRepository.save(user);
            return ResponseEntity.ok("OTP verified");
        } else {
            user.setResetOtp(null);
            user.setOtpIssued(false);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String newPassword = req.get("newPassword");
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        User user = userOptional.get();
        if (Boolean.TRUE.equals(user.getOtpIssued())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setOtpIssued(false);
            userRepository.save(user);
            return ResponseEntity.ok("Password reset");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized reset attempt");
        }
    }

    // helper
    private String generateOtp() {
        Random r = new Random();
        int otp = 100000 + r.nextInt(900000);
        return String.valueOf(otp);
    }
}
