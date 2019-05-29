package com.paulmarkcastillo.androidlogger

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.paulmarkcastillo.androidlogger.databinding.ActivityPmclogBinding
import dmax.dialog.SpotsDialog

class PMCLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPmclogBinding
    private lateinit var logAdapter: PMCLogAdapter
    private lateinit var tagAdapter: ArrayAdapter<String>
    private lateinit var priorityAdapter: ArrayAdapter<String>
    private lateinit var tags: ArrayList<String>
    private lateinit var progressDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_pmclog)

        progressDialog = SpotsDialog.Builder().setContext(this).build()
        progressDialog.setCancelable(false)

        tags = ArrayList()

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

        logAdapter = PMCLogAdapter()
        binding.recyclerviewLogs.adapter = logAdapter

        binding.buttonRefresh.setOnClickListener {
            refreshLogs()
        }

        binding.buttonClear.setOnClickListener {
            deleteLogs()
        }

        setupSpinnerTagAdapter()
        setupSpinnerPriorityAdapter()
    }

    override fun onResume() {
        super.onResume()
        refreshLogs()
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

    private fun setupSpinnerTagAdapter() {
        tagAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item_layout,
            tags
        )
        binding.spinnerTags.adapter = tagAdapter
    }

    private fun setupSpinnerPriorityAdapter() {
        priorityAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item_layout,
            resources.getStringArray(R.array.minimum_priority_array)
        )
        binding.spinnerPriority.adapter = priorityAdapter
    }

    private fun displayLogs(priority: String, tag: String) {
        progressDialog.show()
        val logs = PMCLogger.getLogs(PMCLogger.getPriorityValue(priority), tag)
        logs.observe(this, Observer<List<PMCLog>> {
            displayTags()
            logAdapter.submitList(it)
            progressDialog.dismiss()
        })
    }

    private fun displayTags() {
        val result = PMCLogger.getAllTagsObservable()
        result.observe(this, Observer<List<String>> {
            tags.clear()
            tags.addAll(it)
            tagAdapter.notifyDataSetChanged()
        })
    }

    private fun deleteLogs() {
        progressDialog.show()
        val logs = PMCLogger.deleteLogs()
        logs.observe(this, Observer<List<PMCLog>> {
            logAdapter.submitList(it)
            val retainTag = ArrayList<String>().apply {
                addAll(tags.filterIndexed { index, _ ->
                    index == 0
                })
            }
            tags.clear()
            tags.addAll(retainTag)
            tagAdapter.notifyDataSetChanged()
            progressDialog.dismiss()
            binding.spinnerPriority.setSelection(0)
        })
    }
}
