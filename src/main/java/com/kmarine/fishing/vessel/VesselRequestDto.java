package com.kmarine.fishing.vessel;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

public class VesselRequestDto {

    @Getter
    public static class Register {
        @NotBlank(message = "선박명을 입력해주세요")
        private String name;

        @NotNull(message = "선박 종류를 선택해주세요")
        private VesselType type;

        @NotBlank(message = "지역을 입력해주세요")
        private String region;

        private String departurePort;
        private Double latitude;
        private Double longitude;

        @NotNull(message = "최대 승선 인원을 입력해주세요")
        @Min(value = 1, message = "최소 1명 이상이어야 합니다")
        private Integer maxPassengers;

        @NotNull(message = "가격을 입력해주세요")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다")
        private Integer pricePerPerson;

        private String description;
        private String licenseNumber;
        private Integer buildYear;
        private Double vesselLength;
        private Integer enginePower;
        private List<String> options;   // 편의시설 목록
        private List<String> imageUrls; // 선박 이미지 (첫 번째가 대표 이미지)
    }

    @Getter
    public static class Update {
        @NotBlank(message = "선박명을 입력해주세요")
        private String name;

        private VesselType type;
        private String region;
        private String departurePort;
        private Double latitude;
        private Double longitude;

        @NotNull(message = "최대 승선 인원을 입력해주세요")
        @Min(value = 1, message = "최소 1명 이상이어야 합니다")
        private Integer maxPassengers;

        @NotNull(message = "가격을 입력해주세요")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다")
        private Integer pricePerPerson;

        private String description;
        private String licenseNumber;
        private Integer buildYear;
        private Double vesselLength;
        private Integer enginePower;
        private List<String> options;
        private List<String> imageUrls; // 선박 이미지 (첫 번째가 대표 이미지)
    }

    @Getter
    @Setter
    public static class Search {
        private Long        fleetId;
        private String     region;
        private VesselType type;
        private Integer    minPassengers;
        private Integer    maxPrice;
        private Integer    minPrice;      // ← 추가
        private String     option;        // 편의시설 필터
        private String     keyword;       // 선박명 검색 ← 추가
        private String     sortBy;        // 정렬 기준 ← 추가
    }
}