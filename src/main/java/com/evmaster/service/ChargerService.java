package com.evmaster.service;

import com.evmaster.dto.ChargerDTO;
import com.evmaster.model.Charger;
import com.evmaster.model.ChargingStation;
import com.evmaster.model.User;
import com.evmaster.repository.ChargerRepository;
import com.evmaster.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ChargerService {

    @Autowired
    private ChargerRepository chargerRepository;

    @Autowired
    private StationRepository stationRepository;


    // GET CHARGERS

    @Transactional(readOnly = true)
    public List<Charger> getChargersByStation(ChargingStation station) {
        return chargerRepository.findByStation(station);
    }


    // SAVE CHARGER

    @Transactional
    public Charger saveCharger(Charger charger, ChargingStation station) {
        charger.setStation(station);

        Integer total = charger.getTotalSlots();
        Integer avail = charger.getAvailableSlots();

        if (total == null || total < 0) total = 0;
        if (avail == null || avail < 0 || avail > total) avail = total;

        charger.setTotalSlots(total);
        charger.setAvailableSlots(avail);
        charger.setIsAvailable(avail > 0);

        return chargerRepository.save(charger);
    }


    // ADD CHARGER TO STATION (FIXED)

    @Transactional
    public boolean addChargerToStation(Long stationId, ChargerDTO dto, User manager) {

        Optional<ChargingStation> stationOpt =
                stationRepository.findByIdAndManagerWithChargers(stationId, manager);

        if (stationOpt.isEmpty()) {
            return false; // ❌ Not your station or not found
        }

        ChargingStation station = stationOpt.get();

        Charger c = new Charger();
        c.setName(dto.getName());
        c.setType(dto.getType());
        c.setConnectorType(dto.getConnectorType());
        c.setPowerOutputKw(dto.getPowerOutputKw());
        c.setPricePerHour(dto.getPricePerHour());
        c.setTotalSlots(dto.getTotalSlots());
        c.setAvailableSlots(dto.getAvailableSlots());

        // vERY IMPORTANT: link charger to station
        c.setStation(station);

        // optional but good
        station.getChargers().add(c);

        chargerRepository.save(c);

        return true;
    }


    // THIS IS THE MISSING FIX

    @Transactional(readOnly = true)
    public List<Charger> getChargersByStationId(Long stationId, User manager) {

        ChargingStation station = stationRepository
                .findByIdAndManagerWithChargers(stationId, manager)
                .orElse(null);

        if (station == null) {
            return List.of(); // not your station or not found
        }

        // RETURN REAL DATA FROM DB
        return chargerRepository.findByStation(station);
    }

    // FIND

    @Transactional(readOnly = true)
    public Optional<Charger> findChargerByIdAndStation(Long chargerId, ChargingStation station) {
        return chargerRepository.findById(chargerId)
                .filter(charger -> charger.getStation() != null
                        && station != null
                        && charger.getStation().getId() != null
                        && station.getId() != null
                        && charger.getStation().getId().equals(station.getId()));
    }

    @Transactional
    public boolean deleteCharger(Long chargerId, ChargingStation station) {
        Optional<Charger> chargerOpt = findChargerByIdAndStation(chargerId, station);
        if (chargerOpt.isPresent()) {
            chargerRepository.deleteById(chargerId);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public Optional<Charger> findById(Long chargerId) {
        return chargerRepository.findById(chargerId);
    }
}
