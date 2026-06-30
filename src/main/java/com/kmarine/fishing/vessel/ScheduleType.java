package com.kmarine.fishing.vessel;

public enum ScheduleType {
    AVAILABLE,  // 출항 가능
    CLOSED,     // 휴항 (정비/개인사정)
    FULL        // 수동 만석 처리
}