package com.kmarine.fishing.ocean;

import com.kmarine.fishing.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ocean")
@RequiredArgsConstructor
public class OceanController {

    private final OceanApiService   oceanApiService;
    private final WeatherApiService weatherApiService;

    // 통합 해양 정보 조회
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOceanInfo(
            @RequestParam("latitude")  double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam(name = "obsCode",
                         defaultValue = "DT_0001")
                         String obsCode,
            @RequestParam(name = "date",
                         required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                         LocalDate date) {

        if (date == null) date = LocalDate.now();

        // 조석 정보
        List<TideInfo> tideInfos =
                oceanApiService.getTideInfo(obsCode, date);

        // 물때 계산
        int tideCycle =
                oceanApiService.calculateTideCycle(date);

        // 현재 시간 물 상태
        String currentTime = java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter
                        .ofPattern("HH:mm"));
        String tideType = oceanApiService.getCurrentTideType(
                tideInfos, currentTime);

        // 날씨 정보
        WeatherInfo weather =
                weatherApiService.getWeather(latitude, longitude);

        // 통합 응답
        Map<String, Object> result = new HashMap<>();
        result.put("date",       date.toString());
        result.put("tideCycle",  tideCycle);
        result.put("tideType",   tideType);
        result.put("tideInfos",  tideInfos);
        result.put("weather",    weather);
        result.put("currentTime", currentTime);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // 물때 정보만 조회
    @GetMapping("/tide")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTide(
            @RequestParam(name = "obsCode",
                         defaultValue = "DT_0001")
                         String obsCode,
            @RequestParam(name = "date",
                         required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                         LocalDate date) {

        if (date == null) date = LocalDate.now();

        List<TideInfo> tideInfos =
                oceanApiService.getTideInfo(obsCode, date);
        int tideCycle =
                oceanApiService.calculateTideCycle(date);

        Map<String, Object> result = new HashMap<>();
        result.put("date",      date.toString());
        result.put("tideCycle", tideCycle);
        result.put("tideInfos", tideInfos);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // 날씨만 조회
    @GetMapping("/weather")
    public ResponseEntity<ApiResponse<WeatherInfo>> getWeather(
            @RequestParam("latitude")  double latitude,
            @RequestParam("longitude") double longitude) {

        WeatherInfo weather =
                weatherApiService.getWeather(latitude, longitude);
        return ResponseEntity.ok(ApiResponse.ok(weather));
    }
}