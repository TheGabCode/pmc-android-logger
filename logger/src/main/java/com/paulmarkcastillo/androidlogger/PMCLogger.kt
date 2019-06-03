package com.paulmarkcastillo.androidlogger

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.opencsv.CSVWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

class PMCLogger {

    companion object {

        @SuppressLint("StaticFieldLeak")
        private lateinit var applicationContext: Context
        private const val tag = "PMCLogger"
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

        fun deleteLogs(): MutableLiveData<List<PMCLog>> {
            val logs = MutableLiveData<List<PMCLog>>()
            CoroutineScope(Dispatchers.IO).launch {
                val dao = PMCLogDatabase.getDatabase(applicationContext).logDao()
                dao.deleteAllLogs()
                logs.postValue(emptyList())
            }
            return logs
        }

        fun getAllTagsObservable(): MutableLiveData<List<String>> {
            val tags = MutableLiveData<List<String>>()
            val result = ArrayList<String>()
            CoroutineScope(Dispatchers.IO).launch {
                val dao = PMCLogDatabase.getDatabase(applicationContext).logDao()
                result.addAll(dao.getAllTags())
            }.invokeOnCompletion {
                val filteredResult = ArrayList<String>()
                result.forEach { string ->
                    // Separate tags in a string
                    val splittedTags = string.split(",").map { tag ->
                        tag.trim()
                    }

                    splittedTags.forEach { tag ->
                        if (!filteredResult.contains(tag)) {
                            filteredResult.add(tag)
                        }
                    }
                }
                filteredResult.sortWith(String.CASE_INSENSITIVE_ORDER)
                filteredResult.add(0, "All")
                tags.postValue(filteredResult)
            }
            return tags
        }

        fun getLogs(priority: Int, tag: String): MutableLiveData<List<PMCLog>> {
            val logs = MutableLiveData<List<PMCLog>>()
            CoroutineScope(Dispatchers.IO).launch {
                val dao = PMCLogDatabase.getDatabase(applicationContext).logDao()
                logs.postValue(dao.getAllLogs(priority, "%$tag%"))
            }
            return logs
        }

        // Export CSV

        fun exportCSV(file: File): MutableLiveData<String> {
            val statusObservable = MutableLiveData<String>()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val fileWriter = FileWriter(file)
                    val csvWriter = CSVWriter(fileWriter)

                    csvWriter.writeNext(
                        arrayOf(
                            "id",
                            "timestamp",
                            "priority",
                            "tag",
                            "message"
                        )
                    )

                    val dao = PMCLogDatabase.getDatabase(applicationContext).logDao()
                    val logs = dao.getAllLogs(Log.VERBOSE, "%%")
                    logs.forEach {
                        csvWriter.writeNext(
                            arrayOf(
                                it.id.toString(),
                                it.timestamp,
                                it.priority.toString(),
                                it.tag,
                                it.msg
                            )
                        )
                    }

                    csvWriter.close()
                    statusObservable.postValue("")
                } catch (e: Exception) {
                    e.printStackTrace()
                    statusObservable.postValue(e.message)
                }
            }

            return statusObservable
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

        fun getPriorityValue(priority: String): Int {
            return when (priority) {
                "Verbose" -> Log.VERBOSE
                "Debug" -> Log.DEBUG
                "Info" -> Log.INFO
                "Warn" -> Log.WARN
                "Error" -> Log.ERROR
                else -> 0
            }
        }
    }
}