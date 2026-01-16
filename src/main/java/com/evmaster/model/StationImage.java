package com.evmaster.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "station_images")
@Data
public class StationImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    // Use the ChargingStation entity if your station repository returns ChargingStation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id") // keep column name 'station_id' so DB compatibility remains
    private ChargingStation station;
}
