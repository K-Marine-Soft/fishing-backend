package com.kmarine.fishing.dailylog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {

    // 선박별 날짜 조회 (중복 방지)
    Optional<DailyLog> findByVesselIdAndLogDate(Long vesselId, LocalDate logDate);

    // 선박별 목록
    List<DailyLog> findByVesselIdOrderByLogDateDesc(Long vesselId);

    // 물때별 평균 마릿수 분석
    @Query("""
        SELECT ll.tideCycle, ll.tideType,
               AVG(fr.count), SUM(fr.count), COUNT(DISTINCT ll.id)
        FROM LocationLog ll
        JOIN ll.fishRecords fr
        JOIN ll.dailyLog dl
        WHERE dl.vessel.id = :vesselId
        GROUP BY ll.tideCycle, ll.tideType
        ORDER BY AVG(fr.count) DESC
        """)
    List<Object[]> analyzeTide(@Param("vesselId") Long vesselId);

    // 장소별 조황 누적
    @Query("""
        SELECT ll.locationName, ll.latitude, ll.longitude,
               SUM(fr.count), COUNT(DISTINCT dl.logDate)
        FROM LocationLog ll
        JOIN ll.fishRecords fr
        JOIN ll.dailyLog dl
        WHERE dl.vessel.id = :vesselId
        AND ll.locationName IS NOT NULL
        GROUP BY ll.locationName, ll.latitude, ll.longitude
        ORDER BY SUM(fr.count) DESC
        """)
    List<Object[]> analyzeLocation(@Param("vesselId") Long vesselId);
}