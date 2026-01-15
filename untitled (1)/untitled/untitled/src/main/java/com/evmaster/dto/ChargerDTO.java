package com.evmaster.dto;

public class ChargerDTO {

    private Long id;
    private String name;
    private String type;
    private String connectorType;
    private double powerOutputKw;
    private double pricePerHour;

    private int totalSlots;

    private int availableSlots;

    // GETTERS & SETTERS

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getConnectorType() {
        return connectorType;
    }
    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public double getPowerOutputKw() {
        return powerOutputKw;
    }
    public void setPowerOutputKw(double powerOutputKw) {
        this.powerOutputKw = powerOutputKw;
    }

    public double getPricePerHour() {
        return pricePerHour;
    }
    public void setPricePerHour(double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public int getTotalSlots() {
        return totalSlots;
    }
    public void setTotalSlots(int totalSlots) {
        this.totalSlots = totalSlots;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }
    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }
}
