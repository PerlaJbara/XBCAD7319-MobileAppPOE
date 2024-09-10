package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RegisterVehicleFragment : Fragment() {

    private lateinit var spinCustomer: Spinner
    private lateinit var edtVehicleNoPlate: EditText
    private lateinit var edtVehicleModel: EditText
    private lateinit var edtVinNumber: EditText
    private lateinit var edtVehicleKms: EditText
    private lateinit var btnSubmitRegVehicle: Button
    private var selectedCustomerId: String = ""

    private lateinit var btnBack: ImageView

    // Firebase database reference
    private val database = FirebaseDatabase.getInstance().getReference("Vehicles")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register_vehicle, container, false)

        // Initialize views
        spinCustomer = view.findViewById(R.id.spinCustomer)
        edtVehicleNoPlate = view.findViewById(R.id.edttxtVehicleNoPlate)
        edtVehicleModel = view.findViewById(R.id.edttxtVehicleModel)
        edtVinNumber = view.findViewById(R.id.edttxtVinNumber)
        edtVehicleKms = view.findViewById(R.id.edttxtVehicleKms)
        btnSubmitRegVehicle = view.findViewById(R.id.btnSubmitRegVehicle)

        // Load customer names into spinner
        loadCustomerNames()

        // Set up button click listener
        btnSubmitRegVehicle.setOnClickListener {
            registerVehicle()
        }

        btnBack = view.findViewById(R.id.ivBackButton)

        btnBack.setOnClickListener(){
            replaceFragment(HomeFragment())
        }

        return view
    }




    private fun loadCustomerNames() {
        val customerList = mutableListOf<String>()
        val customerIds = mutableListOf<String>()
        val customerRef = FirebaseDatabase.getInstance().getReference("Customers")

        customerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (customerSnapshot in snapshot.children) {
                    val customerName = customerSnapshot.child("name").getValue(String::class.java)
                    val customerId = customerSnapshot.key
                    if (customerName != null && customerId != null) {
                        customerList.add(customerName)
                        customerIds.add(customerId)
                    }
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, customerList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinCustomer.adapter = adapter

                // Store customer IDs for reference when saving vehicle
                spinCustomer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        selectedCustomerId = customerIds[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load customers", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun registerVehicle() {
        val vehicleNoPlate = edtVehicleNoPlate.text.toString().trim()
        val vehicleModel = edtVehicleModel.text.toString().trim()
        val vinNumber = edtVinNumber.text.toString().trim()
        val vehicleKms = edtVehicleKms.text.toString().trim()

        // Error checking
        if (vehicleNoPlate.isEmpty()) {
            edtVehicleNoPlate.error = "Please enter the vehicle number plate"
            return
        }
        if (vehicleModel.isEmpty()) {
            edtVehicleModel.error = "Please enter the vehicle model"
            return
        }
        if (vinNumber.isEmpty()) {
            edtVinNumber.error = "Please enter the VIN number"
            return
        }
        if (vehicleKms.isEmpty()) {
            edtVehicleKms.error = "Please enter the vehicle kilometers"
            return
        }

        // Create vehicle data object
        val vehicle = VehicleData(
            VehicleOwner = selectedCustomerId, // Link vehicle with customer ID
            VehicleNumPlate = vehicleNoPlate,
            VehicleModel = vehicleModel,
            VinNumber = vinNumber,
            VehicleKms = vehicleKms
        )

        // Save vehicle data in Firebase under the customer node
        database.child(selectedCustomerId).child("Vehicles").push().setValue(vehicle)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Vehicle registered successfully", Toast.LENGTH_SHORT).show()
                    //clear input fields
                    edtVehicleNoPlate.text.clear()
                    edtVehicleModel.text.clear()
                    edtVinNumber.text.clear()
                    edtVehicleKms.text.clear()
                } else {
                    Toast.makeText(context, "Failed to register vehicle. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }

}

