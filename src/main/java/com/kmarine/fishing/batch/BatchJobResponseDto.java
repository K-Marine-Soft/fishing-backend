package com.kmarine.fishing.batch;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BatchJobResponseDto {

    private BatchJobKey jobKey;
    private String cronExpression;
    private boolean enabled;
    private String description;
    private LocalDateTime updatedAt;

    public static BatchJobResponseDto from(BatchJobConfig config) {
        return BatchJobResponseDto.builder()
                .jobKey(config.getJobKey())
                .cronExpression(config.getCronExpression())
                .enabled(config.isEnabled())
                .description(config.getDescription())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
