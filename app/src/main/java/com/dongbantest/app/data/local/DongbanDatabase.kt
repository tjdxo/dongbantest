package com.dongbantest.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TripEntity::class, MemoryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class DongbanDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao

    companion object {
        fun create(context: Context): DongbanDatabase =
            Room.databaseBuilder(context, DongbanDatabase::class.java, "dongbancha.db")
                .build()
    }
}
