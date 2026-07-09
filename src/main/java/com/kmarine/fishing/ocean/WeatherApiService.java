package com.kmarine.fishing.ocean;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WeatherApiService {

    @Value("${weather.api-key}")
    private String apiKey;

    //@Value("${weather.base-url}")
    @Value("${weather.base-url:https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0}")
    private String baseUrl;

    //private final WebClient webClient = WebClient.builder().build();
    private final WebClient webClient;

    // 기본 생성자
    public WeatherApiService() {
        this.webClient = WebClient.builder().build();
    }
    // 날씨 정보 조회
    public WeatherInfo getWeather(double latitude,
                                   double longitude) {
        try {
            // 위경도 → 격자 변환
            int[] grid = convertToGrid(latitude, longitude);

            LocalDateTime now = LocalDateTime.now();
            String baseDate   = now.format(
                    DateTimeFormatter.ofPattern("yyyyMMdd"));
            String baseTime   = getBaseTime(now);

            Map response = webClient.get()
                    .uri(baseUrl +
                         "/getVilageFcst" +
                         "?ServiceKey=" + apiKey +
                         "&pageNo=1&numOfRows=100" +
                         "&dataType=JSON" +
                         "&base_date=" + baseDate +
                         "&base_time=" + baseTime +
                         "&nx=" + grid[0] +
                         "&ny=" + grid[1])
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseWeatherData(response);
        } catch (Exception e) {
            log.error("날씨 조회 실패: {}", e.getMessage());
            return getDefaultWeather();
        }
    }

    // 날씨 데이터 파싱
    private WeatherInfo parseWeatherData(Map response) {
        WeatherInfo info = new WeatherInfo();
        try {
            Map body  = (Map) response.get("response");
            Map body2 = (Map) body.get("body");
            Map items = (Map) body2.get("items");
            List<Map> itemList = (List<Map>) items.get("item");

            Map<String, String> dataMap = new HashMap<>();
            for (Map item : itemList) {
                dataMap.put((String) item.get("category"),
                            (String) item.get("fcstValue"));
            }

            info.setTemperature(parseDouble(dataMap.get("TMP")));
            info.setWindSpeed(parseDouble(dataMap.get("WSD")));
            info.setWindDirection(dataMap.get("VEC"));
            info.setWaveHeight(parseDouble(dataMap.get("WAV")));
            info.setWeatherCode(dataMap.get("PTY"));
            info.setWeatherDesc(
                getWeatherDesc(dataMap.get("PTY")));
        } catch (Exception e) {
            log.error("날씨 파싱 실패: {}", e.getMessage());
            return getDefaultWeather();
        }
        return info;
    }

    // 날씨 코드 → 설명
    private String getWeatherDesc(String code) {
        if (code == null) return "맑음";
        switch (code) {
            case "0": return "맑음";
            case "1": return "비";
            case "2": return "비/눈";
            case "3": return "눈";
            case "4": return "소나기";
            default:  return "맑음";
        }
    }

    // 기준 시간 계산
    private String getBaseTime(LocalDateTime now) {
        int hour = now.getHour();
        if (hour < 2)  return "2300";
        if (hour < 5)  return "0200";
        if (hour < 8)  return "0500";
        if (hour < 11) return "0800";
        if (hour < 14) return "1100";
        if (hour < 17) return "1400";
        if (hour < 20) return "1700";
        if (hour < 23) return "2000";
        return "2300";
    }

    // 위경도 → 기상청 격자 변환
    private int[] convertToGrid(double lat, double lon) {
        double RE = 6371.00877;
        double GRID = 5.0;
        double SLAT1 = 30.0, SLAT2 = 60.0;
        double OLON = 126.0, OLAT = 38.0;
        double XO = 43, YO = 136;

        double DEGRAD = Math.PI / 180.0;
        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon  = OLON * DEGRAD;
        double olat  = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) /
                    Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) /
             Math.log(sn);

        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;

        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        double ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);

        double theta = lon * DEGRAD - olon;
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;

        int x = (int) (ra * Math.sin(theta) + XO + 0.5);
        int y = (int) (ro - ra * Math.cos(theta) + YO + 0.5);

        return new int[]{x, y};
    }

    private Double parseDouble(Object value) {
        if (value == null) return 0.0;
        try { return Double.parseDouble(value.toString()); }
        catch (Exception e) { return 0.0; }
    }

    private WeatherInfo getDefaultWeather() {
        WeatherInfo info = new WeatherInfo();
        info.setTemperature(20.0);
        info.setWindSpeed(3.0);
        info.setWaveHeight(0.5);
        info.setWeatherDesc("맑음");
        return info;
    }
}