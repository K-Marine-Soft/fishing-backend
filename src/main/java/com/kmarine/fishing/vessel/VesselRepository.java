package com.kmarine.fishing.vessel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VesselRepository extends JpaRepository<Vessel, Long> {

    // 승인된 선박 전체 조회
    List<Vessel> findByStatus(VesselStatus status);

    // 지역별 조회
    List<Vessel> findByRegionAndStatus(String region, VesselStatus status);

    // 선주별 조회
    List<Vessel> findByOwnerId(Long ownerId);
    @Query("""
    	    SELECT DISTINCT v FROM Vessel v
    	    LEFT JOIN v.options o
    	    WHERE v.status = 'APPROVED'
    	    AND (:region    IS NULL OR v.region = :region)
    	    AND (:type      IS NULL OR v.type   = :type)
    	    AND (:minPass   IS NULL OR v.maxPassengers  >= :minPass)
    	    AND (:maxPrice  IS NULL OR v.pricePerPerson <= :maxPrice)
    	    AND (:minPrice  IS NULL OR v.pricePerPerson >= :minPrice)
    	    AND (:option    IS NULL OR o.optionName     =  :option)
    	    ORDER BY v.createdAt DESC
    	    """)
    	List<Vessel> search(
    	        @Param("region")   String region,
    	        @Param("type")     VesselType type,
    	        @Param("minPass")  Integer minPass,
    	        @Param("maxPrice") Integer maxPrice,
    	        @Param("minPrice") Integer minPrice,
    	        @Param("option")   String option);
    /*
    // 검색 (지역 + 타입 + 인원)
    @Query("""
        SELECT v FROM Vessel v
        WHERE v.status = 'APPROVED'
        AND (:region   IS NULL OR v.region = :region)
        AND (:type     IS NULL OR v.type   = :type)
        AND (:minPass  IS NULL OR v.maxPassengers >= :minPass)
        AND (:maxPrice IS NULL OR v.pricePerPerson <= :maxPrice)
        """)
    List<Vessel> search(
            @Param("region")   String region,
            @Param("type")     VesselType type,
            @Param("minPass")  Integer minPass,
            @Param("maxPrice") Integer maxPrice
    );*/
}