package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView

class HomeFragment : Fragment() {

    private lateinit var viewCustomersBtn: Button
    private lateinit var btnSettings: ImageView
    private lateinit var btnRegVehicle: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize the button
        viewCustomersBtn = view.findViewById(R.id.btnViewCustomers)
        btnSettings = view.findViewById(R.id.ivSettings)
        btnRegVehicle = view.findViewById(R.id.btnRegisterVehicle)


        // Set up the button click event to navigate to CustomerFragment
        viewCustomersBtn.setOnClickListener {
            // Replace the current fragment with CustomerFragment
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(CustomerFragment())
        }

        btnSettings.setOnClickListener {
            // Replace the current fragment with CustomerFragment
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(SettingsFragment())
        }

        btnRegVehicle.setOnClickListener {
            // Replace the current fragment with CustomerFragment
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(RegisterVehicleFragment())
        }



        return view
    }

    // Function to replace the current fragment
    private fun replaceFragment(fragment: Fragment) {
        Log.d("HomeFragment", "Replacing fragment: ${fragment::class.java.simpleName}")
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }
}
