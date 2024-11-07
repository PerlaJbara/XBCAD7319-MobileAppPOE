package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_customer_details, container, false)
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
            // Handle vehicle selection if needed
        }
        rviewCustomerVehicles.layoutManager = LinearLayoutManager(requireContext())
        rviewCustomerVehicles.adapter = vehicleAdapter
        // Fetch vehicles belonging to the selected customer
        fetchCustomerVehicles(customerID)

        return view
    }

    private fun fetchCustomerVehicles(customerID: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Vehicles")

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val customerVehicles = ArrayList<VehicleData>()

                    for (vehicleSnapshot in snapshot.children) {
                        val vehicle = vehicleSnapshot.getValue(VehicleData::class.java)
                        vehicle?.let {
                            if (it.customerID == customerID) {
                                customerVehicles.add(it) // Only add vehicles belonging to the selected customer
                            }
                        }
                    }

                    if (customerVehicles.isEmpty()) {
                        Toast.makeText(requireContext(), "No vehicles found for this customer.", Toast.LENGTH_SHORT).show()
                    } else {
                        vehicleAdapter.updateList(customerVehicles) // Update adapter with customer vehicles
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error fetching vehicles.", Toast.LENGTH_SHORT).show()
                    Log.e("CustomerDetailsFragment", "Database error: ${error.message}")
                }
            })
        }
    }






    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }

}