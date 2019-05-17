package com.paulmarkcastillo.androidlogger

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.*
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.paulmarkcastillo.androidlogger.databinding.ActivityPmclogBinding
import dmax.dialog.SpotsDialog

class PMCLogActivity : AppCompatActivity() {

    private lateinit var adapter: PMCLogAdapter
    private lateinit var progressDialog: AlertDialog
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private lateinit var tag: String
    private  var showWithTag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityPmclogBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_pmclog)

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
            if (showWithTag) {
                displayLogsWithSelectedTag(tag)
            } else {
                displayLogs()
            }

        }

        binding.buttonClear.setOnClickListener {
            deleteLogs()
        }

        setupSpinnerAdapter(tagsList(), binding.tagsSpnr)

        binding.tagsSpnr.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                showWithTag = position > 0
                tag = binding.tagsSpnr.selectedItem.toString()
            }

        }
    }

    override fun onResume() {
        super.onResume()
        displayLogs()
    }

    fun displayLogs() {
        progressDialog.show()
        val logs = PMCLogger.getLogsObservable()
        logs.observe(this, Observer<List<PMCLog>> {
            adapter.submitList(it)
            progressDialog.dismiss()
        })
    }

    fun deleteLogs() {
        progressDialog.show()
        val logs = PMCLogger.deleteLogs()
        logs.observe(this, Observer<List<PMCLog>> {
            adapter.submitList(it)
            progressDialog.dismiss()
        })
    }

    fun setupSpinnerAdapter(list: ArrayList<String>, spinner: Spinner) {
        spinnerAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item_layout,
            list
        )
        spinner.adapter = spinnerAdapter
    }

    fun tagsList(): ArrayList<String> {
        val tags = ArrayList<String>()
        val logs = PMCLogger.getTag()
        tags.add("All")
        logs.observe(this, Observer<List<String>> {
            it.forEach { tag ->
                tags.add(tag)
            }
            spinnerAdapter.notifyDataSetChanged()
        })
        return tags
    }

    fun displayLogsWithSelectedTag(tag: String) {
        progressDialog.show()
        val logs = PMCLogger.getLogsWithTag(tag)
        logs.observe(this, Observer<List<PMCLog>> {
            adapter.submitList(it)
            progressDialog.dismiss()
        })
    }
}
