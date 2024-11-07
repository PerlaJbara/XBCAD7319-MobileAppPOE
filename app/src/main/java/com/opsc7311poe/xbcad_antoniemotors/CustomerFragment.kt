package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class CustomerFragment : Fragment() {

    private lateinit var vectorPlusButton: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var customerAdapter: CustomerAdapter
    //private val customerList = mutableListOf<CustomerData>()
    private lateinit var customerList: ArrayList<CustomerData>
    private lateinit var searchCustomers: SearchView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_customer, container, false)

        vectorPlusButton = view.findViewById(R.id.btnPlus)
        btnBack = view.findViewById(R.id.ivBackButton)
        searchCustomers = view.findViewById(R.id.customerSearch)
        recyclerView = view.findViewById(R.id.recyclerViewCustomers)


        // Set click listener for the "Add Customer" button
        //this button will be linked to the customer registration page for the web application
       /* vectorPlusButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(AddCustomerFragment())
        }*/

        // Set click listener for the "Back" button
        btnBack.setOnClickListener {
            replaceFragment(HomeFragment())
        }

        customerList = ArrayList()
        //customerAdapter = CustomerAdapter(customerList)
        customerAdapter = CustomerAdapter(customerList) { selectedCustomer ->
            openCustomerDetailsFragment(selectedCustomer)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = customerAdapter



        searchCustomers.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                customerAdapter.filter.filter(newText)
                return false
            }
        })

        // Fetch customer data
        fetchCustomers()

        return view
    }


    private fun fetchCustomers() {
        val adminId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("fetchCustomers", "userId found for admin: $adminId")

        if (adminId != null) {
            // Reference to the "Users" node in Firebase
            val usersReference = FirebaseDatabase.getInstance().getReference("Users")

            usersReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(usersSnapshot: DataSnapshot) {
                    var businessID: String? = null

                    // Iterate over each business node to find where adminId exists under Employees
                    for (businessSnapshot in usersSnapshot.children) {
                        val employeeSnapshot = businessSnapshot.child("Employees").child(adminId)

                        if (employeeSnapshot.exists()) {
                            // Found the employee record; extract the associated BusinessID
                            businessID = employeeSnapshot.child("businessID").getValue(String::class.java)
                            Log.d("fetchCustomers", "BusinessID found for admin: $businessID")
                            break
                        }
                    }

                    if (businessID != null) {
                        // Now use BusinessID to retrieve customers under the correct business
                        val customerReference = usersReference.child(businessID).child("Customers")

                        customerReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                customerList.clear()

                                if (!snapshot.exists()) {
                                    Toast.makeText(requireContext(), "No saved customers found.", Toast.LENGTH_SHORT).show()
                                    return
                                }

                                // Retrieve each customer and add to the list
                                for (customerSnapshot in snapshot.children) {
                                    val customer = customerSnapshot.getValue(CustomerData::class.java)
                                    customer?.let {
                                        customerList.add(it)
                                    }
                                }

                                // Notify the adapter that data has changed
                                customerAdapter.notifyDataSetChanged()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("fetchCustomers", "Error fetching customers: ${error.message}")
                                Toast.makeText(requireContext(), "Error fetching customers.", Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        Log.e("fetchCustomers", "BusinessID not found for the current admin.")
                        Toast.makeText(requireContext(), "Unable to find associated business.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("fetchCustomers", "Error fetching business information: ${error.message}")
                    Toast.makeText(requireContext(), "Error fetching business information.", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Log.e("fetchCustomers", "User is not logged in or adminId is null.")
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
        }
    }





    private fun openCustomerDetailsFragment(customer: CustomerData) {
        val fragment = CustomerDetailsFragment().apply {
            arguments = Bundle().apply {
                putString("customerID", customer.CustomerID)
                putString("customerName", customer.CustomerName)
                putString("customerSurname", customer.CustomerSurname)
                putString("customerEmail", customer.CustomerEmail)
                putString("customerAddress", customer.CustomerAddress)
                putString("customerMobile", customer.CustomerMobileNum)
                putString("customerType", customer.CustomerType)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }




    // Replaces the current fragment with the specified fragment
    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
