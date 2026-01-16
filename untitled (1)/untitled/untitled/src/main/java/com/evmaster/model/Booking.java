package com.evmaster.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "BOOKINGS",
        indexes = {
                @Index(name = "idx_booking_status", columnList = "status"),
                @Index(name = "idx_booking_paid", columnList = "paid"),
                @Index(name = "idx_booking_start_time", columnList = "start_time")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================
    // 👤 EV OWNER
    // ==========================
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnore   // prevent infinite JSON loop
    private User evOwner;


    //CHARGER

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "charger_id", nullable = false)
    @JsonIgnore
    private Charger charger;


    // VEHICLE

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonIgnore
    private Vehicle vehicle;


    //  TIME

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;


    // PRICE

    @Column(name = "total_hours", nullable = false)
    private Integer totalHours;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;


    //STATUS

    @Column(nullable = false, length = 20)
    private String status;


    // PAYMENT

    @Column(nullable = false)
    private boolean paid = false;

    @Column(name = "payment_ref", length = 100)
    private String paymentRef;


    //  API SAFE FIELDS (THIS FIXES YOUR UI)

    @Transient
    public String getOwnerName() {
        if (evOwner == null) return "N/A";
        if (evOwner.getFullName() != null && !evOwner.getFullName().isEmpty())
            return evOwner.getFullName();
        return evOwner.getEmail();
    }

    @Transient
    public String getChargerName() {
        return charger == null ? "N/A" : charger.getName();
    }

    @Transient
    public String getStationName() {
        if (charger == null || charger.getStation() == null) return "N/A";
        return charger.getStation().getName();
    }

    @Transient
    public String getVehicleName() {
        return vehicle == null ? "N/A" : vehicle.getModel();
    }


    //  AUTO NORMALIZE

    @PrePersist
    @PreUpdate
    public void normalize() {

        if (status == null || status.trim().isEmpty()) {
            status = "PENDING";
        }

        if (totalHours == null || totalHours <= 0) {
            totalHours = 1;
        }

        if (totalPrice == null || totalPrice < 0) {
            totalPrice = 0.0;
        }

        if (startTime == null) {
            startTime = LocalDateTime.now();
        }

        if (endTime == null || endTime.isBefore(startTime)) {
            endTime = startTime.plusHours(totalHours);
        }
    }
}
