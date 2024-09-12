package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private lateinit var viewCustomersBtn: Button
    private lateinit var btnSettings: ImageView
    private lateinit var btnRegVehicle: Button
    private lateinit var btnGoToTask: ImageView
    private lateinit var taskContainer: LinearLayout // Changed to LinearLayout
    private lateinit var noTasksMessage: TextView
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        viewCustomersBtn = view.findViewById(R.id.btnViewCustomers)
        btnSettings = view.findViewById(R.id.ivSettings)
        btnRegVehicle = view.findViewById(R.id.btnRegisterVehicle)
        btnGoToTask = view.findViewById(R.id.ivGoToAddTask)
        taskContainer = view.findViewById(R.id.taskContainer)
        noTasksMessage = view.findViewById(R.id.txtNotasksToDisplay)

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        btnGoToTask.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(AddTaskFragment())
        }

        viewCustomersBtn.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(CustomerFragment())
        }

        btnSettings.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(SettingsFragment())
        }

        btnRegVehicle.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(RegisterVehicleFragment())
        }

        loadTasks()

        return view
    }

    private fun loadTasks() {
        val database = Firebase.database.reference.child(userId).child("todolist")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                taskContainer.removeAllViews() // Clear the LinearLayout before adding new tasks

                if (!snapshot.exists()) {
                    noTasksMessage.visibility = View.VISIBLE
                    noTasksMessage.text = "No tasks available at the moment."
                    return
                }

                noTasksMessage.visibility = View.GONE

                for (taskSnapshot in snapshot.children) {
                    val taskId = taskSnapshot.key
                    val taskDescription = taskSnapshot.child("taskDescription").getValue(String::class.java)

                    if (taskId != null && taskDescription != null) {
                        // Create a task view with a checkbox
                        val taskView = layoutInflater.inflate(R.layout.task_item, taskContainer, false)
                        val taskCheckBox = taskView.findViewById<CheckBox>(R.id.taskCheckBox)
                        taskCheckBox.text = taskDescription

                        taskCheckBox.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                database.child(taskId).removeValue()
                                    .addOnSuccessListener {
                                        taskContainer.removeView(taskView)
                                        Toast.makeText(requireContext(), "Task completed!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(requireContext(), "Failed to remove task", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }

                        // Add the task view to the LinearLayout
                        taskContainer.addView(taskView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Failed to load tasks: ${error.message}")
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
