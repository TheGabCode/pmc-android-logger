package com.paulmarkcastillo.androidlogger

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PMCLogDao {

    @Insert
    suspend fun addLog(log: PMCLog)

    @Query("SELECT tag FROM PMCLog GROUP BY tag")
    suspend fun getAllTags(): List<String>

    @Query("DELETE FROM PMCLog")
    suspend fun deleteAllLogs()

    @Query("SELECT * FROM PMCLog WHERE priority >= :priority AND tag LIKE :tag AND msg LIKE :msg ORDER BY id ASC")
    suspend fun getAllLogs(priority: Int, tag: String, msg: String): List<PMCLog>
}