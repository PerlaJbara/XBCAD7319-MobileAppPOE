package com.opsc7311poe.xbcad_antoniemotors

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.ktx.database
import java.text.SimpleDateFormat
import java.util.Locale


class ServiceDetailsFragment : Fragment() {
   private lateinit var btnBack: ImageView
   private lateinit var txtName: TextView
   private lateinit var btnCust: Button
   private lateinit var btnSave: Button
   private lateinit var btnDelete: Button
   private lateinit var imgStatus: ImageView
   private lateinit var txtDateCarReceived: TextView
   private lateinit var txtDateCarReturned: TextView
   private lateinit var txtParts: TextView
   private lateinit var txtLabourCost: TextView

    val database = Firebase.database
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_service_details, container, false)

        //back button functionality
        btnBack = view.findViewById(R.id.ivBackButton)

        btnBack.setOnClickListener(){
            replaceFragment(ServicesFragment())
        }

        //connecting all UI elements
        txtName = view.findViewById(R.id.txtName)
        btnCust = view.findViewById(R.id.btnCustName)
        imgStatus = view.findViewById(R.id.imgStatus)
        txtDateCarReceived = view.findViewById(R.id.txtDateCarReceived)
        txtDateCarReturned = view.findViewById(R.id.txtDateCarReturned)
        txtParts = view.findViewById(R.id.txtParts)
        txtLabourCost = view.findViewById(R.id.txtLabourCost)

        val serRef = database.getReference(userId!!).child("Services")
        val serviceID = arguments?.getString("serviceID")
        //fetching service data from DB
        if (userId != null && serviceID != null) {

            // Query the database to find the service with the matching ID
            val query = serRef.child(serviceID)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Directly fetch the service object without looping
                    val fetchedService = dataSnapshot.getValue(ServiceData::class.java)

                    if (fetchedService != null) {
                        // Assign fetched service info to text views
                        txtName.text = fetchedService.name
                        btnCust.text = fetchedService.custID
                        txtLabourCost.text = " R ${fetchedService.labourCost}"

                        // Convert dates to string values
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        txtDateCarReceived.text = formatter.format(fetchedService.dateReceived)
                        txtDateCarReturned.text = formatter.format(fetchedService.dateReturned)

                        // Populate parts textbox
                        var allPartsString = ""
                        fetchedService.parts?.forEach { part ->
                            allPartsString += "${part.name}             R${String.format(Locale.getDefault(), "%.2f", part.cost)}\n"
                        }
                        txtParts.text = allPartsString

                        // Handle status display
                        when (fetchedService.status) {
                            "Completed" -> imgStatus.setImageResource(R.drawable.vectorstatuscompleted)
                            "Busy" -> imgStatus.setImageResource(R.drawable.vectorstatusbusy)
                            "Not Started" -> imgStatus.setImageResource(R.drawable.vectorstatusnotstrarted)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Service not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(requireContext(), "Error reading from the database: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        //save button functionality
        btnSave = view.findViewById(R.id.btnSave)
        btnSave.setOnClickListener(){

        }

        //delete button functionality
        btnDelete = view.findViewById(R.id.btnDelete)
        btnDelete.setOnClickListener(){

            //making alert dialog to check if user is sure they want to delete service
            val dialogConfirm = AlertDialog.Builder(requireContext())
            dialogConfirm.setTitle("Confirm Delete")
            dialogConfirm.setMessage("Are you sure you want to permanently delete this service.")

            //if user taps yes
            dialogConfirm.setPositiveButton("Yes") { dialog, _ ->
                //deleting service
                val database = Firebase.database
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val serRef = database.getReference(userId!!).child("Services")

                serRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        //deleting service
                        serRef.child(serviceID!!).removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Service Successfully Removed", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to Remove Service", Toast.LENGTH_SHORT).show()
                            }

                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(requireContext(), "Error reading from the database: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                    }
                })
                dialog.dismiss()
                //returning to services overview page
                replaceFragment(ServicesFragment())
            }

            //if user taps no
            dialogConfirm.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

            val alert = dialogConfirm.create()
            alert.show()
        }

        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}