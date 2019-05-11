package com.paulmarkcastillo.androidlogger

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PMCLog(
    var priority: Int,
    var tag: String,
    var msg: String
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    var timestamp = System.currentTimeMillis()
}