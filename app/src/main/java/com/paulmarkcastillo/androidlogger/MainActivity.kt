package com.paulmarkcastillo.androidlogger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PMCLogger.init(this, printLogs = true)

        val tag = "MainActivity"

        button.setOnClickListener {
            PMCLogger.v(tag, "VERBOSE")
            PMCLogger.d(tag, "DEBUG")
            PMCLogger.i(tag, "INFO")
            PMCLogger.w(tag, "WARN")
            PMCLogger.e(tag, "ERROR")

            PMCLogger.d("tag1", "Single Tag")
            PMCLogger.d("tag1, tag2, tag3", "Multiple Tags")
            PMCLogger.d("Activity Tracking  , Yow     ", "Tags with space")

            PMCLogger.viewLogs(this)
        }
    }
}
