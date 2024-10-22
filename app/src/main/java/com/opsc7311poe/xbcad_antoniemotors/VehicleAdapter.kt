package com.opsc7311poe.xbcad_antoniemotors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class VehicleAdapter(
    private var vehicleList: List<VehicleData>,
    private val onVehicleClick: (VehicleData) -> Unit
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    private var filteredList: List<VehicleData> = vehicleList

    // Method to update the vehicle list dynamically
    fun updateList(newVehicleList: List<VehicleData>) {
        vehicleList = newVehicleList
        filteredList = newVehicleList
        notifyDataSetChanged() // Refresh the RecyclerView
    }

    fun filterList(query: String?) {
        filteredList = if (!query.isNullOrEmpty()) {
            vehicleList.filter {
                it.VehicleNumPlate.contains(query, ignoreCase = true) ||
                        it.VehicleModel.contains(query, ignoreCase = true) ||
                        it.VehicleOwner.contains(query, ignoreCase = true)
            }
        } else {
            vehicleList
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = filteredList.size

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    inner class VehicleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtVehicleNumPlate: TextView = view.findViewById(R.id.txtVehicleNumPlate)
        val txtVehicleModel: TextView = view.findViewById(R.id.txtVehicleModel)
        val txtVehicleOwner: TextView = view.findViewById(R.id.txtVehicleOwner)
        val imgVehicle: ImageView = view.findViewById(R.id.imgVehicle)

        fun bind(vehicle: VehicleData) {
            txtVehicleNumPlate.text = vehicle.VehicleNumPlate
            txtVehicleModel.text = vehicle.VehicleModel
            txtVehicleOwner.text = vehicle.VehicleOwner

            // Load the first image from the images map if available
            if (vehicle.images.isNotEmpty()) {
                val firstImageUrl = vehicle.images.values.first()
                Glide.with(imgVehicle.context)
                    .load(firstImageUrl) // Load the first image URL
                    .into(imgVehicle)
            } else {
                imgVehicle.setImageResource(R.drawable.vehicledetails) // Fallback image
            }

            itemView.setOnClickListener { onVehicleClick(vehicle) }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.vehicle_snippet, parent, false)
        return VehicleViewHolder(view)
    }
}
