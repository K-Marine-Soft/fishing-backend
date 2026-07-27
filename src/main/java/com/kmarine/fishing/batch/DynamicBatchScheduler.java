package com.kmarine.fishing.batch;

import com.kmarine.fishing.admin.SettlementService;
import com.kmarine.fishing.reservation.ReservationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

// 배치 작업을 실제로 등록/실행하는 컴포넌트
// - 기존 @Scheduled(cron="...") 어노테이션 대신, DB(batch_job_config)의 cron/enabled 값을 읽어 프로그래밍 방식으로 등록
// - 관리자가 웹에서 cron이나 사용여부를 바꾸면 reschedule()이 호출되어 재시작 없이 즉시 반영됨
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicBatchScheduler {

    private static final int LOG_RETENTION_DAYS = 90;

    private static final Map<BatchJobKey, String[]> DEFAULTS = Map.of(
            BatchJobKey.MONTHLY_SETTLEMENT,
                new String[]{"0 0 0 1 * *", "월별 자동 정산 생성 (매월 1일 자정)"},
            BatchJobKey.RESERVATION_AUTO_COMPLETE,
                new String[]{"0 0 0 * * *", "출항완료 자동 처리 (매일 자정)"},
            BatchJobKey.BATCH_LOG_CLEANUP,
                new String[]{"0 30 3 * * *",
                    "오래된 배치 실행 로그 자동 삭제 (" + LOG_RETENTION_DAYS + "일 이상 보관분, 매일 새벽 3시 30분)"}
    );

    private final BatchJobConfigRepository configRepository;
    private final BatchJobLogRepository logRepository;
    private final SettlementService settlementService;
    private final ReservationService reservationService;

    private final Map<BatchJobKey, Runnable> tasks = new EnumMap<>(BatchJobKey.class);
    private final Map<BatchJobKey, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();

    private ThreadPoolTaskScheduler taskScheduler;

    @PostConstruct
    public void init() {
        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(2);
        taskScheduler.setThreadNamePrefix("batch-job-");
        taskScheduler.initialize();

        tasks.put(BatchJobKey.MONTHLY_SETTLEMENT,
                settlementService::autoGenerateMonthlySettlements);
        tasks.put(BatchJobKey.RESERVATION_AUTO_COMPLETE,
                reservationService::autoCompletePastReservations);
        tasks.put(BatchJobKey.BATCH_LOG_CLEANUP,
                this::cleanupOldLogs);

        for (BatchJobKey jobKey : BatchJobKey.values()) {
            ensureSeeded(jobKey);
            reschedule(jobKey);
        }
    }

    // DB에 설정이 없으면(최초 기동) 기존 코드에 있던 기본값으로 시딩
    private void ensureSeeded(BatchJobKey jobKey) {
        if (configRepository.findByJobKey(jobKey).isPresent()) return;

        String[] def = DEFAULTS.get(jobKey);
        configRepository.save(BatchJobConfig.builder()
                .jobKey(jobKey)
                .cronExpression(def[0])
                .enabled(true)
                .description(def[1])
                .build());
    }

    // 현재 DB 설정을 기준으로 재등록 — 관리자가 설정을 바꾼 직후 호출되어 즉시 반영됨
    public synchronized void reschedule(BatchJobKey jobKey) {
        ScheduledFuture<?> existing = futures.remove(jobKey);
        if (existing != null) {
            existing.cancel(false);
        }

        BatchJobConfig config = configRepository.findByJobKey(jobKey)
                .orElseThrow(() -> new IllegalStateException(
                        "배치 설정을 찾을 수 없습니다: " + jobKey));

        if (!config.isEnabled()) {
            log.info("배치 작업 비활성화 상태 — 스케줄 등록하지 않음: {}", jobKey);
            return;
        }

        ScheduledFuture<?> future = taskScheduler.schedule(
                wrapped(jobKey, BatchJobTriggerType.SCHEDULED),
                new CronTrigger(config.getCronExpression()));
        futures.put(jobKey, future);
        log.info("배치 작업 스케줄 등록: {} cron={}", jobKey, config.getCronExpression());
    }

    // 등록 해제 — 스케줄에서 완전히 제거 (DB 레코드 삭제와 함께 호출됨)
    public synchronized void unregister(BatchJobKey jobKey) {
        ScheduledFuture<?> existing = futures.remove(jobKey);
        if (existing != null) {
            existing.cancel(false);
        }
        log.info("배치 작업 등록 해제: {}", jobKey);
    }

    // 관리자 수동 실행 — 활성화 여부와 무관하게 즉시 1회 실행
    public void runNow(BatchJobKey jobKey) {
        log.info("배치 작업 수동 실행 요청: {}", jobKey);
        taskScheduler.schedule(
                wrapped(jobKey, BatchJobTriggerType.MANUAL), Instant.now());
    }

    // 오래된 배치 실행 로그 삭제 — 로그 테이블이 무한정 쌓이지 않도록 정리
    private void cleanupOldLogs() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(LOG_RETENTION_DAYS);
        long deleted = logRepository.deleteByStartedAtBefore(threshold);
        log.info("오래된 배치 로그 삭제: {}건 (기준: {} 이전)", deleted, threshold);
    }

    private Runnable wrapped(BatchJobKey jobKey, BatchJobTriggerType triggerType) {
        Runnable task = tasks.get(jobKey);
        return () -> {
            BatchJobLog jobLog = logRepository.save(BatchJobLog.builder()
                    .jobKey(jobKey)
                    .triggerType(triggerType)
                    .build());
            try {
                task.run();
                jobLog.success();
                logRepository.save(jobLog);
            } catch (Exception e) {
                log.error("배치 작업 실행 실패: {}", jobKey, e);
                jobLog.fail(e.getMessage());
                logRepository.save(jobLog);
            }
        };
    }
}
