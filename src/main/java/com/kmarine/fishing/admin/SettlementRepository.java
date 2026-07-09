package com.kmarine.fishing.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SettlementRepository
        extends JpaRepository<Settlement, Long> {

    List<Settlement> findByStatus(SettlementStatus status);

    List<Settlement> findByFleetId(Long fleetId);

    // 선단별 특정 기간 정산 내역
    @Query("""
        SELECT s FROM Settlement s
        WHERE s.fleet.id = :fleetId
        AND s.periodStart >= :start
        AND s.periodEnd   <= :end
        ORDER BY s.periodStart DESC
        """)
    List<Settlement> findByFleetAndPeriod(
            @Param("fleetId") Long fleetId,
            @Param("start")   LocalDate start,
            @Param("end")     LocalDate end);

    // 이미 정산된 기간 중복 체크
    @Query("""
        SELECT COUNT(s) FROM Settlement s
        WHERE s.fleet.id = :fleetId
        AND s.periodStart = :start
        AND s.periodEnd   = :end
        """)
    Long countByFleetAndPeriod(
            @Param("fleetId") Long fleetId,
            @Param("start")   LocalDate start,
            @Param("end")     LocalDate end);
}
/*package com.kmarine.fishing.admin;

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
}*/