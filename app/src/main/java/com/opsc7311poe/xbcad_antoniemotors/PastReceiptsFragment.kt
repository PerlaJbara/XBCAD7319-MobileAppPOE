package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class PastReceiptsFragment : Fragment() {

    private lateinit var searchInput: EditText
    private lateinit var linLay: LinearLayout
    private lateinit var btnBack: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_past_receipts, container, false)

        searchInput = view.findViewById(R.id.txtSearch)
        btnBack = view.findViewById(R.id.ivBackButton)

        // Initialize the LinearLayout to display the cards
        linLay = view.findViewById(R.id.linlayServiceCards)
        loadReceipts()

        btnBack.setOnClickListener {
            replaceFragment(DocumentationFragment())
        }

        return view
    }

    private fun loadReceipts() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = Firebase.database
            val receiptsRef = database.getReference("Users").child(userId).child("Receipts")

            receiptsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    linLay.removeAllViews()

                    for (receiptSnapshot in snapshot.children) {
                        val receipt = receiptSnapshot.getValue(ReceiptData::class.java)

                        if (receipt != null) {
                            val cardView = LayoutInflater.from(requireContext()).inflate(R.layout.documentationlayout, linLay, false) as CardView

                            // Populate the card with quote data
                            cardView.findViewById<TextView>(R.id.txtCustName).text = receipt.customerName ?: "Unknown"
                            cardView.findViewById<TextView>(R.id.txtPrice).text = "R ${receipt.totalCost ?: "0"}"

                            // Set OnClickListener for card view to navigate to quote details
                            cardView.setOnClickListener {
                                val receiptOverviewFragment = ReceiptOverviewFragment()
                                val bundle = Bundle()
                                // Pass the quoteID to QuoteOverviewFragment
                                bundle.putString("receiptId", receiptSnapshot.key)
                                receiptOverviewFragment.arguments = bundle

                                // Navigate to the QuoteOverviewFragment
                                replaceFragment(receiptOverviewFragment)
                            }

                            // Add the card to the container
                            linLay.addView(cardView)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
