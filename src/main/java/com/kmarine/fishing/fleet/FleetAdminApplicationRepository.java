package com.kmarine.fishing.fleet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FleetAdminApplicationRepository
        extends JpaRepository<FleetAdminApplication, Long> {

    List<FleetAdminApplication> findByFleet_IdAndStatus(
            Long fleetId, FleetAdminApplicationStatus status);

    Optional<FleetAdminApplication> findByFleet_IdAndApplicant_IdAndStatus(
            Long fleetId, Long applicantId, FleetAdminApplicationStatus status);
}
