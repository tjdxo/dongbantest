package com.dongbantest.app.data.repository

import com.dongbantest.app.data.local.MemoryEntity
import com.dongbantest.app.data.local.TripEntity
import com.dongbantest.app.domain.model.DataSourceType
import com.dongbantest.app.domain.model.MemoryType
import java.util.Calendar

/**
 * 테스트용 예시 여행 기록. isSample = true 로 저장되어 실제 사용자 기록과 구분된다.
 * 주행거리 값은 Mock 출처로 표시된다.
 */
internal object SampleData {

    class SampleTrip(val trip: TripEntity, val memories: List<MemoryEntity>)

    fun trips(): List<SampleTrip> = listOf(
        sample(
            title = "가족과 함께한 속초 여행",
            destination = "속초 영금정",
            companions = "엄마,아빠,지우,지호",
            daysAgo = 40, hours = 9,
            odometer = 12_480f to 12_690f,
            memories = listOf(
                MemoryType.QUESTION to "오늘 여행에서 가장 기대되는 건 뭐야?",
                MemoryType.TEXT to "지호가 바다 보자마자 '파도가 춤춘다!'라고 외쳤다.",
                MemoryType.ARRIVAL to "속초 영금정 도착! 바람이 시원했다.",
                MemoryType.TEXT to "닭강정은 다음에도 꼭 다시 먹기로 약속.",
            ),
        ),
        sample(
            title = "연인과 함께한 제주도 여행",
            destination = "제주 협재해수욕장",
            companions = "민지,현우",
            daysAgo = 20, hours = 6,
            odometer = 30_105f to 30_188f,
            memories = listOf(
                MemoryType.QUESTION to "우리가 처음 함께 여행했던 날 기억나?",
                MemoryType.TEXT to "노을 질 때 차 안에서 같은 노래를 따라 불렀다.",
                MemoryType.ARRIVAL to "협재 도착. 에메랄드빛 바다!",
            ),
        ),
        sample(
            title = "친구들과의 강릉 드라이브",
            destination = "강릉 안목 커피거리",
            companions = "수빈,도윤,하린",
            daysAgo = 5, hours = 4,
            odometer = 8_002f to 8_231f,
            memories = listOf(
                MemoryType.QUESTION to "다음에는 우리 어디로 떠나볼까?",
                MemoryType.TEXT to "휴게소 감자 3개를 순식간에 해치움.",
                MemoryType.ARRIVAL to "안목해변 커피 한 잔.",
                MemoryType.TEXT to "다음 여행은 전주 한옥마을로 결정!",
            ),
        ),
    )

    private fun sample(
        title: String,
        destination: String,
        companions: String,
        daysAgo: Int,
        hours: Int,
        odometer: Pair<Float, Float>,
        memories: List<Pair<MemoryType, String>>,
    ): SampleTrip {
        val start = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
        }.timeInMillis
        val end = start + hours * 60 * 60 * 1000L
        val step = (end - start) / (memories.size + 1)
        return SampleTrip(
            trip = TripEntity(
                title = title,
                destination = destination,
                companions = companions,
                startedAt = start,
                endedAt = end,
                startOdometer = odometer.first,
                endOdometer = odometer.second,
                odometerSource = DataSourceType.MOCK.name,
                isSample = true,
            ),
            memories = memories.mapIndexed { i, (type, text) ->
                MemoryEntity(
                    tripId = 0,
                    type = type.name,
                    createdAt = start + step * (i + 1),
                    text = text,
                    filePath = null,
                    transcript = null,
                    durationMs = null,
                )
            },
        )
    }
}
