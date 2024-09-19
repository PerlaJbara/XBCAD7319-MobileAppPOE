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
    private lateinit var customerSearch: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var customerAdapter: CustomerAdapter
    private val customerList = mutableListOf<CustomerData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_customer, container, false)

        vectorPlusButton = view.findViewById(R.id.btnPlus)
        btnBack = view.findViewById(R.id.ivBackButton)
        customerSearch = view.findViewById(R.id.customerSearch)
        recyclerView = view.findViewById(R.id.recyclerViewCustomers)

        // Set click listener for the "Add Customer" button
        vectorPlusButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(AddCustomerFragment())
        }

        // Set click listener for the "Back" button
        btnBack.setOnClickListener {
            replaceFragment(HomeFragment())
        }

        recyclerView = view.findViewById(R.id.recyclerViewCustomers)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        customerAdapter = CustomerAdapter(customerList)
        recyclerView.adapter = customerAdapter

        // Fetch customer data
        fetchCustomers()

        // Setup SearchView
        val searchView = view.findViewById<SearchView>(R.id.customerSearch)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                customerAdapter.filter.filter(newText)
                return false
            }

        })


        return view
    }

    private fun fetchCustomers() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance().getReference(userId).child("Customers")

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    customerList.clear()

                    if (!snapshot.exists()) {
                        // No customers found, notify the user
                        Toast.makeText(requireContext(), "No saved customers found.", Toast.LENGTH_SHORT).show()
                        return
                    }

                    for (customerSnapshot in snapshot.children) {
                        val customer = customerSnapshot.getValue(CustomerData::class.java)
                        customer?.let {
                            customerList.add(it)
                        }
                    }

                    if (customerList.isEmpty()) {
                        // No customers found, notify the user
                        Toast.makeText(requireContext(), "No saved customers found.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Notify adapter to refresh the list
                        customerAdapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("fetchCustomers", "Error fetching customers: ${error.message}")
                    Toast.makeText(requireContext(), "Error fetching customers.", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // Handle the case where the user is not logged in or UID is null
            Log.e("fetchCustomers", "User is not logged in or UID is null.")
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
        }
    }




    // Replaces the current fragment with the specified fragment
    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
