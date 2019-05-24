package com.paulmarkcastillo.androidlogger

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
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
    private lateinit var adapter: PMCLogAdapter
    private lateinit var progressDialog: AlertDialog
    private lateinit var spinnerTagAdapter: ArrayAdapter<String>
    private lateinit var spinnerPriorityAdapter: ArrayAdapter<String>
    private var tags = ArrayList<String>()
    private val retainTag = ArrayList<String>()
    private var selectedTag: String = ""
    private var selectedPriority: String = ""
    private var displayLogsWithSelectedTag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_pmclog)

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

        adapter = PMCLogAdapter()
        binding.recyclerviewLogs.adapter = adapter

        binding.buttonRefresh.setOnClickListener {
            refreshFilteredLogs()
        }

        binding.buttonClear.setOnClickListener {
            deleteLogs()
            refreshFilteredLogs()
        }

        binding.spinnerTags.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                displayLogsWithSelectedTag = position > 0
                selectedTag = binding.spinnerTags.selectedItem.toString()
            }
        }

        binding.spinnerPriority.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedPriority = binding.spinnerPriority.selectedItem.toString()
            }
        }

        setupSpinnerTagAdapter()
        setupSpinnerPriorityAdapter()
    }

    override fun onResume() {
        super.onResume()
        refreshFilteredLogs()
    }

    private fun refreshFilteredLogs() {
        if (displayLogsWithSelectedTag) {
            displayFilteredPriorityAndTags(selectedPriority, selectedTag)
        }else{
            displayLogs(selectedPriority)
        }
    }

    private fun setupSpinnerTagAdapter() {
        spinnerTagAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item_layout,
            tags
        )
        binding.spinnerTags.adapter = spinnerTagAdapter
    }

    private fun setupSpinnerPriorityAdapter(){
        spinnerPriorityAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item_layout,
            resources.getStringArray(R.array.minimum_priority_array)
        )
        binding.spinnerPriority.adapter = spinnerPriorityAdapter
    }

    private fun displayLogs(priority: String) {
        progressDialog.show()
        val logs = PMCLogger.getLogsObservable(priority)
        logs.observe(this, Observer<List<PMCLog>> {
            if (it.isNotEmpty()) {
                displayTags()
            }
            adapter.submitList(it)
            progressDialog.dismiss()
        })
    }

    private fun displayFilteredPriorityAndTags(priority: String, tag: String){
        progressDialog.show()
        val logs = PMCLogger.getFilteredLogs(priority, tag)
        logs.observe(this, Observer<List<PMCLog>>{
            if (it.isEmpty()) {
                displayTags()
            }
            adapter.submitList(it)
            progressDialog.dismiss()
        })
    }

    private fun displayTags() {
        val result = PMCLogger.getAllTagsObservable()
        result.observe(this, Observer<List<String>> {
            tags.clear()
            tags.addAll(it)
            spinnerTagAdapter.notifyDataSetChanged()
        })
    }

    private fun deleteLogs() {
        progressDialog.show()
        val logs = PMCLogger.deleteLogs()
        logs.observe(this, Observer<List<PMCLog>> {
            adapter.submitList(it)
            displayLogsWithSelectedTag = false
            retainTag.apply {
                addAll(tags.filterIndexed { index, s ->
                    index == 0
                })
            }
            tags.clear()
            tags.addAll(retainTag)
            spinnerTagAdapter.notifyDataSetChanged()
            progressDialog.dismiss()

            setupSpinnerPriorityAdapter()
        })
    }
}
