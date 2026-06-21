package com.kmarine.fishing.vessel;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vessel_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VesselImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vessel_id")
    private Vessel vessel;

    private String imageUrl;    // S3 URL
    private Integer sortOrder;  // 정렬 순서
    private boolean thumbnail;  // 대표 이미지 여부

    public static VesselImage create(Vessel vessel, String imageUrl,
                                     Integer sortOrder, boolean thumbnail) {
        VesselImage image = new VesselImage();
        image.vessel     = vessel;
        image.imageUrl   = imageUrl;
        image.sortOrder  = sortOrder;
        image.thumbnail  = thumbnail;
        return image;
    }
}