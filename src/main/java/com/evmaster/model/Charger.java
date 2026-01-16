package com.evmaster.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "CHARGERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Charger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150)
    private String name;

    @Column(length = 50)
    private String type; // AC / DC

    @Column(name = "connector_type", length = 50)
    private String connectorType; // Type2 / CCS / CHAdeMO

    @Column(name = "power_output_kw")
    private Double powerOutputKw;

    @Column(name = "price_per_hour")
    private Double pricePerHour;

    @Column(name = "total_slots")
    private Integer totalSlots;

    @Column(name = "available_slots")
    private Integer availableSlots;

    @Column(name = "is_available")
    private Boolean isAvailable;

    // ============================
    // 🔌 STATION RELATION
    // ============================
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    @JsonIgnoreProperties({"chargers", "manager", "hibernateLazyInitializer", "handler"})
    private ChargingStation station;


    //  AUTO NORMALIZE

    @PrePersist
    @PreUpdate
    public void normalizeDefaults() {

        if (totalSlots == null || totalSlots < 0) {
            totalSlots = 0;
        }

        if (availableSlots == null || availableSlots < 0) {
            availableSlots = totalSlots;
        }

        if (availableSlots > totalSlots) {
            availableSlots = totalSlots;
        }

        if (isAvailable == null) {
            isAvailable = availableSlots > 0;
        } else if (availableSlots == 0) {
            isAvailable = false;
        }
    }
}
