package com.evmaster.controller;

import com.evmaster.model.Booking;
import com.evmaster.model.User;
import com.evmaster.repository.BookingRepository;
import com.evmaster.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;


    // INITIATE PAYMENT (CARD/BANK DEMO)

    @PostMapping("/initiate/{bookingId}")
    public ResponseEntity<?> initiatePayment(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Booking booking = getBookingForCurrentUser(bookingId, userDetails);

        Map<String, Object> resp = new HashMap<>();

        if (booking.isPaid()) {
            resp.put("message", "Already paid");
            resp.put("status", "PAID");
            return ResponseEntity.ok(resp);
        }

        // DEMO gateway — frontend will redirect and then call /confirm
        resp.put("message", "Redirect to payment gateway");
        resp.put("paymentUrl", "/api/payment/confirm/" + bookingId);

        return ResponseEntity.ok(resp);
    }


    //  CONFIRM PAYMENT (CARD/BANK SUCCESS CALLBACK)

    @PostMapping("/confirm/{bookingId}")
    public ResponseEntity<?> confirmPayment(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Booking booking = getBookingForCurrentUser(bookingId, userDetails);

        if (booking.isPaid()) {
            return ResponseEntity.badRequest().body("Booking already paid");
        }

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            return ResponseEntity.badRequest().body("Booking is cancelled");
        }

        booking.setPaid(true);
        booking.setStatus("CONFIRMED");
        booking.setPaymentRef("PAY_" + System.currentTimeMillis());

        bookingRepository.save(booking);

        return ResponseEntity.ok("Payment successful. Booking confirmed.");
    }

    //  CASH PAYMENT REQUEST (EV OWNER)

    @PostMapping("/cash/{bookingId}")
    public ResponseEntity<?> cashPayment(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Booking booking = getBookingForCurrentUser(bookingId, userDetails);

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            return ResponseEntity.badRequest().body("Booking already cancelled");
        }

        if (booking.isPaid()) {
            return ResponseEntity.badRequest().body("Booking already paid");
        }

        booking.setPaid(false);
        booking.setStatus("CASH_PENDING");
        booking.setPaymentRef("CASH_REQ_" + System.currentTimeMillis());

        bookingRepository.save(booking);

        return ResponseEntity.ok("Cash payment request sent. Waiting for manager approval.");
    }


    //  MANAGER APPROVES CASH PAYMENT

    @PostMapping("/approve/{bookingId}")
    public ResponseEntity<?> approveCashPayment(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User manager = getCurrentUser(userDetails);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        //  SECURITY: ensure this manager owns the station
        if (!booking.getCharger().getStation().getManager().getId().equals(manager.getId())) {
            return ResponseEntity.status(403).body("You do not own this station");
        }

        if (booking.isPaid()) {
            return ResponseEntity.badRequest().body("Booking already paid");
        }

        if (!"CASH_PENDING".equalsIgnoreCase(booking.getStatus())) {
            return ResponseEntity.badRequest().body("Booking is not in CASH_PENDING state");
        }

        booking.setPaid(true);
        booking.setStatus("CONFIRMED");
        booking.setPaymentRef("CASH_APPROVED_" + System.currentTimeMillis());

        bookingRepository.save(booking);

        return ResponseEntity.ok("Cash payment approved. Booking confirmed.");
    }

    // HELPERS

    private Booking getBookingForCurrentUser(Long bookingId, UserDetails userDetails) {
        User user = getCurrentUser(userDetails);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // EV Owner can only access his own booking
        if (!booking.getEvOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return booking;
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
