package com.kmarine.fishing.vessel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

public class VesselResponseDto {

    @Getter
    @Builder
    public static class Summary {
        private Long id;
        private String name;
        private VesselType type;
        private VesselStatus status;
        private String region;
        private String departurePort;
        private Integer maxPassengers;
        private Integer pricePerPerson;
        private String thumbnailUrl;
        private Double latitude;
        private Double longitude;
    }

    @Getter
    @Builder
    @NoArgsConstructor // Added for Jackson deserialization
    @AllArgsConstructor // Added for Builder to work correctly
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)  // ← 추가
    public static class Detail implements Serializable {
        private Long id;
        private String ownerName;
        private String name;
        private VesselType type;
        private VesselStatus status;
        private String region;
        private String departurePort;
        private Double latitude;
        private Double longitude;
        private Integer maxPassengers;
        private Integer pricePerPerson;
        private String description;
        private String licenseNumber;
        private Integer buildYear;
        private Double vesselLength;
        private Integer enginePower;
        private List<String> imageUrls;
        private List<String> options;
        private LocalDateTime createdAt;
    }
}