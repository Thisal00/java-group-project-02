package com.evmaster.controller;

import com.evmaster.model.Booking;
import com.evmaster.model.Review;
import com.evmaster.model.User;
import com.evmaster.repository.BookingRepository;
import com.evmaster.repository.ReviewRepository;
import com.evmaster.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/owner/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public ReviewController(
            ReviewRepository reviewRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    //CREATE REVIEW
    @PostMapping(consumes = "multipart/form-data")
    public Review createReview(
            @RequestParam Long bookingId,
            @RequestParam int rating,
            @RequestParam String comments,
            @RequestParam(required = false) MultipartFile image,
            Authentication authentication
    ) throws Exception {

        if (authentication == null) {
            throw new RuntimeException("Not logged in");
        }

        //GET LOGGED USER 
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //LOAD BOOKING 
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        //SECURITY CHECK
        if (!booking.getEvOwner().getId().equals(user.getId())) {
            throw new RuntimeException("You can only review your own bookings");
        }

        //STATUS CHECK
        if (!"FINISHED".equalsIgnoreCase(booking.getStatus())) {
            throw new RuntimeException("You can only review FINISHED bookings");
        }

        if (!booking.isPaid()) {
            throw new RuntimeException("You can only review PAID bookings");
        }

        //DUPLICATE CHECK
        if (reviewRepository.existsByBooking(booking)) {
            throw new RuntimeException("Review already exists for this booking");
        }

        //CREATE REVIEW
        Review review = new Review();
        review.setBooking(booking);
        review.setEvOwner(user);                        
        review.setStation(booking.getCharger().getStation()); 
        review.setRating(rating);
        review.setComments(comments);
        review.setReviewDate(LocalDateTime.now());
        review.setApproved(false);

        //IMAGE SAVE
        if (image != null && !image.isEmpty()) {

            Path uploadDir = Paths.get("uploads/reviews");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            Path filePath = uploadDir.resolve(fileName);
            Files.write(filePath, image.getBytes());

            review.setImageUrl("/uploads/reviews/" + fileName);
        }

        return reviewRepository.save(review);
    }
}

