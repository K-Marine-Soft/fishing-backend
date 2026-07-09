package com.kmarine.fishing.fleet;

import com.kmarine.fishing.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fleet_admin_mappings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FleetAdminMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fleet_id", nullable = false)
    private Fleet fleet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static FleetAdminMapping create(Fleet fleet, User user) {
        FleetAdminMapping m = new FleetAdminMapping();
        m.fleet = fleet;
        m.user  = user;
        return m;
    }
}