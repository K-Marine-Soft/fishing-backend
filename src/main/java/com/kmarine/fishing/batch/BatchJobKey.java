package com.kmarine.fishing.batch;

// 웹 화면에서 제어 가능한 배치(스케줄) 작업 종류
public enum BatchJobKey {
    MONTHLY_SETTLEMENT,        // 월별 자동 정산 생성
    RESERVATION_AUTO_COMPLETE, // 출항완료 자동 처리
    BATCH_LOG_CLEANUP          // 오래된 배치 실행 로그 자동 삭제
}
