package com.evmaster.dto;

import com.evmaster.model.Booking;
import com.evmaster.model.Charger;
import com.evmaster.model.ChargingStation;
import com.evmaster.model.User;
import com.evmaster.model.Vehicle;

public class BookingDTO {

    private final Long id;
    private final String status;
    private final boolean paid;   
    private final Integer totalHours;
    private final Double totalPrice;
    private final String startTime;
    private final String endTime;
    private final Long stationId;
    private final String stationName;
    private final Long chargerId;
    private final String chargerName;
    private final String connectorType;
    private final Long ownerId;
    private final String ownerEmail;
    private final String ownerName;
    private final Long vehicleId;
    private final String vehicleName;
    private final String vehicleReg;

    public BookingDTO(
            Long id,
            String status,
            boolean paid,
            Integer totalHours,
            Double totalPrice,
            String startTime,
            String endTime,
            Long stationId,
            String stationName,
            Long chargerId,
            String chargerName,
            String connectorType,
            Long ownerId,
            String ownerEmail,
            String ownerName,
            Long vehicleId,
            String vehicleName,
            String vehicleReg
    ) {
        this.id = id;
        this.status = status;
        this.paid = paid;
        this.totalHours = totalHours;
        this.totalPrice = totalPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.stationId = stationId;
        this.stationName = stationName;
        this.chargerId = chargerId;
        this.chargerName = chargerName;
        this.connectorType = connectorType;
        this.ownerId = ownerId;
        this.ownerEmail = ownerEmail;
        this.ownerName = ownerName;
        this.vehicleId = vehicleId;
        this.vehicleName = vehicleName;
        this.vehicleReg = vehicleReg;
    }

    // ---- getters ----
    public Long getId() { return id; }
    public String getStatus() { return status; }
    public boolean isPaid() { return paid; }    // important
    public boolean getPaid() { return paid; }   // for JSON
    public Integer getTotalHours() { return totalHours; }
    public Double getTotalPrice() { return totalPrice; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public Long getStationId() { return stationId; }
    public String getStationName() { return stationName; }
    public Long getChargerId() { return chargerId; }
    public String getChargerName() { return chargerName; }
    public String getConnectorType() { return connectorType; }
    public Long getOwnerId() { return ownerId; }
    public String getOwnerEmail() { return ownerEmail; }
    public String getOwnerName() { return ownerName; }
    public Long getVehicleId() { return vehicleId; }
    public String getVehicleName() { return vehicleName; }
    public String getVehicleReg() { return vehicleReg; }

    // ---- factory ----
    public static BookingDTO from(Booking b) {
        if (b == null) return null;

        Charger charger = b.getCharger();
        ChargingStation station = charger != null ? charger.getStation() : null;
        User owner = b.getEvOwner();
        Vehicle vehicle = b.getVehicle();

        return new BookingDTO(
                b.getId(),
                b.getStatus(),
                b.isPaid(),   // REAL VALUE FROM ENTITY
                b.getTotalHours(),
                b.getTotalPrice(),
                b.getStartTime() != null ? b.getStartTime().toString() : null,
                b.getEndTime() != null ? b.getEndTime().toString() : null,
                station != null ? station.getId() : null,
                station != null ? station.getName() : null,
                charger != null ? charger.getId() : null,
                charger != null ? charger.getName() : null,
                charger != null ? charger.getConnectorType() : null,
                owner != null ? owner.getId() : null,
                owner != null ? owner.getEmail() : null,
                owner != null ? owner.getFullName() : null,
                vehicle != null ? vehicle.getId() : null,
                vehicle != null ? vehicle.getName() : null,
                vehicle != null ? vehicle.getRegisterNumber() : null
        );
    }
}
