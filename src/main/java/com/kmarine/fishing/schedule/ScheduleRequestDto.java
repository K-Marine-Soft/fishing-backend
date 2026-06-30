package com.kmarine.fishing.schedule;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

public class ScheduleRequestDto {

    @Getter
    public static class Update {
        @NotNull
        private LocalDate date;

        @NotNull
        private Boolean available;
    }
}
