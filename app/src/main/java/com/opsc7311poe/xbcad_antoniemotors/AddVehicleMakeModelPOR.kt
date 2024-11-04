package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

private lateinit var database: DatabaseReference
private lateinit var btnBack: ImageView
private lateinit var edtNewVehicleMake: EditText
private lateinit var edtSelectedVehicleMake: EditText
private lateinit var edtNewModel: EditText
private lateinit var edtNewProvinceAreaName: EditText
private lateinit var edtNewPOR: EditText
private lateinit var btnAddNewVMake: Button
private lateinit var btnAddNewVModel: Button
private lateinit var btnAddNewPOR: Button

class AddVehicleMakeModelPOR : Fragment() {

    private val vehicleMakesList = mutableListOf<String>()
    private lateinit var databaseVRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_vehicle_make_model_p_o_r, container, false)

        // Initialize views
        edtNewVehicleMake = view.findViewById(R.id.edttextNewVehicleMake)
        edtSelectedVehicleMake = view.findViewById(R.id.edttxtSelectedVehicleMake)
        edtNewModel = view.findViewById(R.id.edttxtNewVehicleModel)
        edtNewProvinceAreaName = view.findViewById(R.id.edttxtProvince)
        edtNewPOR = view.findViewById(R.id.edttxtAddPOR)
        btnAddNewVMake = view.findViewById(R.id.btnAddNewVehicleMake)
        btnAddNewVModel = view.findViewById(R.id.btnAddVehicleModel)
        btnAddNewPOR = view.findViewById(R.id.btnAddPOR)
        btnBack = view.findViewById(R.id.ivBackButton)

        databaseVRef = FirebaseDatabase.getInstance().getReference("VehicleMake")

        // Fetch vehicle makes when fragment starts
        fetchVehicleMakes()

        // Handle adding a new vehicle make
        btnAddNewVMake.setOnClickListener {
            val newVehicleMake = edtNewVehicleMake.text.toString().trim()
            if (newVehicleMake.isNotEmpty() && newVehicleMake.matches(Regex("^[a-zA-Z0-9]*$"))) {
                checkAndAddVehicleMake(newVehicleMake)
            } else {
                Toast.makeText(requireContext(), "Invalid Vehicle Make. No special characters allowed.", Toast.LENGTH_SHORT).show()
            }
        }


        // **Set OnClickListener for edtSelectedVehicleMake to show dialog**
        edtSelectedVehicleMake.setOnClickListener {
            showVehicleMakeDialog()  // Call the function to show the vehicle make selection dialog
        }

        // Handle adding a new vehicle model
        btnAddNewVModel.setOnClickListener {
            val selectedMake = edtSelectedVehicleMake.text.toString().trim()
            val newModel = edtNewModel.text.toString().trim()
            if (selectedMake.isNotEmpty() && newModel.isNotEmpty() && newModel.matches(Regex("^[a-zA-Z0-9]*$"))) {
                checkAndAddVehicleModel(selectedMake, newModel)
            } else {
                Toast.makeText(requireContext(), "Please fill in both fields. No special characters allowed in the model.", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle adding a new province or area registration code
        btnAddNewPOR.setOnClickListener {
            val newProvince = edtNewProvinceAreaName.text.toString().trim()
            val newPOR = edtNewPOR.text.toString().trim().uppercase()
            if (newProvince.isNotEmpty() && newPOR.isNotEmpty() && newPOR.length <= 3 && newPOR.matches(Regex("^[A-Z]*$"))) {
                checkAndAddPOR(newProvince, newPOR)
            } else {
                Toast.makeText(requireContext(), "Invalid Province or POR Code. POR Code must be letters only, max 3 characters.", Toast.LENGTH_SHORT).show()
            }
        }

        // Back button
        btnBack.setOnClickListener {
            replaceFragment(VehicleMenuFragment())
        }

        return view
    }

    // Check and add vehicle make if it doesn't already exist
    private fun checkAndAddVehicleMake(make: String) {
        databaseVRef.child(make).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(requireContext(), "Vehicle Make already exists.", Toast.LENGTH_SHORT).show()
                } else {
                    databaseVRef.child(make).setValue(VehicleMakeData(make))
                    Toast.makeText(requireContext(), "Vehicle Make added.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error: ${error.message}")
            }
        })
    }

    // Check and add vehicle model under the selected vehicle make
    private fun checkAndAddVehicleModel(make: String, model: String) {
        val makeRef = databaseVRef.child(make).child("Models")
        makeRef.orderByValue().equalTo(model).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(requireContext(), "Model already exists under $make.", Toast.LENGTH_SHORT).show()
                } else {
                    val newModelKey = makeRef.push().key ?: return
                    makeRef.child(newModelKey).setValue(model)
                    Toast.makeText(requireContext(), "Model added under $make.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error: ${error.message}")
            }
        })
    }

    // Check and add Province/Area Registration code
    private fun checkAndAddPOR(province: String, por: String) {
        val porRef = FirebaseDatabase.getInstance().getReference("VehiclePOR")
        porRef.child(province).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(requireContext(), "Province/Area name already exists.", Toast.LENGTH_SHORT).show()
                } else {
                    porRef.child(province).setValue(ProvinceCodeData(province, por))
                    Toast.makeText(requireContext(), "Province/Area and POR Code added.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error: ${error.message}")
            }
        })
    }

    // Fetch vehicle makes to display in the dialog
    private fun fetchVehicleMakes() {
        databaseVRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                vehicleMakesList.clear()
                for (childSnapshot in snapshot.children) {
                    val vehicleMake = childSnapshot.key
                    vehicleMake?.let { vehicleMakesList.add(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching vehicle makes: ${error.message}")
            }
        })
    }

    fun showVehicleMakeDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_vehicle_make, null)
        val searchView = dialogView.findViewById<SearchView>(R.id.searchVehicleMake)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerVehicleMake)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Select Vehicle Make")
            .setNegativeButton("Cancel", null)
            .setCancelable(true)
            .create()

        val adapter = VehicleMakeAdapter(vehicleMakesList) { selectedMake ->
            edtSelectedVehicleMake.setText(selectedMake)
            alertDialog.dismiss()
        }

        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        alertDialog.show()
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }
}