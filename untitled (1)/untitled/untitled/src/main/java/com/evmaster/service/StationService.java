package com.evmaster.service;

import com.evmaster.model.ChargingStation;
import com.evmaster.model.User;
import com.evmaster.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StationService {

    @Autowired
    private StationRepository stationRepository;

    // PUBLIC - GET STATIONS
    @Transactional(readOnly = true)
    public List<ChargingStation> getAllPublicStationsWithChargers() {
        return stationRepository.findAllWithChargers();
    }

    @Transactional(readOnly = true)
    public Optional<ChargingStation> getPublicStationByIdWithChargers(Long stationId) {
        return stationRepository.findByIdWithChargers(stationId);
    }

    //  MANAGER - GET STATIONS

    @Transactional(readOnly = true)
    public List<ChargingStation> getAllStationsByManager(User manager) {
        return stationRepository.findByManagerWithChargers(manager);
    }

    @Transactional(readOnly = true)
    public List<ChargingStation> getAllStationsByManagerWithChargers(User manager) {
        return stationRepository.findByManagerWithChargers(manager);
    }

    @Transactional(readOnly = true)
    public List<ChargingStation> getAllStationsByManagerNoChargers(User manager) {
        return stationRepository.findByManager(manager);
    }

    //  SAVE UPDATE STATION

    @Transactional
    public ChargingStation saveStation(ChargingStation station, User manager) {
        station.setManager(manager);

        // Bind chargers to station
        if (station.getChargers() != null) {
            station.getChargers().forEach(c -> c.setStation(station));
        }

        return stationRepository.save(station);
    }

    //  FIND STATION (MANAGER)

    @Transactional(readOnly = true)
    public Optional<ChargingStation> findStationByIdAndManager(Long stationId, User manager) {
        return stationRepository.findByIdAndManagerWithChargers(stationId, manager);
    }

    @Transactional(readOnly = true)
    public Optional<ChargingStation> findStationByIdAndManagerWithChargers(Long stationId, User manager) {
        return stationRepository.findByIdAndManagerWithChargers(stationId, manager);
    }

    @Transactional(readOnly = true)
    public Optional<ChargingStation> findStationByIdAndManagerNoChargers(Long stationId, User manager) {
        return stationRepository.findByIdAndManager(stationId, manager);
    }

    //  DELETE STATION

    @Transactional
    public boolean deleteStation(Long stationId, User manager) {
        Optional<ChargingStation> stationOpt =
                stationRepository.findByIdAndManagerWithChargers(stationId, manager);

        if (stationOpt.isPresent()) {
            stationRepository.delete(stationOpt.get());
            return true;
        }
        return false;
    }
}
