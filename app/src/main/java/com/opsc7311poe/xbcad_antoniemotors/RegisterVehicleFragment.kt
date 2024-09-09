package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.database.FirebaseDatabase

class RegisterVehicleFragment : Fragment() {

    private lateinit var spinCustomer: Spinner
    private lateinit var edtVehicleNoPlate: EditText
    private lateinit var edtVehicleModel: EditText
    private lateinit var edtVinNumber: EditText
    private lateinit var edtVehicleKms: EditText
    private lateinit var btnSubmitRegCustomer: Button

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
        btnSubmitRegCustomer = view.findViewById(R.id.btnSubmitRegCustomer)

        // Set up button click listener
        btnSubmitRegCustomer.setOnClickListener {
            registerVehicle()
        }

        return view
    }

    private fun registerVehicle() {
        val owner = spinCustomer.selectedItem.toString()
        val vehicleNoPlate = edtVehicleNoPlate.text.toString().trim()
        val vehicleModel = edtVehicleModel.text.toString().trim()
        val vinNumber = edtVinNumber.text.toString().trim()
        val vehicleKms = edtVehicleKms.text.toString().trim()

        if (owner.isEmpty() || vehicleNoPlate.isEmpty() || vehicleModel.isEmpty() || vinNumber.isEmpty() || vehicleKms.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Create a map to store vehicle data
        val vehicleData = hashMapOf(
            "owner" to owner,
            "vehicleNoPlate" to vehicleNoPlate,
            "vehicleModel" to vehicleModel,
            "vinNumber" to vinNumber,
            "vehicleKms" to vehicleKms
        )

        // Push the map to Firebase
        val newVehicleRef = database.push()
        newVehicleRef.setValue(vehicleData)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Vehicle registered successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Failed to register vehicle: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}

