package com.kmarine.fishing.fleet;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FleetAdminApplicationResponseDto {
    private Long id;
    private Long fleetId;
    private String fleetName;
    private Long applicantId;
    private String applicantName;
    private String applicantEmail;
    private FleetAdminApplicationStatus status;
    private LocalDateTime createdAt;
}
