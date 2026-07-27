package com.kmarine.fishing.fleet;

import com.kmarine.fishing.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "fleet_admin_applications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class FleetAdminApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fleet_id", nullable = false)
    private Fleet fleet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FleetAdminApplicationStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static FleetAdminApplication create(Fleet fleet, User applicant) {
        FleetAdminApplication a = new FleetAdminApplication();
        a.fleet     = fleet;
        a.applicant = applicant;
        a.status    = FleetAdminApplicationStatus.PENDING;
        return a;
    }

    public void approve() { this.status = FleetAdminApplicationStatus.APPROVED; }
    public void reject()  { this.status = FleetAdminApplicationStatus.REJECTED; }
}
