package com.evmaster.controller;

import com.evmaster.dto.BookingDTO;
import com.evmaster.dto.ChargerDTO;
import com.evmaster.dto.ChargingStationDTO;
import com.evmaster.model.Booking;
import com.evmaster.model.Charger;
import com.evmaster.model.ChargingStation;
import com.evmaster.model.Review;
import com.evmaster.model.User;
import com.evmaster.service.BookingService;
import com.evmaster.service.ChargerService;
import com.evmaster.service.ReviewService;
import com.evmaster.service.StationService;
import com.evmaster.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
public class StationManagerController {

    @Autowired private UserService userService;
    @Autowired private StationService stationService;
    @Autowired private ChargerService chargerService;
    @Autowired private BookingService bookingService;
    @Autowired private ReviewService reviewService;

    // SESSION HELPER
    private User getCurrentUser(UserDetails principal) {
        if (principal == null || principal.getUsername() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired. Please login again.");
        }
        return userService.findUserByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated manager not found."));
    }

    // CREATE STATION
    @PostMapping("/stations")
    public ResponseEntity<?> createStation(
            @RequestBody ChargingStationDTO dto,
            @AuthenticationPrincipal UserDetails principal) {

        User manager = getCurrentUser(principal);

        ChargingStation s = new ChargingStation();
        s.setName(dto.getName());
        s.setAddress(dto.getAddress());
        s.setLatitude(dto.getLatitude());
        s.setLongitude(dto.getLongitude());
        s.setStatus("ACTIVE");

        stationService.saveStation(s, manager);

        return ResponseEntity.ok("Station created successfully");
    }

    // ADD CHARGER
    @PostMapping("/stations/{stationId}/chargers")
    public ResponseEntity<?> addCharger(
            @PathVariable Long stationId,
            @RequestBody ChargerDTO dto,
            @AuthenticationPrincipal UserDetails principal) {

        User manager = getCurrentUser(principal);

        boolean ok = chargerService.addChargerToStation(stationId, dto, manager);

        if (!ok) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not your station or station not found");
        }

        return ResponseEntity.ok("Charger added successfully");
    }

    @GetMapping("/stations/{stationId}/chargers")
    public ResponseEntity<List<ChargerDTO>> getChargersByStation(
            @PathVariable Long stationId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        User manager = getCurrentUser(principal);

        List<Charger> chargers = chargerService.getChargersByStationId(stationId, manager);

        List<ChargerDTO> dtos = chargers.stream().map(this::toChargerDTO).toList();

        return ResponseEntity.ok(dtos);
    }

    // GET MY STATIONS
    @GetMapping("/stations")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ChargingStationDTO>> getMyStations(@AuthenticationPrincipal UserDetails principal) {
        User manager = getCurrentUser(principal);
        List<ChargingStation> stations = stationService.getAllStationsByManagerWithChargers(manager);
        return ResponseEntity.ok(stations.stream().map(this::toDTO).toList());
    }

    // GET BOOKINGS
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingDTO>> getBookingsForMyStations(@AuthenticationPrincipal UserDetails principal) {
        User manager = getCurrentUser(principal);
        List<Booking> allBookings = bookingService.getBookingsByManager(manager);
        return ResponseEntity.ok(allBookings.stream().map(BookingDTO::from).toList());
    }

    // FINISH BOOKING
    @PostMapping("/bookings/{bookingId}/finish")
    public ResponseEntity<String> finishBookingByManager(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserDetails principal) {

        User manager = getCurrentUser(principal);
        boolean ok = bookingService.finishBookingByManager(bookingId, manager.getId());

        if (ok) return ResponseEntity.ok("Booking marked as FINISHED.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not allowed.");
    }

    // CANCEL BOOKING
    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<String> cancelBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserDetails principal) {

        User manager = getCurrentUser(principal);

        boolean ok = bookingService.cancelBookingByManager(bookingId, manager);
        if (ok) return ResponseEntity.ok("Booking cancelled.");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not allowed.");
    }

    // INCOME
    @GetMapping("/income")
    public ResponseEntity<IncomeResponse> getIncome(@AuthenticationPrincipal UserDetails principal) {
        User manager = getCurrentUser(principal);

        List<Booking> finished = bookingService.getPaidFinishedBookingsByManager(manager);

        double total = finished.stream()
                .mapToDouble(b -> b.getTotalPrice() == null ? 0.0 : b.getTotalPrice())
                .sum();

        return ResponseEntity.ok(new IncomeResponse(
                total,
                finished.stream().map(BookingDTO::from).toList()
        ));
    }

    // GET ALL REVIEWS
    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getReviewsForMyStations(@AuthenticationPrincipal UserDetails principal) {
        User manager = getCurrentUser(principal);
        return ResponseEntity.ok(reviewService.getReviewsByManager(manager));
    }

    // APPROVE REVIEW
    @PostMapping("/reviews/{reviewId}/approve")
    public ResponseEntity<?> approveReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        User manager = getCurrentUser(principal);

        boolean ok = reviewService.approveReview(reviewId, manager);

        if (!ok) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not allowed");
        }

        return ResponseEntity.ok("Review approved");
    }

    // DTO MAPPERS
    private ChargingStationDTO toDTO(ChargingStation s) {
        ChargingStationDTO dto = new ChargingStationDTO();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setAddress(s.getAddress());
        dto.setLatitude(s.getLatitude() != null ? s.getLatitude() : 0);
        dto.setLongitude(s.getLongitude() != null ? s.getLongitude() : 0);
        dto.setStatus(s.getStatus());
        if (s.getChargers() != null) {
            dto.setChargers(s.getChargers().stream().map(this::toChargerDTO).toList());
        }
        return dto;
    }

    private ChargerDTO toChargerDTO(Charger c) {
        ChargerDTO cd = new ChargerDTO();
        cd.setId(c.getId());
        cd.setName(c.getName());
        cd.setType(c.getType());
        cd.setConnectorType(c.getConnectorType());
        cd.setPowerOutputKw(c.getPowerOutputKw() != null ? c.getPowerOutputKw() : 0);
        cd.setPricePerHour(c.getPricePerHour() != null ? c.getPricePerHour() : 0);
        cd.setTotalSlots(c.getTotalSlots() != null ? c.getTotalSlots() : 0);
        cd.setAvailableSlots(c.getAvailableSlots() != null ? c.getAvailableSlots() : 0);
        return cd;
    }

    public record IncomeResponse(double totalRevenue, List<BookingDTO> finishedBookings) {}
}
