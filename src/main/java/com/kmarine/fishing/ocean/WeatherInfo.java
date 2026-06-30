package com.kmarine.fishing.ocean;

import lombok.Data;

@Data
public class WeatherInfo {
    private Double temperature;   // 기온
    private Double windSpeed;     // 풍속 (m/s)
    private String windDirection; // 풍향
    private Double waveHeight;    // 파고 (m)
    private String weatherCode;   // 날씨 코드
    private String weatherDesc;   // 날씨 설명
}