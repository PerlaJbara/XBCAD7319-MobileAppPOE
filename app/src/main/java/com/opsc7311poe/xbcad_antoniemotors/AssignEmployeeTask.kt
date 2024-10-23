package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView


class AssignEmployeeTask : Fragment() {

    private lateinit var txtTaskName: TextView
    private lateinit var txtTaskDesc: TextView
    private lateinit var spVehicleChoice: Spinner
    private lateinit var spEmpChoice: Spinner
    private lateinit var txtSelectDueDate: TextView
    private lateinit var swTaskApproval: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_assign_employee_task, container, false)
    }



}