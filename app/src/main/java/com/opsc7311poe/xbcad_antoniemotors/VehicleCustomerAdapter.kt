package com.opsc7311poe.xbcad_antoniemotors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class VehicleCustomerAdapter(
    private var customers: List<String>,  // Make this mutable to update the list
    private val onCustomerSelected: (String) -> Unit
) : RecyclerView.Adapter<VehicleCustomerAdapter.CustomerViewHolder>() {

    inner class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val customerName: TextView = itemView.findViewById(R.id.txtCustomerName)

        fun bind(customer: String) {
            customerName.text = customer
            itemView.setOnClickListener {
                onCustomerSelected(customer)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer, parent, false)
        return CustomerViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        holder.bind(customers[position])
    }

    override fun getItemCount(): Int = customers.size

    // Method to update customers
    fun updateCustomers(newCustomers: List<String>) {
        customers = newCustomers
        notifyDataSetChanged()  // Notify adapter of data change
    }
}
