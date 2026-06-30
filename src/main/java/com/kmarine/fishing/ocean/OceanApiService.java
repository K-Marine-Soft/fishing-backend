package com.kmarine.fishing.ocean;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OceanApiService {

    @Value("${ocean.api-key}")
    private String apiKey;

    @Value("${ocean.base-url}")
    private String baseUrl;

    private final WebClient webClient = WebClient.builder()
            .build();

    // 조석 정보 조회 (물때)
    public List<TideInfo> getTideInfo(String obsCode,
                                       LocalDate date) {
        try {
            String dateStr = date.format(
                    DateTimeFormatter.ofPattern("yyyyMMdd"));

            Map response = webClient.get()
                    .uri(baseUrl +
                         "/tideObsPreTab/search.do" +
                         "?ServiceKey=" + apiKey +
                         "&ObsCode=" + obsCode +
                         "&Date=" + dateStr +
                         "&ResultType=json")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseTideData(response);
        } catch (Exception e) {
            log.error("조석 정보 조회 실패: {}", e.getMessage());
            return getDefaultTideInfo(date);
        }
    }

    // 조석 데이터 파싱
    private List<TideInfo> parseTideData(Map response) {
        List<TideInfo> result = new ArrayList<>();
        try {
            Map result1 = (Map) response.get("result");
            List<Map> data = (List<Map>) result1.get("data");

            if (data != null) {
                for (Map item : data) {
                    result.add(TideInfo.builder()
                            .time((String) item.get("tph_time"))
                            .height(parseDouble(
                                item.get("tph_level")))
                            .type(getTideType(
                                (String) item.get("hl_code")))
                            .build());
                }
            }
        } catch (Exception e) {
            log.error("조석 데이터 파싱 실패: {}",
                      e.getMessage());
        }
        return result;
    }

    // 물때 계산 (음력 기반)
    public int calculateTideCycle(LocalDate date) {
        // 음력 날짜 기반 물때 계산
        // 실제로는 음력 변환 라이브러리 사용
        // 여기서는 간단한 근사값 계산
        long daysSinceBase = date.toEpochDay() - 19000;
        int lunarDay = (int) (daysSinceBase % 30) + 1;

        if (lunarDay <= 15) {
            return lunarDay;
        } else {
            return lunarDay - 15;
        }
    }

    // 현재 물 상태 (들물/설물)
    public String getCurrentTideType(
            List<TideInfo> tideInfos, String currentTime) {
        if (tideInfos.isEmpty()) return "확인 불가";

        for (int i = 0; i < tideInfos.size() - 1; i++) {
            TideInfo current = tideInfos.get(i);
            TideInfo next    = tideInfos.get(i + 1);

            if (currentTime.compareTo(current.getTime()) >= 0
             && currentTime.compareTo(next.getTime()) < 0) {
                return "HIGH".equals(current.getType())
                    ? "설물 (썰물)" : "들물 (밀물)";
            }
        }
        return "확인 불가";
    }

    private Double parseDouble(Object value) {
        if (value == null) return 0.0;
        try { return Double.parseDouble(value.toString()); }
        catch (Exception e) { return 0.0; }
    }

    private String getTideType(String code) {
        if ("H".equals(code)) return "HIGH";
        if ("L".equals(code)) return "LOW";
        return "UNKNOWN";
    }

    // API 실패 시 기본 데이터
    private List<TideInfo> getDefaultTideInfo(LocalDate date) {
        List<TideInfo> defaults = new ArrayList<>();
        defaults.add(TideInfo.builder()
                .time("06:00").height(150.0).type("HIGH").build());
        defaults.add(TideInfo.builder()
                .time("12:00").height(50.0).type("LOW").build());
        defaults.add(TideInfo.builder()
                .time("18:00").height(160.0).type("HIGH").build());
        defaults.add(TideInfo.builder()
                .time("00:00").height(40.0).type("LOW").build());
        return defaults;
    }
}