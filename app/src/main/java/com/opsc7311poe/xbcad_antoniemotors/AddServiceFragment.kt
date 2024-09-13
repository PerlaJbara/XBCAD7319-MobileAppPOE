package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner


class AddServiceFragment : Fragment() {
    private lateinit var spinStatus: Spinner
    private lateinit var btnBack: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view = inflater.inflate(R.layout.fragment_add_service, container, false)

        //handling back button
        btnBack = view.findViewById(R.id.ivBackButton)

        btnBack.setOnClickListener() {
            replaceFragment(ServicesFragment())
        }

        //populating spinners
        //status spinner
        spinStatus = view.findViewById(R.id.spinStatus)

        val statuses = arrayOf("Not Started", "Busy", "Completed")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statuses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinStatus.adapter = adapter

        //customer spinner



        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }

}