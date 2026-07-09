package com.kmarine.fishing.vessel;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vessel_schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class VesselSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vessel_id", nullable = false)
    private Vessel vessel;

    @Column(nullable = false)
    private LocalDate scheduleDate;     // 날짜

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleType type;          // 출항가능/휴항/만석

    private String memo;                // 메모

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

	// 기존 VesselSchedule.java에 추가
    private String fishType;    // 낚시 종류 (감성돔, 볼락 등)
    
    public static VesselSchedule create(Vessel vessel,
                                         LocalDate date,
                                         ScheduleType type,
                                         String memo,
                                         String fishType) {
        VesselSchedule s = new VesselSchedule();
        s.vessel       = vessel;
        s.scheduleDate = date;
        s.type         = type;
        s.memo         = memo;
        s.fishType     = fishType;
        return s;
    }

    public void update(ScheduleType type, String memo) {
        this.type = type;
        this.memo = memo;
    }
}