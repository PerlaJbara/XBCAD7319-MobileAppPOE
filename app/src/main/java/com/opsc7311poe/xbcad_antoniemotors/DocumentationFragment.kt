package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

class DocumentationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_documentation, container, false)

        // Find the buttons in the layout
        val btnGenQuote = view.findViewById<Button>(R.id.btnGenQuote)
        val viewCustomersBtn = view.findViewById<Button>(R.id.btnPastQuotes)
        val btnRec = view.findViewById<Button>(R.id.btnGenReciept)
        val btnReview = view.findViewById<Button>(R.id.btnReviewReceipt)

        // Set click listeners and replace fragments
        btnGenQuote.setOnClickListener {
            replaceFragment(QuoteGenFragment())
        }

        viewCustomersBtn.setOnClickListener {
            replaceFragment(PastQuotesFragment())
        }

        btnRec.setOnClickListener {
            replaceFragment(ReceiptGeneratorFragment())
        }

        btnReview.setOnClickListener {
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
