package com.opsc7311poe.xbcad_antoniemotors

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class EmpSelectBusinessAdminActivity : AppCompatActivity() {

    private lateinit var spAllBusinessNames: Spinner
    private lateinit var spAllManagerNames: Spinner
    private lateinit var btnEmpBusChoice: Button
    private lateinit var progressDialog: ProgressDialog

    private val businessNames = mutableListOf<String>()
    private val businessIds = mutableListOf<String>() // To store business IDs
    private val managerNames = mutableListOf<String>()
    private val managerIds = mutableListOf<String>() // To hold manager (admin) IDs

    private var selectedBusiness: String? = null
    private var selectedBusinessId: String? = null
    private var selectedAdminId: String? = null // Selected admin ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emp_select_business_admin)

        spAllBusinessNames = findViewById(R.id.spAllBusinessNames)
        spAllManagerNames = findViewById(R.id.spAllManagerNames)
        btnEmpBusChoice = findViewById(R.id.btnEmpBusChoice)
        progressDialog = ProgressDialog(this).apply {
            setMessage("Loading...")
        }

        loadBusinessNames()

        spAllBusinessNames.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedBusiness = businessNames[position]
                selectedBusinessId = businessIds[position]
                loadManagerNames(selectedBusinessId!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedBusiness = null
                selectedBusinessId = null
            }
        }

        btnEmpBusChoice.setOnClickListener {
            if (selectedAdminId != null) {
                // Proceed to the next activity with business name, ID, and admin ID
                val intent = Intent(this, EmpEnterInfo::class.java).apply {
                    putExtra("businessName", selectedBusiness)
                    putExtra("businessId", selectedBusinessId)
                    putExtra("adminId", selectedAdminId)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please select an admin.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadBusinessNames() {
        progressDialog.show()
        val dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                businessNames.clear()
                businessIds.clear()

                for (userSnapshot in snapshot.children) {
                    val businessName = userSnapshot.child("BusinessInfo").child("businessName").getValue(String::class.java)
                    val businessId = userSnapshot.key // Retrieve business ID from snapshot key

                    if (businessName != null && businessId != null) {
                        businessNames.add(businessName)
                        businessIds.add(businessId)
                    }
                }

                val businessAdapter = ArrayAdapter(
                    this@EmpSelectBusinessAdminActivity,
                    android.R.layout.simple_spinner_item, businessNames
                )
                businessAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spAllBusinessNames.adapter = businessAdapter
                progressDialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
                Toast.makeText(this@EmpSelectBusinessAdminActivity, "Failed to load business names.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadManagerNames(businessId: String) {
        managerNames.clear()
        managerIds.clear() // Clear previous IDs

        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(businessId).child("Employees")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (empSnapshot in snapshot.children) {
                    val firstName = empSnapshot.child("firstName").getValue(String::class.java)
                    val lastName = empSnapshot.child("lastName").getValue(String::class.java)
                    val role = empSnapshot.child("role").getValue(String::class.java) // Get the role

                    if (firstName != null && lastName != null && role == "admin") { // Check if role is "admin"
                        managerNames.add("$firstName $lastName")
                        managerIds.add(empSnapshot.key!!) // Add admin ID to the list
                    }
                }

                val managerAdapter = ArrayAdapter(
                    this@EmpSelectBusinessAdminActivity,
                    android.R.layout.simple_spinner_item, managerNames
                )
                managerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spAllManagerNames.adapter = managerAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EmpSelectBusinessAdminActivity, "Failed to load manager names.", Toast.LENGTH_SHORT).show()
            }
        })

        spAllManagerNames.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedAdminId = managerIds[position] // Get corresponding admin ID
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedAdminId = null
            }
        }
    }
}
