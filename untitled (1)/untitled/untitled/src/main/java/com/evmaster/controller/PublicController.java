package com.evmaster.controller;

import com.evmaster.dto.ChargerDTO;
import com.evmaster.dto.ChargingStationDTO;
import com.evmaster.model.Charger;
import com.evmaster.model.ChargingStation;
import com.evmaster.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

//ontroller providing public

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Autowired
    private StationService stationService;
    
//Return stations
  
    @GetMapping("/stations")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ChargingStationDTO>> getAllStationsForMap() {

        List<ChargingStation> stations = stationService.getAllPublicStationsWithChargers();

        List<ChargingStationDTO> dtoList = stations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }


    // DTO MAPPERS


    private ChargingStationDTO toDTO(ChargingStation s) {
        ChargingStationDTO dto = new ChargingStationDTO();

        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setAddress(s.getAddress());

        dto.setLatitude(s.getLatitude() != null ? s.getLatitude() : 0);
        dto.setLongitude(s.getLongitude() != null ? s.getLongitude() : 0);

        dto.setOpenTime(s.getOpenTime() != null ? s.getOpenTime().toString() : null);
        dto.setCloseTime(s.getCloseTime() != null ? s.getCloseTime().toString() : null);

        dto.setStatus(s.getStatus());
        dto.setRating(4.0); //tdo
        
        if (s.getChargers() != null) {
            dto.setChargers(
                    s.getChargers().stream()
                            .map(this::toDTO)
                            .collect(Collectors.toList())
            );
        } else {
            dto.setChargers(Collections.emptyList());
        }

        return dto;
    }

    private ChargerDTO toDTO(Charger c) {
        ChargerDTO dto = new ChargerDTO();

        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setType(c.getType());
        dto.setConnectorType(c.getConnectorType());

        dto.setPowerOutputKw(c.getPowerOutputKw() != null ? c.getPowerOutputKw() : 0.0);
        dto.setPricePerHour(c.getPricePerHour() != null ? c.getPricePerHour() : 0.0);

        dto.setTotalSlots(c.getTotalSlots() != null ? c.getTotalSlots() : 0);
        dto.setAvailableSlots(c.getAvailableSlots() != null ? c.getAvailableSlots() : 0);

        return dto;
    }
}

