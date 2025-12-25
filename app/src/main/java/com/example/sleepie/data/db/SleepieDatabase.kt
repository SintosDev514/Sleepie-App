package com.example.sleepie.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SleepSession::class], version = 1, exportSchema = false)
abstract class SleepieDatabase : RoomDatabase() {

    abstract fun sleepSessionDao(): SleepSessionDao

    companion object {
        @Volatile
        private var INSTANCE: SleepieDatabase? = null

        fun getDatabase(context: Context): SleepieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SleepieDatabase::class.java,
                    "sleepie_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
