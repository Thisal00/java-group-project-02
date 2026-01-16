package com.evmaster.controller;

import com.evmaster.model.Booking;
import com.evmaster.model.ChargingStation;
import com.evmaster.model.Review;
import com.evmaster.model.User;
import com.evmaster.model.User.UserType;
import com.evmaster.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for Super Admin functionalities: managing all users, stations, bookings, and reviews.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private UserRepository userRepository;
    @Autowired private StationRepository stationRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private VehicleRepository vehicleRepository;


    // 1. DASHBOARD STATS

    @GetMapping("/stats")
    public ResponseEntity<Object> getGlobalStats() {
        // FIX: Using the derived countByUserType method defined in the repository
        long totalOwners = userRepository.countByUserType(UserType.EV_OWNER);
        long totalManagers = userRepository.countByUserType(UserType.STATION_MANAGER);

        long totalStations = stationRepository.count();
        long totalCars = vehicleRepository.count();
        long totalBookings = bookingRepository.count();
        long totalReviews = reviewRepository.count();

        // This calculates the total cancellations by filtering all bookings
        long totalCancellations = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus().equals("CANCELLED"))
                .count();

        return ResponseEntity.ok(new Object() {
            public long getOwners() { return totalOwners; }
            public long getManagers() { return totalManagers; }
            public long getStations() { return totalStations; }
            public long getCars() { return totalCars; }
            public long getBookings() { return totalBookings; }
            public long getReviews() { return totalReviews; }
            public long getCancellations() { return totalCancellations; }
        });
    }


    // 2. USER MANAGEMENT


    @GetMapping("/users/{type}")
    public ResponseEntity<List<User>> getUsersByType(@PathVariable String type) {
        try {
            UserType userType = UserType.valueOf(type.toUpperCase());
            List<User> users = userRepository.findByUserType(userType);
            users.forEach(u -> u.setPassword(null));
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/users/search/nic/{nic}")
    public ResponseEntity<User> searchUserByNic(@PathVariable String nic) {
        User user = userRepository.findByNic(nic).orElse(null);
        if (user != null) {
            user.setPassword(null);
            return ResponseEntity.ok(user);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok("User deleted.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
    }

    // =======================================
    // 3. STATION & REVIEW MANAGEMENT
    // =======================================

    @GetMapping("/stations")
    public ResponseEntity<List<ChargingStation>> getAllStations() {
        return ResponseEntity.ok(stationRepository.findAll());
    }

    @DeleteMapping("/stations/{id}")
    public ResponseEntity<String> deleteStation(@PathVariable Long id) {
        if (stationRepository.existsById(id)) {
            stationRepository.deleteById(id);
            return ResponseEntity.ok("Station deleted.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Station not found.");
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingRepository.findAll());
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id) {
        if (bookingRepository.existsById(id)) {
            bookingRepository.deleteById(id);
            return ResponseEntity.ok("Booking deleted.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found.");
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewRepository.findAll());
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        if (reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
            return ResponseEntity.ok("Review deleted.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Review not found.");
    }
}