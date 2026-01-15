package com.evmaster.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "REVIEWS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //  EV OWNER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnoreProperties({"password", "roles", "hibernateLazyInitializer", "handler"})
    private User evOwner;


    // BOOKING
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Booking booking;

  
    // STATION
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    @JsonIgnoreProperties({"chargers", "manager", "hibernateLazyInitializer", "handler"})
    private ChargingStation station;


    //REVIEW DATA
    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "review_date", nullable = false)
    private LocalDateTime reviewDate = LocalDateTime.now();


    //ADMIN APPROVAL
    @Column(nullable = false)
    private Boolean approved = false;
}
