package com.kmarine.fishing.batch;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Builder
public class BatchJobLogResponseDto {

    private Long id;
    private BatchJobKey jobKey;
    private BatchJobTriggerType triggerType;
    private BatchJobLogStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Long durationMs;
    private String errorMessage;

    public static BatchJobLogResponseDto from(BatchJobLog log) {
        Long durationMs = log.getFinishedAt() != null
                ? Duration.between(log.getStartedAt(), log.getFinishedAt()).toMillis()
                : null;

        return BatchJobLogResponseDto.builder()
                .id(log.getId())
                .jobKey(log.getJobKey())
                .triggerType(log.getTriggerType())
                .status(log.getStatus())
                .startedAt(log.getStartedAt())
                .finishedAt(log.getFinishedAt())
                .durationMs(durationMs)
                .errorMessage(log.getErrorMessage())
                .build();
    }
}
