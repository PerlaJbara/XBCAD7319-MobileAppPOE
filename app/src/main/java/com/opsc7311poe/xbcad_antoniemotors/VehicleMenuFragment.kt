package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class VehicleMenuFragment : Fragment() {

    private lateinit var btnVehicleReg: Button
    private lateinit var btnAddVehicleMakes: Button
    private lateinit var btnPartsInventory: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vehicle_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views here, where view is guaranteed to be non-null
        btnVehicleReg = view.findViewById(R.id.btnRegVehicle)
        btnAddVehicleMakes = view.findViewById(R.id.btnAddVehicleMake)
        //btnPartsInventory = view.findViewById(R.id.btnAddPart)

        // Set up button click listeners or other view logic here
        btnVehicleReg.setOnClickListener(){
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(SearchVehiclesFragment())
        }

        btnAddVehicleMakes.setOnClickListener(){
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(AddVehicleMakeModelPOR())
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}