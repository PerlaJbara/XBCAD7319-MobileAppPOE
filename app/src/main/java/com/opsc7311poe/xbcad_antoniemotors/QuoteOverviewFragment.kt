package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.*

class QuoteOverviewFragment : Fragment() {

    private lateinit var customerNameTextView: TextView
    private lateinit var customerPhoneTextView: TextView
    private lateinit var vehicleDetailsTextView: TextView
    private lateinit var txtVinRec: TextView
    private lateinit var kilometersTextView: TextView
    private lateinit var issueDateTextView: TextView
    private lateinit var layoutParts: LinearLayout
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_receipt_overview, container, false)

        // Initialize views
        customerNameTextView = view.findViewById(R.id.txtNameRec)
        customerPhoneTextView = view.findViewById(R.id.txtCellRec)
        vehicleDetailsTextView = view.findViewById(R.id.txtVehicleRec)
        txtVinRec = view.findViewById(R.id.txtVinRec)
        kilometersTextView = view.findViewById(R.id.txtKiloRec)
        issueDateTextView = view.findViewById(R.id.txtDateRec)
        layoutParts = view.findViewById(R.id.layoutParts)
        btnBack = view.findViewById(R.id.ivBackButton)

        btnBack.setOnClickListener {
            replaceFragment(ReceiptGeneratorFragment())
        }

        // Fetch data from Firebase
        fetchDataFromFirebase()

        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun fetchDataFromFirebase() {
        val receiptId = arguments?.getString("receiptId") ?: return

        // Reference to the Firebase Database
        val databaseRef = FirebaseDatabase.getInstance().getReference("Receipts").child(receiptId)

        // Fetch customer details
        databaseRef.child("customerDetails").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                customerNameTextView.text = snapshot.child("name").value.toString()
                customerPhoneTextView.text = snapshot.child("phone").value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error fetching customer details: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Fetch vehicle details
        databaseRef.child("vehicleDetails").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                vehicleDetailsTextView.text = snapshot.child("model").value.toString()
                txtVinRec.text = snapshot.child("vin").value.toString()
                kilometersTextView.text = snapshot.child("kilometers").value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error fetching vehicle details: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Fetch parts details (dynamic number of parts)
        databaseRef.child("parts").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                layoutParts.removeAllViews() // Clear previous views if any

                for (partSnapshot in snapshot.children) {
                    val partName = partSnapshot.child("name").value.toString()
                    val partCost = partSnapshot.child("cost").value.toString()

                    // Create a TextView dynamically for each part
                    val partTextView = TextView(context)
                    partTextView.text = "Part: $partName, Cost: $partCost"
                    partTextView.textSize = 16f
                    partTextView.setPadding(16, 16, 16, 16)

                    // Add TextView to the LinearLayout
                    layoutParts.addView(partTextView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error fetching parts: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        /*// Set the current date for the issued quote
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        issueDateTextView.text = currentDate*/
    }
}
