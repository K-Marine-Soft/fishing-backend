package com.kmarine.fishing.fleet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import java.util.List;

public class FleetRequestDto {

    @Getter
    public static class Create {
        @NotBlank(message = "선단명을 입력해주세요")
        private String fleetName;

        @NotBlank(message = "서브도메인을 입력해주세요")
        private String subdomain;

        private String customDomain;

        @NotBlank(message = "지역을 입력해주세요")
        private String region;

        private String port;
        private String address;

        @NotBlank(message = "전화번호를 입력해주세요")
        private String phone;

        private String bankInfo;
        private String departurTime;
        private String returnTime;
        private String description;
        private String themeColor;

        @NotNull(message = "월정액을 입력해주세요")
        private Integer monthlyFee;

        @NotNull(message = "수수료율을 입력해주세요")
        private Integer commissionRate;

        private Integer adultFare;
        private String  fareIncludes;
        private String  fareExcludes;
    }

    @Getter
    public static class Update {
        private String  fleetName;
        private String  phone;
        private String  bankInfo;
        private String  address;
        private String  description;
        private String  themeColor;
        private String  menuConfig;
        private Integer adultFare;
        private String  fareIncludes;
        private String  fareExcludes;
        private String  departurTime;
        private String  returnTime;
        private List<String> imageUrls;  // 메인 롤링 배너 이미지
    }
}