package com.evmaster.service;

import com.evmaster.model.User;
import com.evmaster.model.Vehicle;
import com.evmaster.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Business logic for managing EV Owner's vehicles.
 */
@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    /**
     * Finds all vehicles registered by the given owner.
     */
    @Transactional(readOnly = true)
    public List<Vehicle> getAllVehiclesByOwner(User owner) {
        return vehicleRepository.findByOwner(owner);
    }

    /**
     * Saves a new vehicle or updates an existing one.
     *  Owner always set server-side
     */
    @Transactional
    public Vehicle saveVehicle(Vehicle vehicle, User owner) {
        vehicle.setOwner(owner);
        return vehicleRepository.save(vehicle);
    }

    /**
     * Deletes a vehicle, ensuring the current owner is authorized.
     */
    @Transactional
    public boolean deleteVehicle(Long vehicleId, User owner) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findByIdAndOwner(vehicleId, owner);

        if (vehicleOpt.isPresent()) {
            vehicleRepository.delete(vehicleOpt.get());
            return true;
        }
        return false;
    }

    /**
     *  Used by controller: check update permission
     */
    @Transactional(readOnly = true)
    public boolean isVehicleOwnedBy(Long id, User owner) {
        return vehicleRepository.existsByIdAndOwner(id, owner);
    }

    /**
     *  Get vehicle only if it belongs to this owner
     */
    @Transactional(readOnly = true)
    public Optional<Vehicle> findByIdAndOwner(Long vehicleId, User owner) {
        return vehicleRepository.findByIdAndOwner(vehicleId, owner);
    }
}
