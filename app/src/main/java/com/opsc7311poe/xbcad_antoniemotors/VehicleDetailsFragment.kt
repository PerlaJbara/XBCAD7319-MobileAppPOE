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
    private lateinit var vehicleImagesRecyclerView: RecyclerView
    private lateinit var btnEditVehicleDetails: Button
    private lateinit var btnDeleteVehicle: Button

    private lateinit var databaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var vehicleId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vehicleId = arguments?.getString("vehicleId")
        auth = FirebaseAuth.getInstance()

        if (vehicleId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Invalid Vehicle ID.", Toast.LENGTH_SHORT).show()
            // Optionally, navigate back or close the fragment
        } else {
            // Adjust Firebase path to Users -> userId -> Vehicles -> vehicleId
            databaseRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(auth.currentUser?.uid ?: "")
                .child("Vehicles")
                .child(vehicleId!!)
        }
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
        btnEditVehicleDetails = view.findViewById(R.id.btnEditVehicle)
        btnDeleteVehicle = view.findViewById(R.id.btnDeleteVehicle)


        vehicleImagesRecyclerView = view.findViewById(R.id.vehicleImagesRecyclerView)

        // Set an empty adapter initially
        vehicleImagesRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        loadVehicleDetails()
        loadVehicleImages()

        return view
    }

    private fun loadVehicleDetails() {
        vehicleId?.let {
            databaseRef.get().addOnSuccessListener { snapshot ->
                Log.d("VehicleDetails", "Snapshot: ${snapshot.value}")

                txtVehicleNumberPlate.text = snapshot.child("vehicleNumPlate").getValue(String::class.java) ?: "N/A"
                txtVehicleOwner.text = snapshot.child("vehicleOwner").getValue(String::class.java) ?: "N/A"
                txtVehicleModel.text = snapshot.child("vehicleModel").getValue(String::class.java) ?: "N/A"
                txtVehicleKms.text = snapshot.child("vehicleKms").getValue(String::class.java) ?: "N/A"
                txtVinNumber.text = snapshot.child("vinNumber").getValue(String::class.java) ?: "N/A"
                txtVehicleReg.text = snapshot.child("registrationDate").getValue(String::class.java) ?: "N/A"

            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load vehicle details.", Toast.LENGTH_SHORT).show()
                Log.e("VehicleDetails", "Error loading vehicle details: ${it.message}")
            }
        }
    }

    private fun loadVehicleImages() {
        vehicleId?.let {
            databaseRef.child("images").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val imageUrls = mutableListOf<String>()
                    for (imageSnapshot in snapshot.children) {
                        imageSnapshot.getValue(String::class.java)?.let { imageUrls.add(it) }
                    }
                    vehicleImagesRecyclerView.adapter = VehicleImagesAdapter(imageUrls)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load vehicle images.", Toast.LENGTH_SHORT).show()
                }
            })
        }
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
