package com.evmaster.repository;

import com.evmaster.model.ChargingStation;
import com.evmaster.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StationRepository extends JpaRepository<ChargingStation, Long> {


    List<ChargingStation> findByManager(User manager);

    Optional<ChargingStation> findByIdAndManager(Long id, User manager);

    @Query("select distinct s from ChargingStation s left join fetch s.chargers")
    List<ChargingStation> findAllWithChargers();

    @Query("select distinct s from ChargingStation s left join fetch s.chargers where s.id = :id")
    Optional<ChargingStation> findByIdWithChargers(@Param("id") Long id);

    @Query("select distinct s from ChargingStation s left join fetch s.chargers where s.manager = :manager")
    List<ChargingStation> findByManagerWithChargers(@Param("manager") User manager);

    @Query("""
        select distinct s 
        from ChargingStation s 
        left join fetch s.chargers 
        where s.id = :id and s.manager = :manager
    """)
    Optional<ChargingStation> findByIdAndManagerWithChargers(
            @Param("id") Long id,
            @Param("manager") User manager
    );

    @EntityGraph(attributePaths = {"chargers"})
    Optional<ChargingStation> findWithChargersById(Long id);
}
