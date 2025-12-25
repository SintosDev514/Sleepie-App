package com.example.sleepie.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepSessionDao {
    @Query("SELECT * FROM sleep_sessions ORDER BY id DESC")
    fun getAllSleepSessions(): Flow<List<SleepSession>>

    @Insert
    suspend fun insertSleepSession(session: SleepSession)
}
