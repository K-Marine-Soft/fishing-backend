package com.kmarine.fishing.batch;

public enum BatchJobTriggerType {
    SCHEDULED, // 등록된 cron에 의한 자동 실행
    MANUAL     // 관리자의 '지금 실행'에 의한 수동 실행
}
