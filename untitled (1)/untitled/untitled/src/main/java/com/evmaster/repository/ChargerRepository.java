package com.evmaster.repository;

import com.evmaster.model.Charger;
import com.evmaster.model.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface ChargerRepository extends JpaRepository<Charger, Long> {

    List<Charger> findByStation(ChargingStation station);
}
