package com.kmarine.fishing.dailylog;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fish_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FishRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_log_id", nullable = false)
    private LocationLog locationLog;

    private String fishType;    // 어종
    private Integer count;      // 마릿수
    private Double avgSize;     // 평균 씨알 (cm)
    private String method;      // 낚시 방법

    public static FishRecord create(LocationLog locationLog,
                                     DailyLogRequestDto.FishInfo request) {
        FishRecord f = new FishRecord();
        f.locationLog = locationLog;
        f.fishType    = request.getFishType();
        f.count       = request.getCount();
        f.avgSize     = request.getAvgSize();
        f.method      = request.getMethod();
        return f;
    }
}