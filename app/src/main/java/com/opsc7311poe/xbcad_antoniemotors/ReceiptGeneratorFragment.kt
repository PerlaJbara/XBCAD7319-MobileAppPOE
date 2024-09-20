package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ReceiptGeneratorFragment : Fragment() {

    private lateinit var spinCustomer: Spinner
    private lateinit var spinQuote: Spinner
    private lateinit var edtParts: EditText
    private lateinit var edtLabour: EditText
    private lateinit var txtFinalQuote: TextView
    private lateinit var btnGenerateReceipt: Button
    private lateinit var btnBack: ImageView
    private lateinit var edtPartName: EditText
    private lateinit var edtPartCost: EditText
    private lateinit var txtPartsList: TextView
    private lateinit var btnAddPart: Button

    private lateinit var partsList: MutableList<Map<String, String>> // List to store parts and their costs

    private var selectedCustomerId: String = ""
    private var selectedServiceTypeId: String = ""

    // Firebase database reference
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_receipt_generator, container, false)

        // Initialize views
        spinCustomer = view.findViewById(R.id.spinCustomer)
        spinQuote = view.findViewById(R.id.spinQuote)
        edtPartName = view.findViewById(R.id.edtPartName)
        edtPartCost = view.findViewById(R.id.edtPartCost)
        txtPartsList = view.findViewById(R.id.txtPartsList)
        btnAddPart = view.findViewById(R.id.btnAddPart)
        edtLabour = view.findViewById(R.id.edttxtLabour)
        txtFinalQuote = view.findViewById(R.id.txtFinalQuote)
        btnGenerateReceipt = view.findViewById(R.id.btnServiceTypes)
        btnBack = view.findViewById(R.id.ivBackButton)

        partsList = mutableListOf()

        // Load customer names and service types into spinners
        loadCustomerNames()
        loadServiceTypes()

        // Handle back button press
        btnBack.setOnClickListener {
            replaceFragment(HomeFragment())
        }

        // Set up receipt generation button
        btnGenerateReceipt.setOnClickListener {
            generateReceipt()
        }

        // Set up add part button
        btnAddPart.setOnClickListener {
            addPart()
        }

        return view
    }

    private fun loadCustomerNames() {
        val customerList = mutableListOf<String>()
        val customerIds = mutableListOf<String>()
        val customerRef = database.getReference("Customers")

        customerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (customerSnapshot in snapshot.children) {
                    val customerName = customerSnapshot.child("name").getValue(String::class.java)
                    val customerId = customerSnapshot.key
                    if (customerName != null && customerId != null) {
                        customerList.add(customerName)
                        customerIds.add(customerId)
                    }
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, customerList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinCustomer.adapter = adapter

                // Store customer IDs for reference when generating the receipt
                spinCustomer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        selectedCustomerId = customerIds[position] // Assign selected customer ID here
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load customers", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadServiceTypes() {
        val serviceList = mutableListOf<String>()
        val serviceTypeIds = mutableListOf<String>()
        val serviceRef = database.getReference("Services")

        serviceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (serviceSnapshot in snapshot.children) {
                    val serviceName = serviceSnapshot.child("name").getValue(String::class.java)
                    val serviceTypeId = serviceSnapshot.key
                    if (serviceName != null && serviceTypeId != null) {
                        serviceList.add(serviceName)
                        serviceTypeIds.add(serviceTypeId)
                    }
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, serviceList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinQuote.adapter = adapter

                // Store service IDs for reference when generating the receipt
                spinQuote.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        selectedServiceTypeId = serviceTypeIds[position] // Assign selected service ID here
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load service types", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addPart() {
        val partName = edtPartName.text.toString().trim()
        val partCost = edtPartCost.text.toString().trim()

        if (partName.isNotEmpty() && partCost.isNotEmpty()) {
            // Add part data to partsList
            val partData = mapOf(
                "name" to partName,
                "cost" to partCost
            )
            partsList.add(partData)
            updatePartsList()

            // Clear inputs
            edtPartName.text.clear()
            edtPartCost.text.clear()

            // Ensure selectedCustomerId is available
            if (selectedCustomerId.isNotEmpty()) {
                val currentDate = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date()) // Format current date
                val receiptId = "currentDate"

                // Save parts to Firebase under the generated receipt ID
                database.getReference("Receipts").child(receiptId).child("Parts").setValue(partsList)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Parts saved successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to save parts: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Please select a customer before adding parts.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Please enter both part name and cost", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePartsList() {0
        val partsStringBuilder = StringBuilder()
        for (part in partsList) {
            partsStringBuilder.append("${part["name"]}: R${part["cost"]}\n")
        }
        txtPartsList.text = partsStringBuilder.toString()
    }

    private fun generateReceipt() {
        val partsCost = edtParts.text.toString().trim()
        val labourCost = edtLabour.text.toString().trim()

        // Check if inputs are valid
        if (partsCost.isEmpty() || labourCost.isEmpty()) {
            Toast.makeText(context, "Please enter all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Calculate final quote
        val finalQuote = partsCost.toDouble() + labourCost.toDouble()

        // Display final quote
        txtFinalQuote.text = "Final Quote: R$finalQuote"

        // Save receipt in Firebase
        if (selectedCustomerId.isNotEmpty()) {
            val currentDate = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            //val receiptId = "$selectedCustomerId_$currentDate"
            val receiptId = "currentDate"

            val receiptRef = database.getReference("Receipts").child(receiptId)
            val receipt = mapOf(
                "serviceType" to selectedServiceTypeId,
                "partsCost" to partsCost,
                "labourCost" to labourCost,
                "finalQuote" to finalQuote.toString()
            )

            receiptRef.setValue(receipt).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Receipt generated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to generate receipt", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Please select a customer before generating the receipt.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }
}
