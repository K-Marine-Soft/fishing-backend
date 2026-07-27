package com.kmarine.fishing.batch;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public class BatchJobRequestDto {

    // 신규 배치 스케줄 등록
    @Getter
    @Setter
    public static class Create {
        @NotBlank(message = "cron 표현식은 필수입니다.")
        private String cronExpression;
        private boolean enabled;
        private String description;
    }

    // cron / 사용여부 / 설명 수정
    @Getter
    @Setter
    public static class Update {
        @NotBlank(message = "cron 표현식은 필수입니다.")
        private String cronExpression;
        private boolean enabled;
        private String description;
    }

    // 사용/미사용만 빠르게 전환
    @Getter
    @Setter
    public static class EnabledUpdate {
        private boolean enabled;
    }
}
