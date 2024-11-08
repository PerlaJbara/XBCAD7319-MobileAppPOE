package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ViewInventoryFragment : Fragment() {

    private lateinit var addImage: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PartAdapter
    private lateinit var database: DatabaseReference
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_inventory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addImage = view.findViewById(R.id.imgPlus)
        recyclerView = view.findViewById(R.id.recyclerViewInventory)

        // Set up RecyclerView with click listener
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PartAdapter { selectedPart ->
            // Navigate to AddPartFragment with the selected part data in edit mode
            val fragment = AddPartFragment().apply {
                arguments = Bundle().apply {
                    putString("partId", selectedPart.id) // Pass the part ID
                    putString("partName", selectedPart.partName)
                    putString("partDescription", selectedPart.partDescription)
                    putInt("stockCount", selectedPart.stockCount)
                    putInt("minStock", selectedPart.minStock)
                    putDouble("costPrice", selectedPart.costPrice ?: 0.0)
                }
            }
            replaceFragment(fragment)
        }
        recyclerView.adapter = adapter

        // Set up Firebase database reference
        database = Firebase.database.reference

        // Fetch parts data and display it
        fetchParts()

        // Set up button to navigate to AddPartFragment in add mode
        addImage.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            val fragment = AddPartFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isEditMode", false) // Set add mode flag
                }
            }
            replaceFragment(fragment)
        }
    }

    private fun fetchParts() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = currentUser.uid

        // Listen for parts data changes in Firebase
        database.child("Users").child(userId).child("parts")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val partsList = mutableListOf<PartsData>()
                    snapshot.children.forEach { partSnapshot ->
                        val part = partSnapshot.getValue(PartsData::class.java)
                        part?.let { partsList.add(it) }
                    }

                    // Sort the partsList alphabetically by partName
                    val sortedPartsList = partsList.sortedBy { it.partName?.toLowerCase() ?: "" }

                    // Log the fetched sorted partsList to check the data
                    Log.d("ViewInventoryFragment", "Fetched sorted parts: $sortedPartsList")

                    // Update the adapter with the sorted data
                    adapter.submitList(sortedPartsList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to fetch parts data", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
