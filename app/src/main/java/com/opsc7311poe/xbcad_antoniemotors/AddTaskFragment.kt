package com.opsc7311poe.xbcad_antoniemotors

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.ktx.Firebase

class AddTaskFragment : Fragment() {

    private lateinit var btnBack: ImageView
    private lateinit var txtTask: EditText
    private lateinit var btnSubmit: Button
    private lateinit var charCount: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String

    private val MAX_CHAR_LIMIT = 400

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_task, container, false)

        btnBack = view.findViewById(R.id.ivBackButton)
        txtTask = view.findViewById(R.id.txtTaskDescription)
        btnSubmit = view.findViewById(R.id.btnAddTask)
        charCount = view.findViewById(R.id.charCount)


        db = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""


        txtTask.filters = arrayOf(InputFilter.LengthFilter(MAX_CHAR_LIMIT))
        txtTask.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val remainingChars = MAX_CHAR_LIMIT - (s?.length ?: 0)
                charCount.text = "$remainingChars characters left"
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        btnBack.setOnClickListener {
            replaceFragment(HomeFragment())
        }


        btnSubmit.setOnClickListener {
            submitTask()
        }

        return view
    }

    private fun submitTask() {
        // Check if the task description is blank
        val taskDescription = txtTask.text.toString().trim()

        if (taskDescription.isBlank()) {
            Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the current user's ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            // Initialize Firebase Database
            val database = Firebase.database

            val entryId = database.getReference(uid).child("todolist").push().key

            entryId?.let { id ->

                val entry = Tasks(
                    taskID = id,
                    taskDescription = taskDescription
                )

                // Create a reference to the specific entry in the database
                val entryRef = database.getReference(uid).child("todolist").child(id)

                // Save the task to Firebase
                entryRef.setValue(entry)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Task successfully added!", Toast.LENGTH_LONG)
                            .show()
                        // Navigate back to MainActivity
                        val intent = Intent(activity, MainActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "An error occurred while saving the entry: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } ?: run {
                Toast.makeText(requireContext(), "Failed to generate entry ID.", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(requireContext(), "User ID is null.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }



}



