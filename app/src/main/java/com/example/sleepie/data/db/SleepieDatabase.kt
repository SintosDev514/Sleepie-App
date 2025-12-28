package com.example.sleepie.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [SleepSession::class], version = 2, exportSchema = false)
abstract class SleepieDatabase : RoomDatabase() {

    abstract fun sleepSessionDao(): SleepSessionDao

    companion object {
        @Volatile
        private var INSTANCE: SleepieDatabase? = null

        // This migration uses the correct INTEGER type for SQLite.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE sleep_sessions ADD COLUMN startTime INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE sleep_sessions ADD COLUMN endTime INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): SleepieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SleepieDatabase::class.java,
                    "sleepie_database"
                )
                .addMigrations(MIGRATION_1_2)
                // This is the definitive fix. It will destroy and recreate the database if migration fails.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
