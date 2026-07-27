package com.kmarine.fishing.batch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface BatchJobLogRepository extends JpaRepository<BatchJobLog, Long> {

    // jobKey / status는 null이면 조건에서 제외 (전체 조회)
    @Query("SELECT l FROM BatchJobLog l WHERE "
            + "(:jobKey IS NULL OR l.jobKey = :jobKey) AND "
            + "(:status IS NULL OR l.status = :status) "
            + "ORDER BY l.startedAt DESC")
    Page<BatchJobLog> search(@Param("jobKey") BatchJobKey jobKey,
                              @Param("status") BatchJobLogStatus status,
                              Pageable pageable);

    // 보관기간이 지난 로그 삭제 (배치 로그 자동 정리용)
    long deleteByStartedAtBefore(LocalDateTime threshold);
}
