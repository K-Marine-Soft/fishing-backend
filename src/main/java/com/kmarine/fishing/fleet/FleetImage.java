package com.kmarine.fishing.fleet;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fleet_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FleetImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fleet_id")
    private Fleet fleet;

    private String imageUrl;
    private Integer sortOrder;  // 롤링 배너 표시 순서

    public static FleetImage create(Fleet fleet, String imageUrl, Integer sortOrder) {
        FleetImage image = new FleetImage();
        image.fleet     = fleet;
        image.imageUrl  = imageUrl;
        image.sortOrder = sortOrder;
        return image;
    }
}
