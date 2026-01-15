package com.evmaster.controller;

import com.evmaster.model.Booking;
import com.evmaster.model.Review;
import com.evmaster.model.User;
import com.evmaster.model.Vehicle;
import com.evmaster.model.Charger;
import com.evmaster.service.BookingService;
import com.evmaster.service.ReviewService;
import com.evmaster.service.UserService;
import com.evmaster.service.VehicleService;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/owner")
public class EvOwnerController {

    @Autowired private UserService userService;
    @Autowired private VehicleService vehicleService;
    @Autowired private BookingService bookingService;
    @Autowired private ReviewService reviewService;


    // AUTH HELPER

    private User getCurrentUser(UserDetails principal) {
        if (principal == null || principal.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired. Please login again.");
        }
        return userService.findUserByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found."));
    }

   

    @GetMapping("/vehicles")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Vehicle>> getMyVehicles(@AuthenticationPrincipal UserDetails principal) {
        User owner = getCurrentUser(principal);
        return ResponseEntity.ok(vehicleService.getAllVehiclesByOwner(owner));
    }

    @PostMapping("/vehicles")
    public ResponseEntity<Vehicle> addOrUpdateVehicle(
            @RequestBody Vehicle vehicle,
            @AuthenticationPrincipal UserDetails principal
    ) {
        User owner = getCurrentUser(principal);

        vehicle.setOwner(owner); // 
        boolean isCreate = (vehicle.getId() == null);

        if (!isCreate) {
            boolean allowed = vehicleService.isVehicleOwnedBy(vehicle.getId(), owner);
            if (!allowed) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vehicle not found or unauthorized.");
            }
        }

        Vehicle saved = vehicleService.saveVehicle(vehicle, owner);
        return ResponseEntity.status(isCreate ? HttpStatus.CREATED : HttpStatus.OK).body(saved);
    }

    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<String> deleteVehicle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        User owner = getCurrentUser(principal);
        if (vehicleService.deleteVehicle(id, owner)) {
            return ResponseEntity.ok("Vehicle deleted successfully.");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Vehicle not found or unauthorized.");
    }


    // BOOKINGS


    @GetMapping("/bookings")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Booking>> getMyBookings(@AuthenticationPrincipal UserDetails principal) {
        User owner = getCurrentUser(principal);
        return ResponseEntity.ok(bookingService.getBookingsByOwner(owner));
    }

    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(
            @RequestBody JsonNode body,
            @AuthenticationPrincipal UserDetails principal
    ) {
        User owner = getCurrentUser(principal);

        Long chargerId = body.path("charger").path("id").asLong(0);
        Long vehicleId = body.path("vehicle").path("id").asLong(0);
        int totalHours = body.path("totalHours").asInt(0);

        if (chargerId <= 0 || vehicleId <= 0 || totalHours <= 0) {
            return ResponseEntity.badRequest().body("Invalid booking data.");
        }

        //  Vehicle must belong to owner
        Optional<Vehicle> optVehicle = vehicleService.findByIdAndOwner(vehicleId, owner);
        if (optVehicle.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Vehicle not found or unauthorized.");
        }

        Booking incoming = new Booking();

        Vehicle v = new Vehicle();
        v.setId(vehicleId);
        incoming.setVehicle(v);

        Charger c = new Charger();
        c.setId(chargerId);
        incoming.setCharger(c);

        incoming.setTotalHours(totalHours);

        try {
            Booking saved = bookingService.createBookingOrThrow(incoming, owner);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error.");
        }
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<String> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        User owner = getCurrentUser(principal);
        if (bookingService.cancelBooking(id, owner)) {
            return ResponseEntity.ok("Booking cancelled successfully.");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Booking not found or cannot be cancelled.");
    }


    //  REVIEWS 
   

    @GetMapping("/reviews")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Review>> getMyReviews(@AuthenticationPrincipal UserDetails principal) {
        User owner = getCurrentUser(principal);
        return ResponseEntity.ok(reviewService.getReviewsByOwner(owner));
    }

    @PostMapping(
            value = "/reviews/{bookingId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> submitReview(
            @PathVariable Long bookingId,
            @RequestParam("rating") int rating,
            @RequestParam("comments") String comments,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetails principal
    ) {
        User owner = getCurrentUser(principal);

        return reviewService.createReviewMultipart(bookingId, rating, comments, image, owner)
                .<ResponseEntity<?>>map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Review failed: Booking invalid / not finished / already reviewed."));
    }


    // PROFILE
 

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal UserDetails principal) {
        User user = getCurrentUser(principal);
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(
            @RequestBody User userUpdates,
            @AuthenticationPrincipal UserDetails principal
    ) {
        User user = getCurrentUser(principal);

        user.setFullName(userUpdates.getFullName());
        user.setNic(userUpdates.getNic());
        user.setPhoneNumber(userUpdates.getPhoneNumber());

        userService.saveUser(user);
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }
}
