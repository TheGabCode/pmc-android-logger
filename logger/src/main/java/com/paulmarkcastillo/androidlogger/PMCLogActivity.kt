package com.paulmarkcastillo.androidlogger

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.assent.Permission
import com.afollestad.assent.runWithPermissions
import com.codekidlabs.storagechooser.StorageChooser
import com.paulmarkcastillo.androidlogger.databinding.ActivityPmclogBinding
import dmax.dialog.SpotsDialog
import java.io.File

class PMCLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPmclogBinding
    private lateinit var progressDialog: AlertDialog
    private var tags = ArrayList<String>()

    private lateinit var adapterLogs: PMCLogAdapter
    private lateinit var adapterTags: ArrayAdapter<String>
    private lateinit var adapterPriorities: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_pmclog)
        setSupportActionBar(findViewById(R.id.toolbar))

        progressDialog = SpotsDialog.Builder().setContext(this).build()
        progressDialog.setCancelable(false)

        val layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )
        layoutManager.stackFromEnd = true
        binding.recyclerviewLogs.layoutManager = layoutManager

        binding.recyclerviewLogs.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )

        adapterLogs = PMCLogAdapter()
        binding.recyclerviewLogs.adapter = adapterLogs

        binding.buttonRefresh.setOnClickListener {
            refreshLogs()
        }

        adapterTags = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            tags
        )
        binding.spinnerTags.adapter = adapterTags

        adapterPriorities = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.priorities)
        )
        binding.spinnerPriority.adapter = adapterPriorities
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onResume() {
        super.onResume()
        refreshLogs()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_clear -> {
                deleteLogs()
                true
            }
            R.id.menu_export -> {
                exportLogs()
                true
            }
            R.id.menu_exit -> {
                finish()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun refreshLogs() {
        displayLogs(
            binding.spinnerPriority.selectedItem.toString(), if (binding.spinnerTags.selectedItemPosition > 0) {
                binding.spinnerTags.selectedItem.toString()
            } else {
                ""
            }
        )
    }

    private fun displayLogs(priority: String, tag: String) {
        progressDialog.show()
        val logsObservable = PMCLogger.getLogs(PMCLogger.getPriorityValue(priority), tag)
        logsObservable.observe(this, Observer<List<PMCLog>> { log ->
            adapterLogs.submitList(log)

            val tagsObservable = PMCLogger.getAllTagsObservable()
            tagsObservable.observe(this, Observer<List<String>> { tags ->
                this.tags.clear()
                this.tags.addAll(tags)
                adapterTags.notifyDataSetChanged()
                progressDialog.dismiss()
            })
        })
    }

    private fun deleteLogs() {
        progressDialog.show()
        val logsObservable = PMCLogger.deleteLogs()
        logsObservable.observe(this, Observer<List<PMCLog>> {
            binding.spinnerPriority.setSelection(0)
            refreshLogs()
        })
    }

    private fun exportLogs() {
        runWithPermissions(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE) {

            val chooser = StorageChooser.Builder()
                .withActivity(this)
                .withFragmentManager(fragmentManager)
                .withMemoryBar(true)
                .allowCustomPath(true)
                .setType(StorageChooser.DIRECTORY_CHOOSER)
                .build()

            chooser.setOnSelectListener { path ->
                PMCLogger.e("PMCLogActivity", "Selected Path: $path")
                val file = File(path + "/" + System.currentTimeMillis() + ".csv")
                progressDialog.show()
                val logsObservable = PMCLogger.exportCSV(file)
                logsObservable.observe(this, Observer<String> { status ->
                    progressDialog.dismiss()
                    if (status.isEmpty()) {
                        AlertDialog.Builder(this@PMCLogActivity)
                            .setTitle("Success")
                            .setMessage("Successfully Saved at: \n${file.absolutePath}")
                            .setPositiveButton(android.R.string.yes) { _, _ -> }
                            .show()
                    } else {
                        AlertDialog.Builder(this@PMCLogActivity)
                            .setTitle("Failed")
                            .setMessage(
                                "Failed to Save at: " +
                                        file.absolutePath + "\n" +
                                        "Message: " + status
                            )
                            .setPositiveButton(android.R.string.yes) { _, _ -> }
                            .show()
                    }
                })
            }
            chooser.show()
        }
    }
}
