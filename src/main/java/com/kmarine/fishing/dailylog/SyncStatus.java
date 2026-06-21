package com.kmarine.fishing.dailylog;

public enum SyncStatus {
    LOCAL,      // 오프라인 저장 (미동기화)
    SYNCED,     // 동기화 완료
    CONFLICT    // 충돌 (서버 우선)
}