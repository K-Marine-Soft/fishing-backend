package com.kmarine.fishing.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchJobService {

    private final BatchJobConfigRepository configRepository;
    private final BatchJobLogRepository logRepository;
    private final DynamicBatchScheduler scheduler;

    @Transactional(readOnly = true)
    public List<BatchJobResponseDto> getAll() {
        return configRepository.findAll().stream()
                .map(BatchJobResponseDto::from)
                .toList();
    }

    // 신규 스케줄 등록 — 이미 등록된 jobKey면 거부 (수정은 update() 사용)
    @Transactional
    public void create(BatchJobKey jobKey, BatchJobRequestDto.Create request) {
        if (configRepository.findByJobKey(jobKey).isPresent()) {
            throw new IllegalArgumentException(
                    "이미 등록된 배치 작업입니다: " + jobKey);
        }

        validateCron(request.getCronExpression());

        configRepository.save(BatchJobConfig.builder()
                .jobKey(jobKey)
                .cronExpression(request.getCronExpression())
                .enabled(request.isEnabled())
                .description(request.getDescription())
                .build());
        scheduler.reschedule(jobKey);
    }

    // cron / 사용여부 / 설명 수정 — 저장 즉시 스케줄에 반영
    @Transactional
    public void update(BatchJobKey jobKey, BatchJobRequestDto.Update request) {
        BatchJobConfig config = getOrThrow(jobKey);

        validateCron(request.getCronExpression());

        config.update(request.getCronExpression(), request.isEnabled(),
                request.getDescription());
        scheduler.reschedule(jobKey);
    }

    // 사용/미사용만 빠르게 전환
    @Transactional
    public void setEnabled(BatchJobKey jobKey, boolean enabled) {
        BatchJobConfig config = getOrThrow(jobKey);
        config.setEnabled(enabled);
        scheduler.reschedule(jobKey);
    }

    // 등록 해제
    @Transactional
    public void delete(BatchJobKey jobKey) {
        BatchJobConfig config = getOrThrow(jobKey);
        configRepository.delete(config);
        scheduler.unregister(jobKey);
    }

    public void runNow(BatchJobKey jobKey) {
        getOrThrow(jobKey);
        scheduler.runNow(jobKey);
    }

    // 배치 실행 로그 조회 (jobKey / status 미지정 시 전체)
    @Transactional(readOnly = true)
    public Page<BatchJobLogResponseDto> getLogs(BatchJobKey jobKey, BatchJobLogStatus status,
                                                 int page, int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(Math.max(size, 1), 100));
        return logRepository.search(jobKey, status, pageable)
                .map(BatchJobLogResponseDto::from);
    }

    // 실행 로그 개별 삭제
    @Transactional
    public void deleteLog(Long logId) {
        if (!logRepository.existsById(logId)) {
            throw new IllegalArgumentException("존재하지 않는 로그입니다: " + logId);
        }
        logRepository.deleteById(logId);
    }

    // 실행 로그 일괄 삭제
    @Transactional
    public void deleteLogs(List<Long> logIds) {
        logRepository.deleteAllByIdInBatch(logIds);
    }

    private BatchJobConfig getOrThrow(BatchJobKey jobKey) {
        return configRepository.findByJobKey(jobKey)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 배치 작업입니다: " + jobKey));
    }

    private void validateCron(String cronExpression) {
        try {
            CronExpression.parse(cronExpression);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "잘못된 cron 표현식입니다: " + cronExpression);
        }
    }
}
