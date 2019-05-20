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
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private val tags = ArrayList<String>()
    private var selectedTag: String = ""
    private  var showWithTag = false

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
            refresh()
        }

        binding.buttonClear.setOnClickListener {
            deleteLogs()
        }

        binding.spinnerTags.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                showWithTag = position > 0
                selectedTag = binding.spinnerTags.selectedItem.toString()
                refresh()
            }
        }

        setupSpinnerAdapter()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        if (showWithTag) {
            displayLogsWithSelectedTag(selectedTag)
        } else {
            displayLogs()
        }
    }

    private fun setupSpinnerAdapter() {
        spinnerAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item_layout,
            tags
        )
        binding.spinnerTags.adapter = spinnerAdapter
    }

    private fun displayLogs() {
        progressDialog.show()
        val logs = PMCLogger.getLogsObservable()
        logs.observe(this, Observer<List<PMCLog>> {
            if (it.isNotEmpty()) {
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
            spinnerAdapter.notifyDataSetChanged()
        })
    }

    private fun displayLogsWithSelectedTag(tag: String) {
        progressDialog.show()
        val logs = PMCLogger.getLogsWithTagObservable(tag)
        logs.observe(this, Observer<List<PMCLog>> {
            adapter.submitList(it)
            progressDialog.dismiss()
        })
    }

    private fun deleteLogs() {
        progressDialog.show()
        val logs = PMCLogger.deleteLogs()
        logs.observe(this, Observer<List<PMCLog>> {
            adapter.submitList(it)
            showWithTag = false
            spinnerAdapter.clear()
            progressDialog.dismiss()
        })
    }
}
