package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView


class CustomerDetailsFragment : Fragment() {
    private lateinit var btnBack: ImageView
    private lateinit var txtCustomerName: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtAddress: TextView
    private lateinit var txtCellNumber: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_customer_details, container, false)
        btnBack = view.findViewById(R.id.ivBackButton)
        txtCustomerName = view.findViewById(R.id.txtCustomerName)
        txtEmail = view.findViewById(R.id.txtEmail)
        txtAddress = view.findViewById(R.id.txtAddress)
        txtCellNumber = view.findViewById(R.id.txtCellNumber)

        // Retrieve customer details from arguments
        val customerName = arguments?.getString("customerName") ?: ""
        val customerSurname = arguments?.getString("customerSurname") ?: ""
        val customerEmail = arguments?.getString("customerEmail") ?: ""
        val customerAddress = arguments?.getString("customerAddress") ?: ""
        val customerMobile = arguments?.getString("customerMobile") ?: ""

        // Set customer information in TextViews
        txtCustomerName.text = "$customerName $customerSurname"
        txtEmail.text = customerEmail
        txtAddress.text = customerAddress
        txtCellNumber.text = customerMobile

        // Back button click listener
        btnBack.setOnClickListener {
            replaceFragment(CustomerFragment())
        }

        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }

}