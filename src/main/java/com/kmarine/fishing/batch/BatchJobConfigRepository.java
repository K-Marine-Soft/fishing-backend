package com.kmarine.fishing.batch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BatchJobConfigRepository extends JpaRepository<BatchJobConfig, Long> {
    Optional<BatchJobConfig> findByJobKey(BatchJobKey jobKey);
}
