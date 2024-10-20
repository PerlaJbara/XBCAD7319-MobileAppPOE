package com.opsc7311poe.xbcad_antoniemotors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class VehicleCustomerAdapter(
    private var customers: List<CustomerData>,  // Use CustomerData instead of String
    private val onCustomerSelected: (CustomerData) -> Unit  // Pass CustomerData
) : RecyclerView.Adapter<VehicleCustomerAdapter.CustomerViewHolder>() {

    inner class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val customerName: TextView = itemView.findViewById(R.id.txtCustomerName)

        fun bind(customer: CustomerData) {
            customerName.text = "${customer.CustomerName} ${customer.CustomerSurname}" // Show full name
            itemView.setOnClickListener {
                onCustomerSelected(customer)  // Pass the entire CustomerData object
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

    fun updateCustomers(newCustomers: List<CustomerData>) {
        customers = newCustomers
        notifyDataSetChanged()
    }
}

