package com.paulmarkcastillo.androidlogger

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PMCLogDao {

    @Insert
    suspend fun addLog(log: PMCLog)

    @Query("SELECT * FROM PMCLog ORDER BY timestamp ASC")
    suspend fun getAllLogs() : List<PMCLog>

    @Query("DELETE FROM PMCLog")
    suspend fun deleteAllLogs()

    @Query("SELECT tag FROM PMCLog GROUP BY tag")
    suspend fun getTag() : List<String>

    @Query("SELECT * FROM PMCLog WHERE tag LIKE :tags")
    suspend fun getLogsWithTag(tags: String) : List<PMCLog>
}