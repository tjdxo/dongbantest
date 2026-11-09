package com.dongbantest.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Query("SELECT * FROM trips ORDER BY startedAt DESC")
    fun observeTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE endedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    fun observeOngoingTrip(): Flow<TripEntity?>

    @Query("SELECT * FROM trips WHERE id = :id")
    fun observeTrip(id: Long): Flow<TripEntity?>

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTrip(id: Long): TripEntity?

    @Query("SELECT * FROM trips WHERE isSample = 1")
    suspend fun getSampleTrips(): List<TripEntity>

    @Insert
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteTrip(id: Long)

    @Query("SELECT * FROM memories WHERE tripId = :tripId ORDER BY createdAt ASC")
    fun observeMemories(tripId: Long): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE tripId = :tripId")
    suspend fun getMemories(tripId: Long): List<MemoryEntity>

    /** 다이어리 목록 카드의 표지 사진용 */
    @Query("SELECT * FROM memories WHERE type = 'PHOTO' ORDER BY createdAt ASC")
    fun observePhotoMemories(): Flow<List<MemoryEntity>>

    @Insert
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Query("SELECT * FROM memories WHERE id = :id")
    suspend fun getMemory(id: Long): MemoryEntity?

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemory(id: Long)
}
