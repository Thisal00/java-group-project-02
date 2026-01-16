package com.evmaster.repository;

import com.evmaster.model.Booking;
import com.evmaster.model.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {


    // OWNER


    // All bookings for owner (latest first)
    List<Booking> findByEvOwnerOrderByStartTimeDesc(User evOwner);

    // Single booking owned by user (security check)
    Optional<Booking> findByIdAndEvOwner(Long bookingId, User owner);


    // MANAGER (FETCH JOIN to avoid Lazy issues)


    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.evOwner
        JOIN FETCH b.charger c
        JOIN FETCH c.station
        WHERE c.station.manager = :manager
        ORDER BY b.startTime DESC
    """)
    List<Booking> findAllForManager(@Param("manager") User manager);


    // INCOME REPORT (PAID + FINISHED)

    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.evOwner
        JOIN FETCH b.charger c
        JOIN FETCH c.station
        WHERE c.station.manager = :manager
          AND b.paid = true
          AND UPPER(b.status) = 'FINISHED'
        ORDER BY b.startTime DESC
    """)
    List<Booking> findPaidFinishedBookings(@Param("manager") User manager);



    // Fast exists check
    boolean existsByIdAndEvOwner(Long id, User evOwner);
}
