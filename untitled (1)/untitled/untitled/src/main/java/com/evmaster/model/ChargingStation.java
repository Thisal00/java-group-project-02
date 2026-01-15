package com.evmaster.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CHARGING_STATIONS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ChargingStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Manager
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    @JsonIgnoreProperties({"password", "stations", "hibernateLazyInitializer", "handler"})
    private User manager;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 800)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String district;
    private String province;
    private String phoneNumber;

    @Column(length = 1200)
    private String description;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    private String status;

    // Chargers
    @OneToMany(
            mappedBy = "station",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonIgnoreProperties({"station", "hibernateLazyInitializer", "handler"})
    private List<Charger> chargers = new ArrayList<>();

    // Helper methods
    public void addCharger(Charger charger) {
        if (charger == null) return;
        chargers.add(charger);
        charger.setStation(this);
    }

    public void removeCharger(Charger charger) {
        if (charger == null) return;
        chargers.remove(charger);
        charger.setStation(null);
    }
}
