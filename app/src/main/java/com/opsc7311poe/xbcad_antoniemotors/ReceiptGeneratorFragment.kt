package com.opsc7311poe.xbcad_antoniemotors

import QuoteData
import android.content.Context
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import java.util.*

class ReceiptGeneratorFragment : Fragment() {

    private lateinit var spinCustomer: Spinner
    private lateinit var edtLabour: EditText
    private lateinit var txtRvalue: TextView
    private lateinit var btnFinalReview: Button
    private lateinit var btnBack: ImageView
    private lateinit var btnAddNewPart: ImageView
    private lateinit var txtPartsList: TextView
    private lateinit var btnAddPart: Button
    private lateinit var SpinPartName: Spinner  // Changed from EditText
    private lateinit var edtPartCost: EditText
    private lateinit var npStockCounter: NumberPicker  // New NumberPicker

    private lateinit var partsList: MutableList<Map<String, String>>  // List to store parts and their costs
    private var totalPartsCost: Double = 0.0
    private var totalLabourCost: Double = 0.0

    private var selectedCustomerName: String = ""
    private var selectedServiceTypeName: String = ""

    private lateinit var auth: FirebaseAuth
    private lateinit var databases: DatabaseReference
    private lateinit var businessId: String

    // Firebase database reference
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_receipt_generator, container, false)

        businessId = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getString("business_id", null)!!

        // Initialize views
        spinCustomer = view.findViewById(R.id.spinCustomer)
        SpinPartName = view.findViewById(R.id.spinPartName)
        edtPartCost = view.findViewById(R.id.edtPartCost)
        txtPartsList = view.findViewById(R.id.txtPartsList)
        btnAddPart = view.findViewById(R.id.btnAddPart)
        edtLabour = view.findViewById(R.id.edttxtLabour)
        txtRvalue = view.findViewById(R.id.txtRvalue)
        btnBack = view.findViewById(R.id.ivBackButton)
        btnFinalReview = view.findViewById(R.id.btnFinalReview)
        npStockCounter = view.findViewById(R.id.npStockCounter)
        btnAddNewPart = view.findViewById(R.id.btnPlus)

        partsList = mutableListOf()

        npStockCounter.minValue = 1
        npStockCounter.maxValue = 1000  // Adjust based on max stock value in Firebase
        npStockCounter.wrapSelectorWheel = true

        SpinPartName.setSelection(0)
        edtPartCost.text.clear()
        npStockCounter.value = 1
        txtPartsList.text = ""
        edtLabour.text.clear()
        txtRvalue.text = "R0.00"
        spinCustomer.setSelection(0)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            populateCustomerSpinner()
            populatePartSpinner() // Populate SpinPartName with parts from Firebase
        }

        // Handle back button press
        btnBack.setOnClickListener {
            replaceFragment(DocumentationFragment())
        }

        //directs the user to the add parts/ inventory page
        btnAddNewPart.setOnClickListener {
            replaceFragment(AddPartFragment())
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
            val receiptId = generateReceipt()
            val bundle = Bundle()
            bundle.putString("receiptID", receiptId)

            val ReceiptOverviewFragment = ReceiptOverviewFragment()
            ReceiptOverviewFragment.arguments = bundle

            replaceFragment(ReceiptOverviewFragment)  // Navigate to the receipt overview fragment
        }

        return view
    }

    private fun populateCustomerSpinner() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val customerRef = FirebaseDatabase.getInstance().getReference("Users").child(businessId).child("Customers")

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
                    val firstName = customerSnapshot.child("CustomerName").getValue(String::class.java)
                    val lastName = customerSnapshot.child("CustomerSurname").getValue(String::class.java)

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

    private fun populatePartSpinner() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val partNames = mutableListOf<String>()
        val partsReference = database.reference.child("Users").child(userId!!).child("parts")

        partsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (partSnapshot in snapshot.children) {
                    val partName = partSnapshot.child("partName").getValue(String::class.java)
                    partName?.let { partNames.add(it) }
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, partNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                SpinPartName.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load parts: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Method to clear fields after adding a part
    private fun clearFields() {
        SpinPartName.setSelection(0) // Reset the spinner to the first item
        edtPartCost.text.clear()     // Clear the cost input
        npStockCounter.value = 1     // Reset the quantity picker to its minimum value (e.g., 1)
    }

    private fun addPart() {
        val selectedPartName = SpinPartName.selectedItem?.toString()
        val partCost = edtPartCost.text.toString().trim()
        val selectedQuantity = npStockCounter.value

        if (selectedPartName != null && partCost.isNotEmpty()) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val partsReference = database.reference.child("Users").child(userId!!).child("parts")

            partsReference.orderByChild("partName").equalTo(selectedPartName)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val partSnapshot = snapshot.children.first()
                            val currentStock = partSnapshot.child("stockCount").getValue(Int::class.java) ?: 0

                            if (selectedQuantity <= currentStock) {
                                val newStockCount = currentStock - selectedQuantity
                                val partId = partSnapshot.key

                                partsReference.child(partId!!).child("stockCount").setValue(newStockCount)
                                    .addOnSuccessListener {
                                        val partData = mapOf(
                                            "name" to selectedPartName,
                                            "cost" to partCost,
                                            "quantity" to selectedQuantity.toString()
                                        )
                                        partsList.add(partData)

                                        totalPartsCost += partCost.toDouble() * selectedQuantity
                                        txtPartsList.text = formatPartsList()
                                        updateTotalQuote()

                                        clearFields()  // Reset fields after adding the part
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(requireContext(), "Failed to update stock", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(requireContext(), "Not enough stock available", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), "Part not found", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), "Error adding part: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            Toast.makeText(requireContext(), "Please provide part details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatPartsList(): String {
        var partsListText = ""
        for (part in partsList) {
            val name = part["name"]
            val cost = part["cost"]
            val quantity = part["quantity"]
            partsListText += "$name x$quantity - R$cost\n"
        }
        return partsListText
    }

    private fun updateTotalQuote() {
        val totalQuote = totalLabourCost + totalPartsCost
        txtRvalue.text = "R$totalQuote"
    }

    private fun generateReceipt(): String {
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        if (userID == null) {
            Toast.makeText(requireContext(), "User not authenticated. Cannot save quote.", Toast.LENGTH_SHORT).show()
            return ""
        }

        val receiptId = UUID.randomUUID().toString()
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val quoteData = QuoteData(
            id = receiptId,
            customerName = selectedCustomerName,
            parts = partsList,
            labourCost = totalLabourCost.toString(),
            totalCost = (totalPartsCost + totalLabourCost).toString(),
            dateCreated = currentDate
        )

        // Reference the path as "Users/<userID>/Quotes/<quoteId>"
        val quoteRef = database.getReference("Users").child(userID).child("Receipts").child(receiptId)
        quoteRef.setValue(quoteData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Invoice saved!", Toast.LENGTH_SHORT).show()

                val bundle = Bundle().apply {
                    putString("receiptId", receiptId) // Pass the quoteId to the next fragment
                }
                val receiptOverviewFragment = ReceiptOverviewFragment().apply {
                    arguments = bundle
                }
                replaceFragment(receiptOverviewFragment)
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Failed to save quote data: ${error.message}", Toast.LENGTH_SHORT).show()
            }

        return receiptId // Returning the quoteId to pass it to the next fragment
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
