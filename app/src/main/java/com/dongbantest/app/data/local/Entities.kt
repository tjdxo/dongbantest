package com.dongbantest.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val destination: String,
    /** 쉼표(,)로 구분된 동승자 이름 */
    val companions: String,
    val startedAt: Long,
    val endedAt: Long?,
    val startOdometer: Float?,
    val endOdometer: Float?,
    /** DataSourceType.name 또는 null */
    val odometerSource: String?,
    val isSample: Boolean,
)

@Entity(
    tableName = "memories",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("tripId")],
)
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    /** MemoryType.name */
    val type: String,
    val createdAt: Long,
    val text: String?,
    /** 녹음/사진 파일 경로 (앱 전용 저장소 내부) */
    val filePath: String?,
    /** 음성 → 텍스트 변환 결과. 녹음 원본과 별도로 관리한다. */
    val transcript: String?,
    val durationMs: Long?,
)
