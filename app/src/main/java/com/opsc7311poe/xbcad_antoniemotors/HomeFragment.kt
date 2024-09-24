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
import android.graphics.Color

class HomeFragment : Fragment() {

    private lateinit var viewCustomersBtn: Button
    private lateinit var btnSettings: ImageView
    private lateinit var btnRegVehicle: Button
    private lateinit var btnGoToTask: ImageView
    private lateinit var taskContainer: LinearLayout
    private lateinit var noTasksMessage: TextView

    private lateinit var txtBusy: TextView
    private lateinit var txtToStart: TextView
    private lateinit var txtCompleted: TextView

    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Ensure all IDs are correct and present in fragment_home.xml
        viewCustomersBtn = view.findViewById(R.id.btnViewCustomers)
        btnSettings = view.findViewById(R.id.ivSettings)
        btnRegVehicle = view.findViewById(R.id.btnRegisterVehicle)
        btnGoToTask = view.findViewById(R.id.ivGoToAddTask)
        taskContainer = view.findViewById(R.id.taskContainer)
        noTasksMessage = view.findViewById(R.id.txtNotasksToDisplay)

        txtBusy = view.findViewById(R.id.txtCarsInProgress)
        txtToStart = view.findViewById(R.id.txtCarsToBeDone)
        txtCompleted = view.findViewById(R.id.txtCarsCompleted)

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
        loadServiceStatuses()

        return view
    }

    private fun loadTasks() {
        val database = Firebase.database.reference.child(userId).child("todolist")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Ensure the fragment is attached before inflating views
                if (!isAdded) return

                taskContainer.removeAllViews()

                if (!snapshot.exists()) {
                    noTasksMessage.visibility = View.VISIBLE
                    noTasksMessage.text = "No tasks available at the moment."
                    return
                }

                noTasksMessage.visibility = View.GONE

                // To store colors for number plates
                val plateColorMap = mutableMapOf<String, Int>()
                var colorIndex = 0
                val colors = listOf(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW)

                for (taskSnapshot in snapshot.children) {
                    val taskId = taskSnapshot.key
                    val taskDescription = taskSnapshot.child("taskDescription").getValue(String::class.java)
                    val numberPlate = taskSnapshot.child("numberPlate").getValue(String::class.java)

                    if (taskId != null && taskDescription != null && numberPlate != null) {
                        val taskView = layoutInflater.inflate(R.layout.task_item, taskContainer, false)
                        val taskCheckBox = taskView.findViewById<CheckBox>(R.id.taskCheckBox)
                        val numberPlateText = taskView.findViewById<TextView>(R.id.numberPlateText)
                        val taskDescriptionText = taskView.findViewById<TextView>(R.id.taskDescriptionText)

                        // Set task description and checkbox
                        taskDescriptionText.text = taskDescription
                        taskCheckBox.text = ""

                        // Assign a color to the number plate and reuse it if it has been assigned before
                        if (!plateColorMap.containsKey(numberPlate)) {
                            plateColorMap[numberPlate] = colors[colorIndex % colors.size]
                            colorIndex++
                        }

                        // Set the number plate text and background color
                        numberPlateText.text = numberPlate
                        numberPlateText.setBackgroundColor(plateColorMap[numberPlate] ?: Color.GRAY)

                        // Checkbox listener to remove the task when checked
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

                        // Add the view to the task container
                        taskContainer.addView(taskView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Failed to load tasks: ${error.message}")
            }
        })
    }


    private fun loadServiceStatuses() {
        // Get the current user's ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Check if userId is not null
        userId?.let { uid ->
            // Reference the services node under the current user's ID
            val database = Firebase.database.reference.child(uid).child("services")

            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("HomeFragment", "Data snapshot received with ${snapshot.childrenCount} services.")

                    var busyCount = 0
                    var completedCount = 0
                    var notStartedCount = 0

                    if (!snapshot.exists()) {
                        if (isAdded) {
                            txtToStart.text = "Nothing To Be Started"
                            txtBusy.text = "Nothing Is In Progress"
                            txtCompleted.text = "Nothing Is Completed"
                        }
                        return
                    }

                    for (serviceSnapshot in snapshot.children) {
                        val status = serviceSnapshot.child("status").getValue(String::class.java)
                        Log.d("HomeFragment", "Status for service: $status")

                        when (status) {
                            "Busy" -> busyCount++
                            "Completed" -> completedCount++
                            "Not Started" -> notStartedCount++
                        }
                    }

                    if (isAdded) {
                        txtToStart.text = "$notStartedCount Cars To Start"
                        txtBusy.text = "$busyCount Cars In Progress"
                        txtCompleted.text = "$completedCount Cars Completed"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("HomeFragment", "Failed to load service statuses: ${error.message}")
                }
            })
        } ?: run {
            Log.e("HomeFragment", "User ID is null.")
            // Handle the case where userId is null, although it's less likely in this context
        }
    }


    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
