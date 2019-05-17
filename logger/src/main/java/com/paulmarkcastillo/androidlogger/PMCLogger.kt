package com.paulmarkcastillo.androidlogger

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PMCLogger {

    companion object {

        @SuppressLint("StaticFieldLeak")
        private lateinit var applicationContext: Context
        private val tag = "PMCLogger"
        var enabled = true
        var printLogs = true
        var debugMode = false

        // Initialize

        fun init(context: Context, printLogs: Boolean = true) {
            applicationContext = context.applicationContext
            Companion.printLogs = printLogs
            i(tag, "PMC Logger Initialized")
        }

        // Log Creation

        fun v(tag: String, msg: String) {
            if (enabled) {
                if (printLogs && !debugMode) Log.v(tag, msg)
                addLog(Log.VERBOSE, tag, msg)
            }
        }

        fun d(tag: String, msg: String) {
            if (enabled) {
                if (printLogs && !debugMode) Log.d(tag, msg)
                addLog(Log.DEBUG, tag, msg)
            }
        }

        fun i(tag: String, msg: String) {
            if (enabled) {
                if (printLogs && !debugMode) Log.i(tag, msg)
                addLog(Log.INFO, tag, msg)
            }
        }

        fun w(tag: String, msg: String) {
            if (enabled) {
                if (printLogs && !debugMode) Log.w(tag, msg)
                addLog(Log.WARN, tag, msg)
            }
        }

        fun e(tag: String, msg: String) {
            if (enabled) {
                if (printLogs && !debugMode) Log.e(tag, msg)
                addLog(Log.ERROR, tag, msg)
            }
        }

        private fun addLog(priority: Int, tag: String, msg: String) {
            if (!debugMode) {
                val log = PMCLog(
                    priority = priority,
                    tag = tag,
                    msg = msg
                )
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = PMCLogDatabase.getDatabase(applicationContext).logDao()
                    dao.addLog(log)
                }
            } else {
                System.out.println("[${getPriorityText(priority)}] [$tag] $msg")
            }
        }

        // Log Access

        fun viewLogs(activity: Activity) {
            val intent = Intent(activity, PMCLogActivity::class.java)
            activity.startActivity(intent)
        }

        fun getLogsObservable(): MutableLiveData<List<PMCLog>> {
            val logs = MutableLiveData<List<PMCLog>>()
            CoroutineScope(Dispatchers.IO).launch {
                val dao = PMCLogDatabase.getDatabase(applicationContext).logDao()
                logs.postValue(dao.getAllLogs())
            }
            return logs
        }

        fun deleteLogs(): MutableLiveData<List<PMCLog>> {
            val logs = MutableLiveData<List<PMCLog>>()
            CoroutineScope(Dispatchers.IO).launch {
                val dao = PMCLogDatabase.getDatabase(applicationContext).logDao()
                dao.deleteAllLogs()
                logs.postValue(dao.getAllLogs())
            }
            return logs
        }

        fun getTag(): MutableLiveData<List<String>> {
            val logs = MutableLiveData<List<String>>()
            CoroutineScope(Dispatchers.IO).launch {
                val dao = PMCLogDatabase.getDatabase(applicationContext).logDao()
                logs.postValue(dao.getTag())
            }
            return logs
        }

        fun getLogsWithTag(tag: String): MutableLiveData<List<PMCLog>> {
            val logs = MutableLiveData<List<PMCLog>>()
            CoroutineScope(Dispatchers.IO).launch {
                val dao = PMCLogDatabase.getDatabase(applicationContext).logDao()
                val tags = "%$tag%"
                logs.postValue(dao.getLogsWithTag(tags))
            }
            return logs
        }

        // Utilities

        fun getPriorityText(priority: Int): String {
            return when (priority) {
                Log.VERBOSE -> "Verbose"
                Log.DEBUG -> "Debug"
                Log.INFO -> "Info"
                Log.WARN -> "Warn"
                Log.ERROR -> "Error"
                else -> "Unknown"
            }
        }
    }
}