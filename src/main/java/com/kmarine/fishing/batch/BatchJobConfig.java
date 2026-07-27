package com.kmarine.fishing.batch;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 배치(스케줄) 작업의 실행 주기(cron)와 사용 여부를 DB에 보관 — 코드 수정/재배포 없이 웹에서 변경 가능
@Entity
@Table(name = "batch_job_config")
@Getter
@NoArgsConstructor
public class BatchJobConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private BatchJobKey jobKey;

    @Column(nullable = false)
    private String cronExpression;

    @Column(nullable = false)
    private boolean enabled;

    private String description;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public BatchJobConfig(BatchJobKey jobKey, String cronExpression,
                           boolean enabled, String description) {
        this.jobKey = jobKey;
        this.cronExpression = cronExpression;
        this.enabled = enabled;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String cronExpression, boolean enabled, String description) {
        this.cronExpression = cronExpression;
        this.enabled = enabled;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }
}
