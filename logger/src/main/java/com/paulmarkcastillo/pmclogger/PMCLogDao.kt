package com.paulmarkcastillo.pmclogger

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PMCLogDao {

    @Insert
    suspend fun addLog(log: PMCLog)

    @Query("SELECT * FROM PMCLog ORDER BY timestamp ASC")
    suspend fun getLogs() : List<PMCLog>

    @Query("DELETE FROM PMCLog")
    suspend fun deleteAllLogs()
}