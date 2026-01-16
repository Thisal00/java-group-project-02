package com.evmaster.repository;

import com.evmaster.model.StationImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StationImageRepository extends JpaRepository<StationImage, Long> {
    List<StationImage> findByStation_Id(Long stationId);
}
