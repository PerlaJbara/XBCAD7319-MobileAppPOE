package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ReceiptGeneratorFragment : Fragment() {

    private lateinit var spinCustomer: Spinner
    private lateinit var spinServiceType: Spinner
    private lateinit var edtParts: EditText
    private lateinit var edtLabour: EditText
    private lateinit var txtRvalue: TextView
    private lateinit var btnFinalReview: Button
    private lateinit var btnBack: ImageView
    private lateinit var edtPartName: EditText
    private lateinit var edtPartCost: EditText
    private lateinit var txtPartsList: TextView
    private lateinit var btnAddPart: Button

    private lateinit var partsList: MutableList<Map<String, String>>  // List to store parts and their costs
    private var totalPartsCost: Double = 0.0
    private var totalLabourCost: Double = 0.0

    private var selectedCustomerName: String = ""
    private var selectedServiceTypeName: String = ""

    // Firebase database reference
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_receipt_generator, container, false)

        // Initialize views
        spinCustomer = view.findViewById(R.id.spinCustomer)
        spinServiceType = view.findViewById(R.id.spinQuote)
        edtPartName = view.findViewById(R.id.edtPartName)
        edtPartCost = view.findViewById(R.id.edtPartCost)
        txtPartsList = view.findViewById(R.id.txtPartsList)
        btnAddPart = view.findViewById(R.id.btnAddPart)
        edtLabour = view.findViewById(R.id.edttxtLabour)
        txtRvalue = view.findViewById(R.id.txtRvalue)
        btnBack = view.findViewById(R.id.ivBackButton)
        btnFinalReview = view.findViewById(R.id.btnFinalReview)

        partsList = mutableListOf()

        // Populate Spinner with customer names
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { populateCustomerSpinner() }

        val userIds = FirebaseAuth.getInstance().currentUser?.uid
        userIds?.let { populateServiceTypeSpinner() }

        // Handle back button press
        btnBack.setOnClickListener {
            replaceFragment(HomeFragment())
        }

        // Set up add part button
        btnAddPart.setOnClickListener {
            addPart()
        }

        // Add TextWatcher to update total when labor changes
        edtLabour.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                totalLabourCost = s.toString().toDoubleOrNull() ?: 0.0
                updateTotalQuote()
            }
        })

        // Set up final review button
        btnFinalReview.setOnClickListener {
            generateReceipt()  // Generate and save receipt to Firebase
            replaceFragment(ReceiptOverviewFragment())  // Navigate to the receipt overview fragment
        }

        return view
    }

    private fun populateCustomerSpinner() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val customerRef = FirebaseDatabase.getInstance().getReference(userId).child("Customers")
        customerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val customerMap = mutableMapOf<String, String>()
                val customerNames = mutableListOf<String>()

                if (!snapshot.exists()) {
                    Toast.makeText(requireContext(), "No customers found for this user", Toast.LENGTH_SHORT).show()
                    return
                }

                for (customerSnapshot in snapshot.children) {
                    val customerId = customerSnapshot.key
                    val firstName = customerSnapshot.child("customerName").getValue(String::class.java)
                    val lastName = customerSnapshot.child("customerSurname").getValue(String::class.java)

                    if (!firstName.isNullOrEmpty() && !lastName.isNullOrEmpty() && customerId != null) {
                        val fullName = "$firstName $lastName"
                        customerMap[customerId] = fullName
                        customerNames.add(fullName)
                    }
                }

                if (customerNames.isEmpty()) {
                    Toast.makeText(requireContext(), "No customers found", Toast.LENGTH_SHORT).show()
                    return
                }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, customerNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinCustomer.adapter = adapter

                spinCustomer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        selectedCustomerName = parent.getItemAtPosition(position).toString()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load customers", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateServiceTypeSpinner() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val serviceTypeRef = FirebaseDatabase.getInstance().getReference(userId).child("ServiceTypes")
        serviceTypeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val serviceTypeMap = mutableMapOf<String, String>()
                val serviceTypeNames = mutableListOf<String>()

                if (!snapshot.exists()) {
                    Toast.makeText(requireContext(), "No service types found.", Toast.LENGTH_SHORT).show()
                    return
                }

                for (serviceTypeSnapshot in snapshot.children) {
                    val serviceTypeId = serviceTypeSnapshot.key
                    val serviceName = serviceTypeSnapshot.child("name").getValue(String::class.java)

                    if (!serviceName.isNullOrEmpty() && serviceTypeId != null) {
                        serviceTypeMap[serviceTypeId] = serviceName
                        serviceTypeNames.add(serviceName)
                    }
                }

                if (serviceTypeNames.isEmpty()) {
                    Toast.makeText(requireContext(), "No service types found", Toast.LENGTH_SHORT).show()
                    return
                }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, serviceTypeNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinServiceType.adapter = adapter

                spinServiceType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        selectedServiceTypeName = parent.getItemAtPosition(position).toString()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load service types", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addPart() {
        val partName = edtPartName.text.toString().trim()
        val partCost = edtPartCost.text.toString().trim()

        if (partName.isNotEmpty() && partCost.isNotEmpty()) {
            val partData = mapOf("name" to partName, "cost" to partCost)
            partsList.add(partData)

            // Update the total parts cost
            totalPartsCost += partCost.toDouble()

            // Update parts list and total quote
            updatePartsList()
            updateTotalQuote()

            edtPartName.text.clear()
            edtPartCost.text.clear()
        } else {
            Toast.makeText(context, "Please enter both part name and cost", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePartsList() {
        val partsText = partsList.joinToString(separator = "\n") { "${it["name"]}: R ${it["cost"]}" }
        txtPartsList.text = partsText
    }

    private fun updateTotalQuote() {
        // Calculate the total quote by adding parts and labor
        val totalQuote = totalPartsCost + totalLabourCost
        txtRvalue.text = "Total: $totalQuote"
    }

    private fun generateReceipt() {
        if (selectedCustomerName.isNotEmpty() && selectedServiceTypeName.isNotEmpty()) {
            // Get current date for receipt timestamp
            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            // Create a receipt object
            val receiptData = mapOf(
                "customerName" to selectedCustomerName,
                "serviceTypeName" to selectedServiceTypeName,
                "parts" to partsList,
                "labourCost" to totalLabourCost,
                "finalQuote" to (totalPartsCost + totalLabourCost).toString(),
                "date" to currentDate
            )

            // Save receipt data to Firebase
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            userId?.let {
                val receiptsRef = database.getReference(it).child("Receipts")
                val newReceiptRef = receiptsRef.push() // Create a new entry
                newReceiptRef.setValue(receiptData)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Receipt generated successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(context, "Failed to generate receipt: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(context, "Please select a customer and a service type", Toast.LENGTH_SHORT).show()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
