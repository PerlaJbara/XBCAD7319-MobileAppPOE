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
import java.text.SimpleDateFormat
import java.util.Locale

class PastReceiptsFragment : Fragment() {

    private lateinit var searchInput: EditText
    private lateinit var linLay: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_past_receipts, container, false)

        searchInput = view.findViewById(R.id.txtSearch)

        //loading receipts in database
        linLay = view.findViewById(R.id.linlayServiceCards)
        loadReceipts()

        return view
    }

    private fun loadReceipts() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = Firebase.database
            val receiptsRef = database.getReference(userId).child("Receipts")

            receiptsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    linLay.removeAllViews()

                    for (receiptSnapshot in snapshot.children) {
                        val receipt = receiptSnapshot.getValue(ReceiptData::class.java)

                        if (receipt != null) {
                            val cardView = LayoutInflater.from(requireContext()).inflate(R.layout.documentationlayout , linLay, false) as CardView

                            // Populate the card with receipt data
                            cardView.findViewById<TextView>(R.id.txtCustName).text = receipt.customerName ?: "Unknown"
                            cardView.findViewById<TextView>(R.id.txtServiceType).text = receipt.serviceTypeName ?: "Unknown"
                            cardView.findViewById<TextView>(R.id.txtPrice).text = "R ${receipt.finalQuote ?: "0"}"

                            // Set OnClickListener for card view
                            cardView.setOnClickListener {
                                val receiptDetailFragment = ReceiptGeneratorFragment()
                                val bundle = Bundle()
                                bundle.putString("receiptID", receiptSnapshot.key)
                                receiptDetailFragment.arguments = bundle
                                replaceFragment(receiptDetailFragment)
                            }

                            // Add the card to the container
                            linLay.addView(cardView)
                        } else {
                            Log.e("PastReceiptsFragment", "Receipt data is null for snapshot: ${receiptSnapshot.key}")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("PastReceiptsFragment", "Failed to load receipts.", error.toException())
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