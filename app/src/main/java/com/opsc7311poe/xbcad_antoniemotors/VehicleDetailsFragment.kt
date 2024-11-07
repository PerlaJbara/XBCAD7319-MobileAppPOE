package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class VehicleDetailsFragment : Fragment() {

    private lateinit var txtVehicleNumberPlate: TextView
    private lateinit var txtVehicleOwner: TextView
    private lateinit var txtVehicleModel: TextView
    private lateinit var txtVehicleKms: TextView
    private lateinit var txtVinNumber: TextView
    private lateinit var txtVehicleReg: TextView
    private lateinit var txtAdminFN: TextView // New TextView for Admin Full Name
    private lateinit var vehicleImagesRecyclerView: RecyclerView
    private lateinit var btnEditVehicleDetails: Button
    private lateinit var btnDeleteVehicle: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private var vehicleId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vehicleId = arguments?.getString("vehicleId")
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vehicle_details, container, false)

        txtVehicleNumberPlate = view.findViewById(R.id.txtNumberPlate)
        txtVehicleOwner = view.findViewById(R.id.txtVehicleOwner)
        txtVehicleModel = view.findViewById(R.id.txtVehicleModel)
        txtVehicleKms = view.findViewById(R.id.txtVehicleKms)
        txtVinNumber = view.findViewById(R.id.txtVinNumber)
        txtVehicleReg = view.findViewById(R.id.txtVehicleRegDate)
        txtAdminFN = view.findViewById(R.id.txtAdminFullName) // New TextView initialization
        btnEditVehicleDetails = view.findViewById(R.id.btnEditVehicle)
        btnDeleteVehicle = view.findViewById(R.id.btnDeleteVehicle)

        vehicleImagesRecyclerView = view.findViewById(R.id.vehicleImagesRecyclerView)
        vehicleImagesRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        searchVehicleAcrossBusinesses()

        return view
    }

    private fun searchVehicleAcrossBusinesses() {
        val usersReference = FirebaseDatabase.getInstance().getReference("Users")

        usersReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(usersSnapshot: DataSnapshot) {
                for (businessSnapshot in usersSnapshot.children) {
                    val vehiclesSnapshot = businessSnapshot.child("Vehicles").child(vehicleId ?: "")

                    if (vehiclesSnapshot.exists()) {
                        val vehicleData = vehiclesSnapshot.getValue(VehicleData::class.java)
                        vehicleData?.let {
                            displayVehicleDetails(it)
                        }
                        return  // Exit loop once vehicle is found
                    }
                }
                Toast.makeText(requireContext(), "Vehicle not found.", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error searching vehicle.", Toast.LENGTH_SHORT).show()
                Log.e("VehicleDetailsFragment", "Database error: ${error.message}")
            }
        })
    }

    private fun displayVehicleDetails(vehicle: VehicleData) {
        txtVehicleNumberPlate.text = vehicle.VehicleNumPlate
        txtVehicleOwner.text = vehicle.VehicleOwner
        txtVehicleModel.text = vehicle.VehicleModel
        txtVehicleKms.text = vehicle.VehicleKms
        txtVinNumber.text = vehicle.VinNumber
        txtVehicleReg.text = vehicle.registrationDate
        txtAdminFN.text = vehicle.AdminFullName // Set Admin Full Name from VehicleData

        loadVehicleImages(vehicle.images)
    }

    private fun loadVehicleImages(images: Map<String, String>) {
        val imageUrls = images.values.toList()
        vehicleImagesRecyclerView.adapter = VehicleImagesAdapter(imageUrls)
    }

    companion object {
        fun newInstance(vehicleId: String) =
            VehicleDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString("vehicleId", vehicleId)
                }
            }
    }
}

class VehicleImagesAdapter(private val imageUrls: List<String>) : RecyclerView.Adapter<VehicleImagesAdapter.VehicleImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_vehicle_images, parent, false)
        return VehicleImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: VehicleImageViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(imageUrls[position])
            .into(holder.vehicleImageView)
    }

    override fun getItemCount(): Int = imageUrls.size

    class VehicleImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val vehicleImageView: ImageView = itemView.findViewById(R.id.vehicleImageView)
    }
}
