package com.opsc7311poe.xbcad_antoniemotors

import QuoteData
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import android.Manifest
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.widget.ImageView
import androidx.core.app.NotificationCompat

class ReceiptOverviewFragment : Fragment() {

    private lateinit var scrollView: ScrollView
    private lateinit var txtDate: TextView
    private lateinit var tvOwnerName: TextView
    private lateinit var addParts: LinearLayout
    private lateinit var llHoursLabour: LinearLayout
    private lateinit var tvTotal: TextView
    private lateinit var receiptId: String
    private lateinit var btnBack: ImageView

    companion object {
        private const val REQUEST_CODE_WRITE_STORAGE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the quote overview fragment layout
        val view = inflater.inflate(R.layout.fragment_receipt_overview, container, false)

        // Initialize the ScrollView and invoice layout
        scrollView = view.findViewById(R.id.svQuoteOverview)

        // Inflate the invoice_layout.xml into a new LinearLayout
        val invoiceView = inflater.inflate(R.layout.invoice_layout, null)

        // Initialize the views from invoice_layout
        txtDate = invoiceView.findViewById(R.id.txtDate)
        tvOwnerName = invoiceView.findViewById(R.id.tvOwnerName)
        addParts = invoiceView.findViewById(R.id.addparts)
        llHoursLabour = invoiceView.findViewById(R.id.llHoursLabour)
        tvTotal = invoiceView.findViewById(R.id.tvTotal)
        btnBack = view.findViewById(R.id.ivBackButton)

        // Find the LinearLayout inside the ScrollView and add the inflated invoice view
        val linearLayout = scrollView.getChildAt(0) as LinearLayout
        linearLayout.addView(invoiceView)

        // Retrieve the quoteId passed from the previous fragment
        receiptId = arguments?.getString("receiptId") ?: ""
        if (receiptId.isEmpty()) {
            Toast.makeText(context, "Quote ID provided", Toast.LENGTH_SHORT).show()
            return view
        }
        btnBack.setOnClickListener {
            replaceFragment(DocumentationFragment())
        }

        // Load data from Firebase
        loadDataFromFirebase()

        // Handle Delete Button click
        view.findViewById<View>(R.id.btnQuoteOv).setOnClickListener {
            deleteQuote()
        }
        view.findViewById<View>(R.id.btnMakePDF).setOnClickListener {
            createPDF()
        }

        // Request permission to write to external storage if not granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_STORAGE)
        }

        return view
    }

    private fun loadDataFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null && receiptId.isNotEmpty()) {
            val database = FirebaseDatabase.getInstance()
            val quoteRef =
                database.getReference("Users").child(userId).child("Receipts").child(receiptId)

            quoteRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val receipt = snapshot.getValue(ReceiptData::class.java)
                    if (receipt != null) {

                        // Set the data into the views
                        txtDate.text = receipt.dateCreated ?: "N/A"
                        tvOwnerName.text = receipt.customerName ?: "Unknown"
                        tvTotal.text = "R ${receipt.totalCost ?: "0"}"

                        // Display Labor Cost in llHoursLabour LinearLayout
                        llHoursLabour.removeAllViews() // Clear any existing views
                        val labourCostText = TextView(requireContext()).apply {
                            text = "Labor Cost: R ${receipt.labourCost ?: "0"}"
                            textSize = 16f
                            setPadding(16, 16, 16, 16)
                        }
                        llHoursLabour.addView(labourCostText)

                        // Display Parts in addParts LinearLayout
                        addParts.removeAllViews() // Clear existing views
                        if (receipt.parts.isNotEmpty()) {
                            for (part in receipt.parts) {
                                val partName = part["name"] as? String ?: "Unknown"
                                val partCost = part["cost"] as? String ?: "0"
                                val partQuantity = part["quantity"] as? String ?: "0"

                                val partView = LinearLayout(requireContext()).apply {
                                    orientation = LinearLayout.HORIZONTAL
                                    val nameText = TextView(context).apply {
                                        text = "$partName"
                                        layoutParams = LinearLayout.LayoutParams(
                                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                                        )
                                    }

                                    val quantityText = TextView(context).apply {
                                        text = "Qty: $partQuantity"
                                        layoutParams = LinearLayout.LayoutParams(
                                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                                        )
                                    }

                                    val priceText = TextView(context).apply {
                                        text = "R $partCost"
                                        layoutParams = LinearLayout.LayoutParams(
                                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                                        )
                                    }

                                    addView(nameText)
                                    addView(quantityText)
                                    addView(priceText)
                                }

                                addParts.addView(partView)
                            }
                        } else {
                            val noPartsText = TextView(requireContext()).apply {
                                text = "No parts added."
                                textSize = 16f
                                setPadding(16, 16, 16, 16)
                            }
                            addParts.addView(noPartsText)
                        }
                    } else {
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    private fun deleteQuote() {
        // Show confirmation dialog before deleting
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Receipt")
        builder.setMessage("Are you sure you want to delete this Receipt?")
        builder.setPositiveButton("Yes") { _, _ ->
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null && receiptId.isNotEmpty()) {
                val database = FirebaseDatabase.getInstance()
                val quoteRef =
                    database.getReference("Users").child(userId).child("Receipts").child(receiptId)

                // Delete the quote from Firebase
                quoteRef.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Receipt deleted successfully", Toast.LENGTH_SHORT)
                            .show()

                        // Navigate to DocumentationFragment using replaceFragment
                        val documentationFragment =
                            DocumentationFragment() // Create the new fragment instance
                        replaceFragment(documentationFragment) // Replace the container with DocumentationFragment
                    } else {
                        Toast.makeText(context, "Failed to delete Receipt", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun showSaveNotification(isSaving: Boolean) {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "pdf_save_channel"

        // Create the notification channel (required for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "PDF Save Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for PDF saving process"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create the notification
        val notification: Notification = if (isSaving) {
            NotificationCompat.Builder(requireContext(), channelId)
                .setContentTitle("Saving PDF")
                .setContentText("Your PDF is being saved...")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(0, 0, true) // Show indeterminate progress
                .build()
        } else {
            NotificationCompat.Builder(requireContext(), channelId)
                .setContentTitle("PDF Saved")
                .setContentText("Your PDF has been successfully saved.")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setAutoCancel(true)
                .build()
        }

        // Show the notification
        notificationManager.notify(0, notification)
    }

    private fun createPDF() {
        // Get the invoice layout as a bitmap
        val bitmap = getBitmapFromView(scrollView)

        // Create the PDF document
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        pdfDocument.finishPage(page)

        // Save the PDF to external storage
        val pdfFile = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "quote_$receiptId.pdf")

        try {
            pdfDocument.writeTo(FileOutputStream(pdfFile))
            Toast.makeText(context, "PDF saved", Toast.LENGTH_SHORT).show()
            showSaveNotification(false) // Show the success notification

            // Share the PDF file
            sharePDF(pdfFile)

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving PDF", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        // Create a bitmap from the view
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun sharePDF(pdfFile: File) {
        // Get the URI of the PDF file
        val pdfUri: Uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            pdfFile
        )

        // Create a share intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_SUBJECT, "Here is your receipt PDF")
            putExtra(Intent.EXTRA_TEXT, "Please find attached your receipt.")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        // Show the share options
        startActivity(Intent.createChooser(shareIntent, "Share PDF via"))
    }



    private fun replaceFragment(fragment: Fragment) {
        val transaction = requireFragmentManager().beginTransaction()
        transaction.replace(R.id.frame_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
