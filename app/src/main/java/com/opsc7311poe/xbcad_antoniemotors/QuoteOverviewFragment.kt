package com.opsc7311poe.xbcad_antoniemotors

import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class QuoteOverviewFragment : Fragment() {

    private lateinit var ownerName: TextView
    private lateinit var vehicleMake: TextView
    private lateinit var address: TextView
    private lateinit var registration: TextView
    private lateinit var partsLayout: LinearLayout
    private lateinit var laborLayout: LinearLayout
    private lateinit var scrollView: ScrollView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quote_overview, container, false)

        // Initialize the views
        ownerName = view.findViewById(R.id.tvOwnerName)
        vehicleMake = view.findViewById(R.id.tvVehicleMake)
        address = view.findViewById(R.id.tvAddress)
        registration = view.findViewById(R.id.tvRegistration)
        partsLayout = view.findViewById(R.id.addparts)
        laborLayout = view.findViewById(R.id.llHoursLabour)
        scrollView = view.findViewById(R.id.svQuoteOverview)  // Ensure your ScrollView has this ID

        // Load data from Firebase
        loadDataFromFirebase()

        // Handle Make PDF Button click
        view.findViewById<View>(R.id.btnMakePDF).setOnClickListener {
            createPDF()
        }

        return view
    }

    private fun loadDataFromFirebase() {
        val database = FirebaseDatabase.getInstance().getReference("quotes")

        // Load Owner and Vehicle Details
        database.child("quoteId").get().addOnSuccessListener { dataSnapshot ->
            ownerName.text = dataSnapshot.child("ownerName").value.toString()
            vehicleMake.text = dataSnapshot.child("vehicleMake").value.toString()
            address.text = dataSnapshot.child("address").value.toString()
            registration.text = dataSnapshot.child("registration").value.toString()

            // Load Parts
            val partsList = dataSnapshot.child("parts").children
            for (part in partsList) {
                val partView = TextView(context)
                partView.text = part.value.toString()
                partsLayout.addView(partView)
            }

            // Load Labor
            val laborList = dataSnapshot.child("labor").children
            for (labor in laborList) {
                val laborView = TextView(context)
                laborView.text = labor.value.toString()
                laborLayout.addView(laborView)
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to load data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createPDF() {
        // Create a new PdfDocument
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(scrollView.width, scrollView.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        // Capture the ScrollView content into the PDF
        scrollView.draw(page.canvas)
        pdfDocument.finishPage(page)

        // Save the document to external storage
        val directory = File(Environment.getExternalStorageDirectory(), "QuotesPDFs")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, "quote_${System.currentTimeMillis()}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "PDF saved to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}
