package com.kmarine.fishing.vessel;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SeatMapResponseDto {
    private Integer maxPassengers;
    private List<Integer> occupiedSeats;
}
