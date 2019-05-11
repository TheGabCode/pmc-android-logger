package com.paulmarkcastillo.pmclogger

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.paulmarkcastillo.pmclogger.databinding.ListItemPmclogBinding
import com.paulmarkcastillo.pmclogger.logger.PMCLogger
import java.util.*

class PMCLogAdapter : ListAdapter<PMCLog, PMCLogAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<PMCLog>() {
        override fun areItemsTheSame(oldItem: PMCLog, newItem: PMCLog): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PMCLog, newItem: PMCLog): Boolean {
            return oldItem.msg == newItem.msg
        }
    }) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ListItemPmclogBinding>(
            LayoutInflater.from(parent.context),
            R.layout.list_item_pmclog,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ListItemPmclogBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(log: PMCLog) {
            binding.textviewId.text = "#${log.id}"
            binding.textviewTimestamp.text = Date(log.timestamp).toString()
            binding.textviewTags.text = log.tag
            binding.textviewPriority.text = PMCLogger.getPriorityText(log.priority)
            binding.textviewMessage.text = log.msg
        }
    }
}

