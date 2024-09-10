package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentTransaction

class CustomerFragment : Fragment() {

    private lateinit var vectorPlusButton: ImageView
    private lateinit var btnBack: ImageView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_customer, container, false)

        vectorPlusButton = view.findViewById(R.id.btnPlus)

        // Set click listener for the button
        vectorPlusButton.setOnClickListener {
            // Replace the current fragment with AddCustomerFragment
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(AddCustomerFragment())
        }
        btnBack = view.findViewById(R.id.ivBackButton)

        btnBack.setOnClickListener() {
            replaceFragment(HomeFragment())
        }

        return view
    }




    // Function to replace the current fragment
    private fun replaceFragment(fragment: Fragment) {
        Log.d("CustomerFragment", "Replacing fragment: ${fragment::class.java.simpleName}")
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }
}
