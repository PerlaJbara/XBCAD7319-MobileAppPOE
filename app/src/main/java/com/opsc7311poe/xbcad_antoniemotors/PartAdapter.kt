package com.opsc7311poe.xbcad_antoniemotors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class PartAdapter(private val onItemClick: (PartsData) -> Unit) : ListAdapter<PartsData, PartAdapter.PartViewHolder>(PartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.stockviewlayout, parent, false)
        return PartViewHolder(view)
    }

    override fun onBindViewHolder(holder: PartViewHolder, position: Int) {
        val part = getItem(position)
        holder.bind(part)
        // Set click listener on the item view
        holder.itemView.setOnClickListener {
            onItemClick(part) // Pass the clicked item to the listener
        }
    }

    class PartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val partNameTextView: TextView = itemView.findViewById(R.id.tvPartName)
        private val partDescriptionTextView: TextView = itemView.findViewById(R.id.tvPartDescription)
        private val partCostTextView: TextView = itemView.findViewById(R.id.tvPartCost)

        fun bind(part: PartsData) {
            partNameTextView.text = part.partName
            partDescriptionTextView.text = part.partDescription
            partCostTextView.text = "R ${part.costPrice}"
        }
    }

    class PartDiffCallback : DiffUtil.ItemCallback<PartsData>() {
        override fun areItemsTheSame(oldItem: PartsData, newItem: PartsData): Boolean {
            return oldItem.partName == newItem.partName
        }

        override fun areContentsTheSame(oldItem: PartsData, newItem: PartsData): Boolean {
            return oldItem == newItem
        }
    }
}
