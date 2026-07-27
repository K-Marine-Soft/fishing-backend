package com.kmarine.fishing.vessel;

import com.kmarine.fishing.fleet.Fleet;
import com.kmarine.fishing.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vessels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Vessel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;                 // 선주

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fleet_id")
    private Fleet fleet;                // 소속 선단
    
    @Column(nullable = false)
    private String name;                // 선박명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VesselType type;            // 선박 종류

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VesselStatus status;        // 승인 상태

    @Column(nullable = false)
    private String region;              // 출항 지역 (부산, 제주 등)

    private String departurePort;       // 출항 항구명

    private Double latitude;            // 출항지 위도
    private Double longitude;           // 출항지 경도

    @Column(nullable = false)
    private Integer maxPassengers;      // 최대 승선 인원

    @Column(nullable = false)
    private Integer pricePerPerson;     // 1인당 가격

    private String description;         // 선박 소개

    private String licenseNumber;       // 선박 등록번호

    private Integer buildYear;          // 건조 연도
    private Double vesselLength;        // 선박 길이 (m)
    private Integer enginePower;        // 엔진 출력 (HP)

    @OneToMany(mappedBy = "vessel",
               cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<VesselImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "vessel",
               cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VesselOption> options = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 생성 메서드
    public static Vessel create(User owner, Fleet fleet, VesselRequestDto.Register request) {
        Vessel vessel = new Vessel();
        vessel.owner          = owner;
        vessel.fleet  		  = fleet;          // ← 추가
        vessel.name           = request.getName();
        vessel.type           = request.getType();
        // 선단 관리자가 직접 등록하므로 별도 승인 절차 없이 즉시 승인
        vessel.status         = VesselStatus.APPROVED;
        vessel.region         = request.getRegion();
        vessel.departurePort  = request.getDeparturePort();
        vessel.latitude       = request.getLatitude();
        vessel.longitude      = request.getLongitude();
        vessel.maxPassengers  = request.getMaxPassengers();
        vessel.pricePerPerson = request.getPricePerPerson();
        vessel.description    = request.getDescription();
        vessel.licenseNumber  = request.getLicenseNumber();
        vessel.buildYear      = request.getBuildYear();
        vessel.vesselLength   = request.getVesselLength();
        vessel.enginePower    = request.getEnginePower();
        return vessel;
    }

    // 승인
    public void approve() { this.status = VesselStatus.APPROVED; }

    // 거절
    public void reject()  { this.status = VesselStatus.REJECTED; }

    // 정보 수정
    public void update(VesselRequestDto.Update request) {
        this.name           = request.getName();
        this.type           = request.getType();
        this.region         = request.getRegion();
        this.departurePort  = request.getDeparturePort();
        this.latitude       = request.getLatitude();
        this.longitude      = request.getLongitude();
        this.maxPassengers  = request.getMaxPassengers();
        this.pricePerPerson = request.getPricePerPerson();
        this.description    = request.getDescription();
        this.licenseNumber  = request.getLicenseNumber();
        this.buildYear      = request.getBuildYear();
        this.vesselLength   = request.getVesselLength();
        this.enginePower    = request.getEnginePower();
    }
	// fleet getter 확인용
    public Long getFleetId() {
        return fleet != null ? fleet.getId() : null;
    }
}