package com.kmarine.fishing.vessel;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vessel_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VesselOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vessel_id")
    private Vessel vessel;

    private String optionName;  // 화장실, 에어컨, 낚시대 제공 등

    public static VesselOption create(Vessel vessel, String optionName) {
        VesselOption option = new VesselOption();
        option.vessel     = vessel;
        option.optionName = optionName;
        return option;
    }
}