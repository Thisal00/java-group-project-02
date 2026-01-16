package com.evmaster.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "EV_VEHICLES",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vehicle_register_number", columnNames = {"register_number"})
        },
        indexes = {
                @Index(name = "idx_vehicle_owner", columnList = "owner_id"),
                @Index(name = "idx_vehicle_connector", columnList = "connector_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Foreign key linking the vehicle to its owner
     *  IMPORTANT: avoid recursion + lazy JSON issues
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnore
    private User owner;

    @Column(name = "car_name", nullable = false, length = 120)
    private String name; // e.g., Tesla Model 3

    @Column(name = "register_number", nullable = false, length = 60)
    private String registerNumber;

    @Column(length = 80)
    private String brand;

    @Column(length = 80)
    private String model;

    private Integer year;

    @Column(name = "connector_type", nullable = false, length = 50)
    private String connectorType; // Type2 / CCS / CHAdeMO

    //  Normalize data before save
    @PrePersist
    @PreUpdate
    private void normalize() {

        // trim strings
        if (name != null) name = name.trim();
        if (registerNumber != null) registerNumber = registerNumber.trim();
        if (brand != null) brand = brand.trim();
        if (model != null) model = model.trim();
        if (connectorType != null) connectorType = connectorType.trim();

        // normalize connector type for matching
        // (frontend sometimes sends "ccs" lower-case)
        if (connectorType != null) {
            String x = connectorType.replace(" ", "").toUpperCase();
            if (x.equals("CCS") || x.equals("CCSCOMBO") || x.equals("CCS/COMBO")) connectorType = "CCS";
            else if (x.equals("TYPE2") || x.equals("TYPE-2")) connectorType = "Type2";
            else if (x.equals("CHADEMO")) connectorType = "CHAdeMO";
        }
    }
}
