package com.kmarine.fishing.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findByStatus(SettlementStatus status);

    List<Settlement> findByVesselId(Long vesselId);

    // 기간 내 정산 조회
    @Query("""
        SELECT s FROM Settlement s
        WHERE s.vessel.id = :vesselId
        AND s.periodStart >= :start
        AND s.periodEnd   <= :end
        """)
    List<Settlement> findByVesselAndPeriod(
            @Param("vesselId") Long vesselId,
            @Param("start")    LocalDate start,
            @Param("end")      LocalDate end);
}