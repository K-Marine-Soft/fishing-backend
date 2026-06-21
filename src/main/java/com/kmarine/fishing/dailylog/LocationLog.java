package com.kmarine.fishing.dailylog;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "location_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class LocationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_log_id", nullable = false)
    private DailyLog dailyLog;

    @Column(nullable = false)
    private Integer sequence;           // 이동 순서 1,2,3...

    private LocalDateTime recordedAt;   // 기록 시간
    private Double latitude;            // 위도
    private Double longitude;           // 경도
    private String locationName;        // 장소명

    private String tideType;            // 들물 / 설물
    private Integer tideCycle;          // 물때 1~12물
    private Double currentSpeed;        // 조류 속도
    private Double waterTemp;           // 수온
    private String weatherCondition;    // 맑음/흐림/비/바람
    private Double windSpeed;           // 풍속

    @OneToMany(mappedBy = "locationLog",
               cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FishRecord> fishRecords = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // 생성 메서드
    public static LocationLog create(DailyLog dailyLog,
                                      DailyLogRequestDto.AddLocation request,
                                      Integer sequence) {
        LocationLog loc = new LocationLog();
        loc.dailyLog         = dailyLog;
        loc.sequence         = sequence;
        loc.recordedAt       = request.getRecordedAt();
        loc.latitude         = request.getLatitude();
        loc.longitude        = request.getLongitude();
        loc.locationName     = request.getLocationName();
        loc.tideType         = request.getTideType();
        loc.tideCycle        = request.getTideCycle();
        loc.currentSpeed     = request.getCurrentSpeed();
        loc.waterTemp        = request.getWaterTemp();
        loc.weatherCondition = request.getWeatherCondition();
        loc.windSpeed        = request.getWindSpeed();
        return loc;
    }
}