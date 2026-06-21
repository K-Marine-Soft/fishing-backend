package com.kmarine.fishing.dailylog;

import com.kmarine.fishing.vessel.Vessel;
import com.kmarine.fishing.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "daily_logs",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"vessel_id", "log_date"})) // 하루 1건
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DailyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vessel_id", nullable = false)
    private Vessel vessel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captain_id", nullable = false)
    private User captain;               // 선장

    @Column(nullable = false)
    private LocalDate logDate;          // 출항 날짜

    private LocalDateTime departureTime; // 출항 시간
    private LocalDateTime returnTime;    // 입항 시간

    private Integer totalPassengers;    // 승선 인원
    private String memo;                // 메모

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SyncStatus syncStatus;      // 동기화 상태

    // 오프라인 로컬 UUID (앱에서 생성)
    private String localId;

    @OneToMany(mappedBy = "dailyLog",
               cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    private List<LocationLog> locationLogs = new ArrayList<>();

    @OneToMany(mappedBy = "dailyLog",
               cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyExpense> dailyExpenses = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 생성 메서드
    public static DailyLog create(Vessel vessel, User captain,
                                   DailyLogRequestDto.Create request) {
        DailyLog log = new DailyLog();
        log.vessel          = vessel;
        log.captain         = captain;
        log.logDate         = request.getLogDate();
        log.departureTime   = request.getDepartureTime();
        log.totalPassengers = request.getTotalPassengers();
        log.memo            = request.getMemo();
        log.localId         = request.getLocalId();
        log.syncStatus      = SyncStatus.SYNCED;
        return log;
    }

    // 입항 처리
    public void arrive(LocalDateTime returnTime) {
        this.returnTime = returnTime;
    }

    // 동기화 완료
    public void markSynced() {
        this.syncStatus = SyncStatus.SYNCED;
    }
}