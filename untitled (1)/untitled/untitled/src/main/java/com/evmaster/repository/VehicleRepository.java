package com.evmaster.repository;

import com.evmaster.model.User;
import com.evmaster.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Vehicle operations.
 */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    //  list vehicles for logged owner
    List<Vehicle> findByOwner(User owner);

    // secure find (prevents other users accessing)
    Optional<Vehicle> findByIdAndOwner(Long id, User owner);

    // secure delete (if you want direct delete query style)
    void deleteByIdAndOwner(Long id, User owner);

    // optional: check duplicate register number per owner
    boolean existsByOwnerAndRegisterNumber(User owner, String registerNumber);

    boolean existsByIdAndOwner(Long id, User owner);
}
