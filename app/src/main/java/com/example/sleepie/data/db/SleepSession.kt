package com.example.sleepie.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_sessions")
data class SleepSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val duration: String,
    val quality: String
)
