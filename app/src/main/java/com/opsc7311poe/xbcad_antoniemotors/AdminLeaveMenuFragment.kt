package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView


class AdminLeaveMenuFragment : Fragment() {

    private lateinit var imgLeavess: ImageView
    private lateinit var imgleaveaps: ImageView
    private lateinit var btnBackButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_leave_menu, container, false)

        // Initialize ImageViews
        imgLeavess = view.findViewById(R.id.imgleaves)
        imgleaveaps = view.findViewById(R.id.imgleaveap)
        btnBackButton = view.findViewById(R.id.ivBackButton)

        // Set click listeners for the image views
        imgLeavess.setOnClickListener {
            replaceFragment(EMPLeaveListFragment()) // Replace with your actual fragment class
        }

        imgleaveaps.setOnClickListener {
            replaceFragment(EmployeeFragment()) // Replace with your actual fragment class
        }

        btnBackButton.setOnClickListener {
            replaceFragment(AdminEmpFragment()) // Replace with your actual fragment class
        }

        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }


}
