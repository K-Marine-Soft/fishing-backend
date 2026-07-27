package com.kmarine.fishing.fleet;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

public class FleetResponseDto {

    // 선단 기본 정보
    @Getter
    @Builder
    public static class Info {
        private Long   id;
        private String fleetName;
        private String subdomain;
        private String customDomain;
        private String region;
        private String port;
        private String address;
        private String phone;
        private String bankInfo;
        private String departurTime;
        private String returnTime;
        private String description;
        private String logoUrl;
        private String themeColor;
        private String menuConfig;
        private Integer adultFare;
        private String  fareIncludes;
        private String  fareExcludes;
        private FleetStatus status;
        private LocalDateTime createdAt;
        private Long   requestedById;
        private String requestedByName;
        private String requestedByEmail;
        private List<String> imageUrls;
    }

    // 통합 플랫폼용 간략 정보
    @Getter
    @Builder
    public static class Summary {
        private Long   id;
        private String fleetName;
        private String subdomain;
        private String region;
        private String port;
        private String phone;
        private String themeColor;
        private String logoUrl;
        private Integer vesselCount;  // 선박 수
        private FleetStatus status;
    }
}