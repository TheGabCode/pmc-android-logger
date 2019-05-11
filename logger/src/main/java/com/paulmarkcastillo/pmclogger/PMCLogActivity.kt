package com.paulmarkcastillo.pmclogger

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.paulmarkcastillo.pmclogger.databinding.ActivityPmclogBinding
import dmax.dialog.SpotsDialog

class PMCLogActivity : AppCompatActivity() {

    private lateinit var adapter: PMCLogAdapter
    private lateinit var progressDialog: AlertDialog

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
        binding.recyclerviewLogs.layoutManager = layoutManager

        binding.recyclerviewLogs.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )

        adapter = PMCLogAdapter()
        binding.recyclerviewLogs.adapter = adapter

        binding.buttonRefresh.setOnClickListener {
            displayLogs()
        }

        binding.buttonClear.setOnClickListener {
            deleteLogs()
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
}
