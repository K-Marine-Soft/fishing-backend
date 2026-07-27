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

// 배치 작업 실행 이력 — 시작/종료 시각, 성공/실패 여부를 웹에서 확인하기 위한 로그
@Entity
@Table(name = "batch_job_log")
@Getter
@NoArgsConstructor
public class BatchJobLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchJobKey jobKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchJobTriggerType triggerType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchJobLogStatus status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @Column(length = 2000)
    private String errorMessage;

    @Builder
    public BatchJobLog(BatchJobKey jobKey, BatchJobTriggerType triggerType) {
        this.jobKey = jobKey;
        this.triggerType = triggerType;
        this.status = BatchJobLogStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    public void success() {
        this.status = BatchJobLogStatus.SUCCESS;
        this.finishedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = BatchJobLogStatus.FAILED;
        this.finishedAt = LocalDateTime.now();
        this.errorMessage = (errorMessage != null && errorMessage.length() > 2000)
                ? errorMessage.substring(0, 2000)
                : errorMessage;
    }
}
