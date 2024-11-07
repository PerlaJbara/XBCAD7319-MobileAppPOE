package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CustomerDetailsFragment : Fragment() {
    private lateinit var btnBack: ImageView
    private lateinit var txtCustomerName: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtAddress: TextView
    private lateinit var txtCellNumber: TextView
    private lateinit var txtCustType: TextView
    private lateinit var rviewCustomerVehicles: RecyclerView
    private lateinit var vehicleAdapter: VehicleAdapter
    private lateinit var vehicleList: ArrayList<VehicleData>
    private var businessId: String? = null  // Store business ID for reuse

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_customer_details, container, false)

        // Initialize UI elements
        btnBack = view.findViewById(R.id.ivBackButton)
        txtCustomerName = view.findViewById(R.id.txtCustomerName)
        txtEmail = view.findViewById(R.id.txtEmail)
        txtAddress = view.findViewById(R.id.txtAddress)
        txtCellNumber = view.findViewById(R.id.txtCellNumber)
        txtCustType = view.findViewById(R.id.txtCustomerType)
        rviewCustomerVehicles = view.findViewById(R.id.rvCustomerVehicles)

        // Retrieve customer details from arguments
        val customerID = arguments?.getString("customerID") ?: ""
        val customerName = arguments?.getString("customerName") ?: ""
        val customerSurname = arguments?.getString("customerSurname") ?: ""
        val customerEmail = arguments?.getString("customerEmail") ?: ""
        val customerAddress = arguments?.getString("customerAddress") ?: ""
        val customerMobile = arguments?.getString("customerMobile") ?: ""
        val customerType = arguments?.getString("customerType") ?: ""

        // Set customer information in TextViews
        txtCustomerName.text = "$customerName $customerSurname"
        txtEmail.text = customerEmail
        txtAddress.text = customerAddress
        txtCellNumber.text = customerMobile
        txtCustType.text = customerType

        // Back button click listener
        btnBack.setOnClickListener {
            replaceFragment(CustomerFragment())
        }

        vehicleList = ArrayList()
        vehicleAdapter = VehicleAdapter(vehicleList) { selectedVehicle ->
            businessId?.let { openVehicleDetailsFragment(selectedVehicle, it) }  // Ensure businessId is not null
        }
        rviewCustomerVehicles.layoutManager = LinearLayoutManager(requireContext())
        rviewCustomerVehicles.adapter = vehicleAdapter

        // Fetch vehicles belonging to the selected customer
        fetchCustomerVehicles(customerID)

        return view
    }

    private fun fetchCustomerVehicles(customerID: String) {
        val adminId = FirebaseAuth.getInstance().currentUser?.uid

        if (adminId != null) {
            val usersReference = FirebaseDatabase.getInstance().getReference("Users")

            usersReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(usersSnapshot: DataSnapshot) {
                    // Find the business ID associated with this admin
                    for (businessSnapshot in usersSnapshot.children) {
                        val employeeSnapshot = businessSnapshot.child("Employees").child(adminId)
                        if (employeeSnapshot.exists()) {
                            businessId = businessSnapshot.key  // Save businessId for use in other functions
                            break
                        }
                    }

                    if (businessId != null) {
                        // Reference to the Vehicles under the business
                        val vehicleReference = usersReference.child(businessId!!).child("Vehicles")

                        vehicleReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val customerVehicles = ArrayList<VehicleData>()

                                for (vehicleSnapshot in snapshot.children) {
                                    val vehicle = vehicleSnapshot.getValue(VehicleData::class.java)
                                    vehicle?.let {
                                        // Match the vehicle's customerID with the selected customer's ID
                                        if (it.customerID == customerID) {
                                            it.vehicleId = vehicleSnapshot.key ?: ""
                                            customerVehicles.add(it)
                                        }
                                    }
                                }

                                if (customerVehicles.isEmpty()) {
                                    Toast.makeText(requireContext(), "No vehicles found for this customer.", Toast.LENGTH_SHORT).show()
                                } else {
                                    vehicleAdapter.updateList(customerVehicles)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(requireContext(), "Error fetching vehicles.", Toast.LENGTH_SHORT).show()
                                Log.e("CustomerDetailsFragment", "Database error: ${error.message}")
                            }
                        })
                    } else {
                        Log.e("fetchCustomerVehicles", "BusinessID not found for current admin.")
                        Toast.makeText(requireContext(), "Unable to find associated business.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("fetchCustomerVehicles", "Error fetching business information: ${error.message}")
                    Toast.makeText(requireContext(), "Error fetching business information.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun openVehicleDetailsFragment(vehicle: VehicleData, businessId: String) {
        if (vehicle.vehicleId.isEmpty()) {
            Toast.makeText(requireContext(), "Invalid Vehicle ID.", Toast.LENGTH_SHORT).show()
            return
        }

        val fragment = VehicleDetailsFragment().apply {
            arguments = Bundle().apply {
                putString("vehicleId", vehicle.vehicleId)
                putString("businessId", businessId)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
