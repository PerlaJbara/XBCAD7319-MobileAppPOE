package com.opsc7311poe.xbcad_antoniemotors

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.opsc7311poe.xbcad_antoniemotors.VehicleAdapter.VehicleViewHolder

class SearchVehiclesFragment : Fragment() {

    private lateinit var vectorPlusButton: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var vehicleAdapter: VehicleAdapter
    private lateinit var vehicleList: ArrayList<VehicleData>
    private lateinit var searchVehicle: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_vehicles, container, false)

        vectorPlusButton = view.findViewById(R.id.btnPlus)
        btnBack = view.findViewById(R.id.ivBackButton)
        searchVehicle = view.findViewById(R.id.vehicleSearch)
        recyclerView = view.findViewById(R.id.recyclerViewVehicle)
        
        // Set click listener for the "Add Vehicle" button
        vectorPlusButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(RegisterVehicleFragment())
        }

        // Set click listener for the "Back" button
        btnBack.setOnClickListener {
            replaceFragment(HomeFragment())
        }

        vehicleList = ArrayList()
        vehicleAdapter = VehicleAdapter(vehicleList) { selectedVehicle ->
            openVehicleDetailsFragment(selectedVehicle) // Updated to pass vehicleId
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = vehicleAdapter

        searchVehicle.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                vehicleAdapter.filterList(newText) // Use the filterList method in VehicleAdapter
                return false
            }
        })

        // Fetch vehicle data
        fetchVehicles()

        return view
    }


    private fun fetchVehicles() {
        val adminId = FirebaseAuth.getInstance().currentUser?.uid

        if (adminId != null) {
            // Reference to the "Users" node in Firebase
            val usersReference = FirebaseDatabase.getInstance().getReference("Users")

            // Locate the business associated with this admin
            usersReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(usersSnapshot: DataSnapshot) {
                    var businessID: String? = null

                    // Iterate over each business node to find where adminId exists under Employees
                    for (businessSnapshot in usersSnapshot.children) {
                        val employeeSnapshot = businessSnapshot.child("Employees").child(adminId)

                        if (employeeSnapshot.exists()) {
                            // Found the employee record; extract the associated BusinessID
                            businessID = employeeSnapshot.child("businessID").getValue(String::class.java)
                            Log.d("fetchVehicles", "BusinessID found for admin: $businessID")
                            break
                        }
                    }

                    if (businessID != null) {
                        // Now use BusinessID to retrieve vehicles under the correct business
                        val vehicleReference = usersReference.child(businessID).child("Vehicles")

                        vehicleReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                vehicleList.clear()

                                if (!snapshot.exists()) {
                                    Toast.makeText(requireContext(), "No saved vehicles found.", Toast.LENGTH_SHORT).show()
                                    return
                                }

                                // Retrieve each vehicle and add to the list
                                for (vehicleSnapshot in snapshot.children) {
                                    val vehicle = vehicleSnapshot.getValue(VehicleData::class.java)

                                    vehicle?.let {
                                        it.vehicleId = vehicleSnapshot.key ?: "" // Assign vehicleId from snapshot key
                                        vehicleList.add(it)
                                    }
                                }

                                if (vehicleList.isEmpty()) {
                                    Toast.makeText(requireContext(), "No saved vehicles found.", Toast.LENGTH_SHORT).show()
                                } else {
                                    vehicleAdapter.notifyDataSetChanged()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(requireContext(), "Error fetching vehicles.", Toast.LENGTH_SHORT).show()
                                Log.e("SearchVehiclesFragment", "Database error: ${error.message}")
                            }
                        })
                    } else {
                        Log.e("fetchVehicles", "BusinessID not found for the current admin.")
                        Toast.makeText(requireContext(), "Unable to find associated business.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("fetchVehicles", "Error fetching business information: ${error.message}")
                    Toast.makeText(requireContext(), "Error fetching business information.", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
        }
    }




    private fun openVehicleDetailsFragment(vehicle: VehicleData) {
        if (vehicle.vehicleId.isEmpty()) {
            Toast.makeText(requireContext(), "Invalid Vehicle ID.", Toast.LENGTH_SHORT).show()
            return
        }

        val fragment = VehicleDetailsFragment().apply {
            arguments = Bundle().apply {
                putString("vehicleId", vehicle.vehicleId) // Pass vehicleId
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }



    // Replaces the current fragment with the specified fragment
    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
