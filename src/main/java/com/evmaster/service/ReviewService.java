package com.evmaster.service;

import com.evmaster.model.Booking;
import com.evmaster.model.Review;
import com.evmaster.model.User;
import com.evmaster.repository.BookingRepository;
import com.evmaster.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private static final String UPLOAD_ROOT = "uploads/reviews/";


    // GET REVIEWS BY OWNER

    public List<Review> getReviewsByOwner(User owner) {
        return reviewRepository.findByEvOwnerOrderByReviewDateDesc(owner);
    }


    // CREATE REVIEW (PENDING)

    @Transactional
    public Optional<Review> createReviewMultipart(
            Long bookingId,
            int rating,
            String comments,
            MultipartFile image,
            User owner
    ) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) return Optional.empty();

        Booking booking = bookingOpt.get();

        // Must be owner
        if (!booking.getEvOwner().getId().equals(owner.getId())) return Optional.empty();

        // Must be finished
        if (!"FINISHED".equalsIgnoreCase(String.valueOf(booking.getStatus()))) return Optional.empty();

        // Prevent duplicate
        if (reviewRepository.findByBooking(booking).isPresent()) return Optional.empty();

        // Rating check
        if (rating < 1 || rating > 5) return Optional.empty();

        Review review = new Review();
        review.setEvOwner(owner);
        review.setBooking(booking);
        review.setStation(booking.getCharger().getStation());
        review.setRating(rating);
        review.setComments(comments);
        review.setReviewDate(LocalDateTime.now());

        //  IMPORTANT: START AS PENDING
        review.setApproved(false);

        // Save image if exists
        if (image != null && !image.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_ROOT);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

                String safeName = System.currentTimeMillis() + "_" +
                        image.getOriginalFilename().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

                Path filePath = uploadPath.resolve(safeName);
                Files.copy(image.getInputStream(), filePath);

                review.setImageUrl("/uploads/reviews/" + safeName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Optional.of(reviewRepository.save(review));
    }


    // MANAGER: GET ALL REVIEWS (PENDING + APPROVED)

    public List<Review> getReviewsByManager(User manager) {
        return reviewRepository.findByStation_ManagerOrderByReviewDateDesc(manager);
    }


    // MANAGER: GET ONLY APPROVED

    public List<Review> getApprovedReviewsByManager(User manager) {
        return reviewRepository.findByStation_ManagerAndApprovedTrueOrderByReviewDateDesc(manager);
    }


    //  MANAGER: APPROVE REVIEW

    @Transactional
    public boolean approveReview(Long reviewId, User manager) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) return false;

        // Only station owner can approve
        if (!review.getStation().getManager().getId().equals(manager.getId())) {
            return false;
        }

        review.setApproved(true);
        reviewRepository.save(review);
        return true;
    }
}
