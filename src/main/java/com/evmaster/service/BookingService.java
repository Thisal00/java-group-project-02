package com.evmaster.service;

import com.evmaster.model.Booking;
import com.evmaster.model.Charger;
import com.evmaster.model.User;
import com.evmaster.model.Vehicle;
import com.evmaster.repository.BookingRepository;
import com.evmaster.repository.ChargerRepository;
import com.evmaster.repository.VehicleRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ChargerRepository chargerRepository;
    private final VehicleRepository vehicleRepository;

    public BookingService(BookingRepository bookingRepository,
                          ChargerRepository chargerRepository,
                          VehicleRepository vehicleRepository) {
        this.bookingRepository = bookingRepository;
        this.chargerRepository = chargerRepository;
        this.vehicleRepository = vehicleRepository;
    }


    // GET BOOKINGS


    @Transactional(readOnly = true)
    public List<Booking> getBookingsByOwner(User owner) {
        return bookingRepository.findByEvOwnerOrderByStartTimeDesc(owner);
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByManager(User manager) {
        return bookingRepository.findAllForManager(manager);
    }


    // CREATE BOOKING


    @Transactional
    public Booking createBookingOrThrow(Booking incoming, User owner) {

        if (incoming == null
                || incoming.getCharger() == null || incoming.getCharger().getId() == null
                || incoming.getVehicle() == null || incoming.getVehicle().getId() == null) {
            throw new IllegalArgumentException("Invalid payload");
        }

        int hours = Math.max(1, incoming.getTotalHours() == null ? 1 : incoming.getTotalHours());

        Charger charger = chargerRepository.findById(incoming.getCharger().getId())
                .orElseThrow(() -> new IllegalArgumentException("Charger not found"));

        Vehicle vehicle = vehicleRepository.findById(incoming.getVehicle().getId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        if (!Objects.equals(vehicle.getOwner().getId(), owner.getId())) {
            throw new IllegalArgumentException("Vehicle does not belong to user");
        }

        if (charger.getAvailableSlots() == null || charger.getAvailableSlots() <= 0) {
            throw new IllegalArgumentException("No slots available");
        }

        double pricePerHour = charger.getPricePerHour() == null ? 0.0 : charger.getPricePerHour();
        double totalPrice = pricePerHour * hours;

        Booking b = new Booking();
        b.setEvOwner(owner);
        b.setCharger(charger);
        b.setVehicle(vehicle);
        b.setTotalHours(hours);
        b.setTotalPrice(totalPrice);

        LocalDateTime start = LocalDateTime.now();
        b.setStartTime(start);
        b.setEndTime(start.plusHours(hours));

        b.setStatus("PENDING");
        b.setPaid(false);

        return bookingRepository.save(b);
    }


    // CONFIRM PAYMENT


    @Transactional
    public void confirmPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.isPaid()) return;
        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) return;

        booking.setPaid(true);
        booking.setStatus("CONFIRMED");

        Charger charger = booking.getCharger();
        if (charger != null && charger.getAvailableSlots() != null && charger.getAvailableSlots() > 0) {
            charger.setAvailableSlots(charger.getAvailableSlots() - 1);
            chargerRepository.save(charger);
        }

        bookingRepository.save(booking);
    }


    // CANCEL BY MANAGER

    @Transactional
    public boolean cancelBookingByManager(Long bookingId, User manager) {

        Booking b = bookingRepository.findById(bookingId).orElse(null);
        if (b == null) return false;

        if ("CANCELLED".equalsIgnoreCase(b.getStatus())) return false;
        if ("FINISHED".equalsIgnoreCase(b.getStatus())) return false;

        if (!Objects.equals(b.getCharger().getStation().getManager().getId(), manager.getId())) {
            return false;
        }

        releaseSlotIfNeeded(b);
        b.setStatus("CANCELLED");
        bookingRepository.save(b);
        return true;
    }


    //  FINISH BY MANAGER


    @Transactional
    public boolean finishBookingByManager(Long bookingId, Long managerId) {

        Booking b = bookingRepository.findById(bookingId).orElse(null);
        if (b == null) return false;

        if (!"CONFIRMED".equalsIgnoreCase(b.getStatus())) return false;

        // ensure paid
        b.setPaid(true);
        b.setStatus("FINISHED");

        Charger charger = b.getCharger();
        if (charger != null) {
            Integer slots = charger.getAvailableSlots();
            charger.setAvailableSlots((slots == null ? 0 : slots) + 1);
            chargerRepository.save(charger);
        }

        bookingRepository.save(b);
        return true;
    }


    // AUTO FINISH


    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoFinishBookings() {

        List<Booking> all = bookingRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Booking b : all) {
            if ("CONFIRMED".equalsIgnoreCase(b.getStatus()) && b.getEndTime() != null) {
                if (now.isAfter(b.getEndTime())) {
                    b.setPaid(true);
                    b.setStatus("FINISHED");

                    Charger charger = b.getCharger();
                    if (charger != null) {
                        Integer slots = charger.getAvailableSlots();
                        charger.setAvailableSlots((slots == null ? 0 : slots) + 1);
                        chargerRepository.save(charger);
                    }

                    bookingRepository.save(b);
                }
            }
        }
    }


    // UTIL


    private void releaseSlotIfNeeded(Booking b) {
        if (b.isPaid()) {
            Charger charger = b.getCharger();
            if (charger != null) {
                Integer slots = charger.getAvailableSlots();
                charger.setAvailableSlots((slots == null ? 0 : slots) + 1);
                chargerRepository.save(charger);
            }
        }
    }


    //  INCOME (PAID + FINISHED)


    @Transactional(readOnly = true)
    public List<Booking> getPaidFinishedBookingsByManager(User manager) {
        return bookingRepository.findPaidFinishedBookings(manager); //  CORRECT METHOD
    }


    // OWNER CANCEL


    @Transactional
    public boolean cancelBooking(Long bookingId, User owner) {
        Optional<Booking> opt = bookingRepository.findByIdAndEvOwner(bookingId, owner);
        if (opt.isEmpty()) return false;

        Booking b = opt.get();
        if ("CANCELLED".equalsIgnoreCase(b.getStatus())) return false;
        if ("FINISHED".equalsIgnoreCase(b.getStatus())) return false;

        releaseSlotIfNeeded(b);
        b.setStatus("CANCELLED");
        bookingRepository.save(b);
        return true;
    }
}
