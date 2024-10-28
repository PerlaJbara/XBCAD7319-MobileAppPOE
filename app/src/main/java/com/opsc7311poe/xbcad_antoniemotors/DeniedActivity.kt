package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class DeniedActivity : AppCompatActivity() {

    private lateinit var btnRequestAgain: Button
    private lateinit var btnDeleteAcc: Button

    private var businessId: String? = null
    private var userId: String? = null
    private val currentUserId: String? = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_denied)

        btnDeleteAcc = findViewById(R.id.btnDeleteAccountDenied)
        btnRequestAgain = findViewById(R.id.btnRequestAccess)

        // Fetch businessId and userId from Firebase
        fetchUserData()

        btnRequestAgain.setOnClickListener {
            updateStatus()
        }

        btnDeleteAcc.setOnClickListener {
            deleteAccount()
        }
    }

    private fun fetchUserData() {
        if (currentUserId == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
            return
        }

        val dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (businessSnapshot in snapshot.children) {
                    val pendingNode = businessSnapshot.child("Pending")

                    // Search in Pending for the current user ID
                    for (userSnapshot in pendingNode.children) {
                        val managerId = userSnapshot.child("managerID").getValue(String::class.java)

                        // Check if the managerID matches the current user ID
                        if (managerId == currentUserId) {
                            businessId = businessSnapshot.key
                            userId = userSnapshot.key
                            break
                        }
                    }

                    if (businessId != null && userId != null) {
                        break // Exit loop once we have the IDs
                    }
                }

                if (businessId == null || userId == null) {
                    Toast.makeText(this@DeniedActivity, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DeniedActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteAccount() {
        if (businessId == null || userId == null) {
            Toast.makeText(this, "Business ID or User ID is missing.", Toast.LENGTH_SHORT).show()
            return
        }

        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(businessId!!).child("Pending").child(userId!!)
        dbRef.removeValue().addOnSuccessListener {
            Toast.makeText(this, "Account deleted successfully.", Toast.LENGTH_SHORT).show()
            finish() // Close the activity
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to delete account.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateStatus() {
        if (businessId == null || userId == null) {
            Toast.makeText(this, "Business ID or User ID is missing.", Toast.LENGTH_SHORT).show()
            return
        }

        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(businessId!!).child("Pending").child(userId!!)
        dbRef.child("approval").setValue("pending").addOnSuccessListener {
            Toast.makeText(this, "Access request sent again.", Toast.LENGTH_SHORT).show()
            finish() // Close the activity
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to request access.", Toast.LENGTH_SHORT).show()
        }
    }
}
