package com.kmarine.fishing.fleet;

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
@Table(name = "fleets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Fleet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fleetName;          // 선단명

    @Column(unique = true)
    private String subdomain;          // 서브도메인 (busan-fishing)

    private String customDomain;       // 자체 도메인 (www.xxx.com)

    @Column(nullable = false)
    private String region;             // 지역

    private String port;               // 출조항

    private String address;            // 주소

    private String phone;              // 전화번호

    private String bankInfo;           // 입금안내

    private String departurTime;       // 출발시간 (06:00)

    private String returnTime;         // 입항시간 (15:00)

    private String description;        // 선단소개

    private String logoUrl;            // 로고 이미지

    private String themeColor;         // 테마색상 (#0d47a1)

    // 계약 정보
    private Integer monthlyFee;        // 월정액 (원)
    private Integer commissionRate;    // 수수료율 (%)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FleetStatus status;        // ACTIVE / INACTIVE / PENDING

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by")
    private User requestedBy;          // 선단 등록을 신청한 사용자 (승인 시 선단관리자로 지정)

    @OneToMany(mappedBy = "fleet",
               cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<FleetImage> images = new ArrayList<>();  // 메인 롤링 배너 이미지

    // 메뉴 설정 (JSON)
    @Column(columnDefinition = "TEXT")
    private String menuConfig;

    // 출조 요금 정보
    private Integer adultFare;         // 성인 요금
    private String  fareIncludes;      // 포함 항목
    private String  fareExcludes;      // 불포함 항목

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 생성 메서드
    public static Fleet create(FleetRequestDto.Create request,
                                User requestedBy) {
        Fleet f = new Fleet();
        f.requestedBy   = requestedBy;
        f.fleetName     = request.getFleetName();
        f.subdomain     = request.getSubdomain();
        f.customDomain  = request.getCustomDomain();
        f.region        = request.getRegion();
        f.port          = request.getPort();
        f.address       = request.getAddress();
        f.phone         = request.getPhone();
        f.bankInfo      = request.getBankInfo();
        f.departurTime  = request.getDeparturTime();
        f.returnTime    = request.getReturnTime();
        f.description   = request.getDescription();
        f.themeColor    = request.getThemeColor() != null
                          ? request.getThemeColor() : "#0d47a1";
        f.monthlyFee    = request.getMonthlyFee();
        f.commissionRate = request.getCommissionRate();
        f.adultFare     = request.getAdultFare();
        f.fareIncludes  = request.getFareIncludes();
        f.fareExcludes  = request.getFareExcludes();
        f.status        = FleetStatus.PENDING;
        f.menuConfig    = defaultMenuConfig();
        return f;
    }

    // 기본 메뉴 설정
    private static String defaultMenuConfig() {
        return "[\"reservation\",\"my-reservation\"," +
               "\"fishing-report\",\"vessel-info\"," +
               "\"guide\",\"notice\",\"inquiry\"," +
               "\"free-board\",\"location\",\"admin\"]";
    }

    public void activate()   { this.status = FleetStatus.ACTIVE; }
    public void deactivate() { this.status = FleetStatus.INACTIVE; }

    public void update(FleetRequestDto.Update request) {
        if (request.getFleetName() != null)
            this.fleetName    = request.getFleetName();
        if (request.getPhone() != null)
            this.phone        = request.getPhone();
        if (request.getBankInfo() != null)
            this.bankInfo     = request.getBankInfo();
        if (request.getAddress() != null)
            this.address      = request.getAddress();
        if (request.getDescription() != null)
            this.description  = request.getDescription();
        if (request.getThemeColor() != null)
            this.themeColor   = request.getThemeColor();
        if (request.getMenuConfig() != null)
            this.menuConfig   = request.getMenuConfig();
        if (request.getAdultFare() != null)
            this.adultFare    = request.getAdultFare();
        if (request.getFareIncludes() != null)
            this.fareIncludes = request.getFareIncludes();
        if (request.getFareExcludes() != null)
            this.fareExcludes = request.getFareExcludes();
    }
}