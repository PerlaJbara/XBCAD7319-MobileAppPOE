package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

class DocumentationFragment : Fragment() {

    private lateinit var invoice: ImageView
    private lateinit var viewInvoces : ImageView
    private lateinit var receipts: ImageView
    private lateinit var viewReceipts: ImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_documentation, container, false)

        // Find the buttons in the layout
        invoice = view.findViewById(R.id.btnQuotes)
        viewInvoces = view.findViewById(R.id.btnpastQuotes)
        receipts = view.findViewById(R.id.btnInvoiceGen)
        viewReceipts = view.findViewById(R.id.btnViewInvoices)

        // Set click listeners and replace fragments
        invoice.setOnClickListener {
            replaceFragment(QuoteGenFragment())
        }

        viewInvoces.setOnClickListener {
            replaceFragment(PastQuotesFragment())
        }

        receipts.setOnClickListener {
            replaceFragment(ReceiptGeneratorFragment())
        }

        viewReceipts.setOnClickListener {
             replaceFragment(PastReceiptsFragment())
         }

        return view
    }

    // Helper function to replace fragment
    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
