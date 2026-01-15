package com.evmaster.repository;

import com.evmaster.model.Booking;
import com.evmaster.model.Review;
import com.evmaster.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Review operations.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // OWNER SIDE


    // Get all reviews by EV owner (latest first)
    List<Review> findByEvOwnerOrderByReviewDateDesc(User evOwner);

    // DUPLICATE PREVENTION


    Optional<Review> findByBooking(Booking booking);
    Optional<Review> findByBookingId(Long bookingId);

    boolean existsByBooking(Booking booking);
    boolean existsByBookingId(Long bookingId);

   
    // MANAGER SIDE


    // Get ALL reviews for manager stations
    List<Review> findByStation_ManagerOrderByReviewDateDesc(User manager);

    // Get ONLY approved reviews (still works, but now all are approved)
    List<Review> findByStation_ManagerAndApprovedTrueOrderByReviewDateDesc(User manager);


    // ADMIN SIDE
 

    // Get all pending reviews (will be empty now because auto-approve)
    List<Review> findByApprovedFalseOrderByReviewDateAsc();
}
