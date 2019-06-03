package com.paulmarkcastillo.androidlogger

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PMCLog::class], version = 2)
abstract class PMCLogDatabase : RoomDatabase() {

    abstract fun logDao(): PMCLogDao

    companion object {
        @Volatile
        private var INSTANCE: PMCLogDatabase? = null

        fun getDatabase(context: Context): PMCLogDatabase {
            val tempInstance = INSTANCE

            if (tempInstance != null) return tempInstance

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PMCLogDatabase::class.java,
                    "pmclog_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}