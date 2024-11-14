package com.opsc7311poe.xbcad_antoniemotors

import QuoteData
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class PastQuotesFragment : Fragment() {

    private lateinit var searchInput: EditText
    private lateinit var linLay: LinearLayout
    private val quotesList = mutableListOf<QuoteData>()
    private lateinit var btnBack: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_past_quotes, container, false)

        searchInput = view.findViewById(R.id.txtSearch)
        linLay = view.findViewById(R.id.linlayServiceCards)
        btnBack = view.findViewById(R.id.ivBackButton)

        // Load all quotes initially
        loadReceipts()

        btnBack.setOnClickListener {
            replaceFragment(DocumentationFragment())
        }

        // Add a listener to filter quotes as the user types
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuotes(s.toString())
            }
        })

        return view
    }

    private fun loadReceipts() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = Firebase.database
            val receiptsRef = database.getReference("Users").child(userId).child("Quotes")

            receiptsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    linLay.removeAllViews()
                    quotesList.clear() // Clear the list before adding new data

                    for (receiptSnapshot in snapshot.children) {
                        val quote = receiptSnapshot.getValue(QuoteData::class.java)
                        if (quote != null) {
                            quotesList.add(quote) // Add each quote to the list
                        }
                    }

                    // Display all quotes by default
                    displayQuotes(quotesList)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Error handling (removed log statements)
                }
            })
        }
    }

    private fun searchQuotes(query: String) {
        val filteredQuotes = quotesList.filter {
            it.customerName?.contains(query, ignoreCase = true) == true
        }
        displayQuotes(filteredQuotes)
    }

    private fun displayQuotes(quotes: List<QuoteData>) {
        linLay.removeAllViews() // Clear previous views

        for (quote in quotes) {
            val cardView = LayoutInflater.from(requireContext()).inflate(R.layout.documentationlayout, linLay, false) as CardView

            cardView.findViewById<TextView>(R.id.txtCustName).text = quote.customerName ?: "Unknown"
            cardView.findViewById<TextView>(R.id.txtPrice).text = "R ${quote.totalCost ?: "0"}"
            cardView.findViewById<TextView>(R.id.txtDate).text = "${quote.dateCreated ?: "0"}"

            // Set OnClickListener for card view to navigate to quote details
            cardView.setOnClickListener {
                val quoteOverviewFragment = QuoteOverviewFragment()
                val bundle = Bundle()
                // Pass the quoteID to QuoteOverviewFragment
                bundle.putString("quoteId", quote.id) // Assuming `id` exists in QuoteData
                quoteOverviewFragment.arguments = bundle

                // Navigate to the QuoteOverviewFragment
                replaceFragment(quoteOverviewFragment)
            }

            // Add the card to the container
            linLay.addView(cardView)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
