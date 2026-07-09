package com.kmarine.fishing.fleet;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FleetRepository
        extends JpaRepository<Fleet, Long> {

    Optional<Fleet> findBySubdomain(String subdomain);
    Optional<Fleet> findByCustomDomain(String customDomain);
    List<Fleet> findByStatus(FleetStatus status);
    boolean existsBySubdomain(String subdomain);
}