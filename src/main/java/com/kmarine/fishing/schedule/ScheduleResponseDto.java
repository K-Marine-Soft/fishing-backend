package com.kmarine.fishing.schedule;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduleResponseDto {

    private int reserved;
    private boolean available;
}
