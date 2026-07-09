package com.kmarine.fishing.fleet;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FleetAdminMappingRepository
        extends JpaRepository<FleetAdminMapping, Long> {

    Optional<FleetAdminMapping> findByUserId(Long userId);
    Optional<FleetAdminMapping> findByFleetId(Long fleetId);
}