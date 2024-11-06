package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView


class AdminLeaveMenuFragment : Fragment() {


    private lateinit var imgLeavess: ImageView
    private lateinit var imgleaveaps: ImageView
    private lateinit var btnBackButton : ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ImageViews
        imgLeavess = view.findViewById(R.id.imgleavemang)
        imgleaveaps = view.findViewById(R.id.imgsearchandreg)
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




    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_leave_menu, container, false)
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }


}