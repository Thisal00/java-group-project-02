package com.evmaster.service.impl;

import com.evmaster.model.*;
import com.evmaster.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReviewServiceImpl {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private static final String UPLOAD_DIR = "uploads/reviews/";

    @Transactional(readOnly = true)
    public List<Review> getReviewsByOwner(User owner) {
        return reviewRepository.findByEvOwnerOrderByReviewDateDesc(owner);
    }

    @Transactional
    public Optional<Review> createReviewMultipart(Long bookingId, int rating, String comments,
                                                  MultipartFile image, User owner) {

        Optional<Booking> optBooking = bookingRepository.findById(bookingId);
        if (optBooking.isEmpty()) return Optional.empty();

        Booking booking = optBooking.get();

        if (!booking.getEvOwner().getId().equals(owner.getId())) return Optional.empty();
        if (!"FINISHED".equalsIgnoreCase(String.valueOf(booking.getStatus()))) return Optional.empty();
        if (reviewRepository.findByBooking(booking).isPresent()) return Optional.empty();
        if (rating < 1 || rating > 5) return Optional.empty();

        Review review = new Review();
        review.setEvOwner(owner);
        review.setBooking(booking);
        review.setStation(booking.getCharger().getStation());
        review.setRating(rating);
        review.setComments(comments);
        review.setReviewDate(LocalDateTime.now());
        review.setApproved(false);

        if (image != null && !image.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

                String safeName = System.currentTimeMillis() + "_" +
                        image.getOriginalFilename().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

                Path filePath = uploadPath.resolve(safeName);
                Files.copy(image.getInputStream(), filePath);

                review.setImageUrl("/" + UPLOAD_DIR + safeName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Optional.of(reviewRepository.save(review));
    }
}
