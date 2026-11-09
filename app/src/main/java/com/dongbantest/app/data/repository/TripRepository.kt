package com.dongbantest.app.data.repository

import com.dongbantest.app.data.local.MemoryEntity
import com.dongbantest.app.data.local.TripDao
import com.dongbantest.app.data.local.TripEntity
import com.dongbantest.app.domain.model.DataSourceType
import com.dongbantest.app.domain.model.Memory
import com.dongbantest.app.domain.model.MemoryType
import com.dongbantest.app.domain.model.Trip
import com.dongbantest.app.domain.model.TripWithMemories
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** 여행 기록 저장/조회. 서버 없이 Room 에만 저장한다. */
class TripRepository(
    private val dao: TripDao,
    private val files: MediaFileStore,
) {

    fun observeTrips(): Flow<List<Trip>> =
        combine(dao.observeTrips(), dao.observePhotoMemories()) { trips, photos ->
            val coverByTrip = photos.groupBy { it.tripId }.mapValues { it.value.first().filePath }
            trips.map { it.toDomain(coverByTrip[it.id]) }
        }

    fun observeOngoingTrip(): Flow<Trip?> = dao.observeOngoingTrip().map { it?.toDomain() }

    fun observeTripWithMemories(tripId: Long): Flow<TripWithMemories?> =
        combine(dao.observeTrip(tripId), dao.observeMemories(tripId)) { trip, memories ->
            trip?.let { t ->
                val domainMemories = memories.map { it.toDomain() }
                TripWithMemories(
                    trip = t.toDomain(domainMemories.firstOrNull { it.type == MemoryType.PHOTO }?.filePath),
                    memories = domainMemories,
                )
            }
        }

    suspend fun startTrip(
        title: String,
        destination: String,
        companions: List<String>,
        odometer: Float?,
        odometerSource: DataSourceType?,
        now: Long = System.currentTimeMillis(),
    ): Long = dao.insertTrip(
        TripEntity(
            title = title.trim(),
            destination = destination.trim(),
            companions = companions.joinToString(",") { it.trim() },
            startedAt = now,
            endedAt = null,
            startOdometer = odometer,
            endOdometer = null,
            odometerSource = odometerSource?.name,
            isSample = false,
        )
    )

    suspend fun endTrip(tripId: Long, odometer: Float?, now: Long = System.currentTimeMillis()) {
        val trip = dao.getTrip(tripId) ?: return
        dao.updateTrip(trip.copy(endedAt = now, endOdometer = odometer ?: trip.endOdometer))
    }

    suspend fun addTextMemo(tripId: Long, text: String) =
        addMemory(tripId, MemoryType.TEXT, text = text.trim())

    suspend fun addVoiceMemo(tripId: Long, path: String, durationMs: Long) =
        addMemory(tripId, MemoryType.VOICE, filePath = path, durationMs = durationMs)

    suspend fun addPhoto(tripId: Long, path: String) =
        addMemory(tripId, MemoryType.PHOTO, filePath = path)

    suspend fun addArrival(tripId: Long, note: String) =
        addMemory(tripId, MemoryType.ARRIVAL, text = note)

    suspend fun addQuestion(tripId: Long, question: String) =
        addMemory(tripId, MemoryType.QUESTION, text = question)

    suspend fun deleteMemory(memoryId: Long) {
        val memory = dao.getMemory(memoryId) ?: return
        files.delete(memory.filePath)
        dao.deleteMemory(memoryId)
    }

    /** 기록 삭제 시 관련 음성/사진 파일도 함께 정리한다. */
    suspend fun deleteTrip(tripId: Long) {
        dao.getMemories(tripId).forEach { files.delete(it.filePath) }
        dao.deleteTrip(tripId)
    }

    suspend fun seedSampleTrips() {
        if (dao.getSampleTrips().isNotEmpty()) return
        SampleData.trips().forEach { sample ->
            val id = dao.insertTrip(sample.trip)
            sample.memories.forEach { dao.insertMemory(it.copy(tripId = id)) }
        }
    }

    suspend fun clearSampleTrips() {
        dao.getSampleTrips().forEach { deleteTrip(it.id) }
    }

    private suspend fun addMemory(
        tripId: Long,
        type: MemoryType,
        text: String? = null,
        filePath: String? = null,
        durationMs: Long? = null,
    ): Long = dao.insertMemory(
        MemoryEntity(
            tripId = tripId,
            type = type.name,
            createdAt = System.currentTimeMillis(),
            text = text,
            filePath = filePath,
            transcript = null, // STT 미연동: 녹음 원본과 별도 필드로 남겨둔다.
            durationMs = durationMs,
        )
    )
}

private fun TripEntity.toDomain(coverPhotoPath: String? = null) = Trip(
    id = id,
    title = title,
    destination = destination,
    companions = companions.split(",").map { it.trim() }.filter { it.isNotEmpty() },
    startedAt = startedAt,
    endedAt = endedAt,
    startOdometer = startOdometer,
    endOdometer = endOdometer,
    odometerSource = odometerSource?.let { runCatching { DataSourceType.valueOf(it) }.getOrNull() },
    isSample = isSample,
    coverPhotoPath = coverPhotoPath,
)

private fun MemoryEntity.toDomain() = Memory(
    id = id,
    tripId = tripId,
    type = runCatching { MemoryType.valueOf(type) }.getOrDefault(MemoryType.TEXT),
    createdAt = createdAt,
    text = text,
    filePath = filePath,
    transcript = transcript,
    durationMs = durationMs,
)
