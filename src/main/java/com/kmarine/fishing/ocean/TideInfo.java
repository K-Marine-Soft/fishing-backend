package com.kmarine.fishing.ocean;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TideInfo {
    private String time;    // 시간
    private Double height;  // 높이 (cm)
    private String type;    // HIGH / LOW
}