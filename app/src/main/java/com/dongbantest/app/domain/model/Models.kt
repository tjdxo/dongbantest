package com.dongbantest.app.domain.model

/**
 * 동반차 앱의 도메인 모델.
 * UI / ViewModel 은 이 모델만 사용하며 Room Entity 나 Pleos SDK 클래스에 직접 의존하지 않는다.
 */

data class Trip(
    val id: Long,
    val title: String,
    val destination: String,
    val companions: List<String>,
    val startedAt: Long,
    val endedAt: Long?,
    /** 여행 시작 시점의 누적 주행거리 (SDK 값이 있을 때만). */
    val startOdometer: Float?,
    /** 여행 종료 시점의 누적 주행거리 (SDK 값이 있을 때만). */
    val endOdometer: Float?,
    /** 주행거리 값의 출처 (실제 차량 / Mock). */
    val odometerSource: DataSourceType?,
    /** 예시(테스트) 데이터 여부 — 실제 사용자 기록과 구분한다. */
    val isSample: Boolean,
    val coverPhotoPath: String? = null,
) {
    val isOngoing: Boolean get() = endedAt == null

    /** 주행거리 차이. 두 값이 모두 있을 때만 계산한다. */
    val drivenDistance: Float?
        get() = if (startOdometer != null && endOdometer != null && endOdometer >= startOdometer) {
            endOdometer - startOdometer
        } else null
}

enum class MemoryType { TEXT, VOICE, PHOTO, ARRIVAL, QUESTION }

/**
 * 여행 중 남긴 하나의 기록.
 * - TEXT: [text] 에 메모
 * - VOICE: [filePath] 에 녹음 원본, [transcript] 에 STT 결과(별도 데이터, 현재는 미지원 → null)
 * - PHOTO: [filePath] 에 앱 전용 저장소로 복사한 사진
 * - ARRIVAL: 도착 기록 (수동 버튼)
 * - QUESTION: 함께 나눈 질문
 */
data class Memory(
    val id: Long,
    val tripId: Long,
    val type: MemoryType,
    val createdAt: Long,
    val text: String? = null,
    val filePath: String? = null,
    val transcript: String? = null,
    val durationMs: Long? = null,
)

data class TripWithMemories(
    val trip: Trip,
    val memories: List<Memory>,
)

enum class DataSourceType { PLEOS_VEHICLE, MOCK }
