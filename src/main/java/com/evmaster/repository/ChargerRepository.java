package com.evmaster.repository;

import com.evmaster.model.Charger;
import com.evmaster.model.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository for Charger operations.
 */
public interface ChargerRepository extends JpaRepository<Charger, Long> {

    /**
     * Find all chargers belonging to a specific station.
     */
    List<Charger> findByStation(ChargingStation station);
}