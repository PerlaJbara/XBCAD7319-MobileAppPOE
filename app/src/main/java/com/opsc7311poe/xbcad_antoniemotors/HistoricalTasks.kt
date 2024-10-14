package com.opsc7311poe.xbcad_antoniemotors

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class HistoricalTasks : Fragment() {

    private lateinit var svTasksOld: ScrollView // Reference to the ScrollView
    private lateinit var taskContainer: LinearLayout // Reference to the LinearLayout inside ScrollView

    private var completedTasks: List<Tasks>? = null // List to store completed tasks
    private lateinit var dpStartDate: TextView
    private lateinit var dpEndDate: TextView

    private var startDate: Long? = null
    private var endDate: Long? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    companion object {
        private const val ARG_COMPLETED_TASKS = "completed_tasks"

        fun newInstance(completedTasks: List<Tasks>): HistoricalTasks {
            val fragment = HistoricalTasks()
            val args = Bundle()
            args.putParcelableArrayList(ARG_COMPLETED_TASKS, ArrayList(completedTasks))
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            completedTasks = it.getParcelableArrayList(ARG_COMPLETED_TASKS)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_historical_tasks, container, false)

        // Reference the ScrollView and LinearLayout from the layout
        svTasksOld = view.findViewById(R.id.svTasksOld) as ScrollView // Use ScrollView for svTasksOld
        taskContainer = view.findViewById(R.id.svlinlay) // Use svlinlay for the inner LinearLayout

        dpStartDate = view.findViewById(R.id.dpStartDate)
        dpEndDate = view.findViewById(R.id.dpEndDate)

        // Set up click listeners for date pickers
        dpStartDate.setOnClickListener { showDatePickerDialog(true) }
        dpEndDate.setOnClickListener { showDatePickerDialog(false) }

        // Display completed tasks
        displayCompletedTasks()

        return view
    }

    private fun displayCompletedTasks() {
        taskContainer.removeAllViews() // Clear previous task views

        // Filter and display tasks that have a completed date
        val tasksToDisplay = completedTasks?.filter { it.completedDate != null } ?: emptyList()

        tasksToDisplay.forEach { task ->
            val taskView = layoutInflater.inflate(R.layout.old_tasks_item, taskContainer, false)

            // Get references to the TextViews in each task item layout
            val numberPlateText = taskView.findViewById<TextView>(R.id.numberPlateText)
            val taskDescriptionText = taskView.findViewById<TextView>(R.id.taskDescriptionText)
            val taskCompletedDateText = taskView.findViewById<TextView>(R.id.taskDateDone)

            // Set data from the task
            numberPlateText.text = task.vehicleNumberPlate ?: "No Number Plate"
            taskDescriptionText.text = task.taskDescription ?: "No Description"
            taskCompletedDateText.text = "Date Completed: ${dateFormat.format(Date(task.completedDate ?: 0))}"

            // Add click listener to show task details in a popup
            taskView.setOnClickListener { showTaskPopup(task) }

            // Add the task view to the LinearLayout container inside ScrollView
            taskContainer.addView(taskView)
        }
    }

    private fun showTaskPopup(task: Tasks) {
        val popupView = layoutInflater.inflate(R.layout.popup_task_details, null)

        val popupDialog = AlertDialog.Builder(requireContext())
            .setView(popupView)
            .create()

        // Populate the popup with task details
        val numberPlateText = popupView.findViewById<TextView>(R.id.txtNumberPlate)
        val taskDescriptionText = popupView.findViewById<TextView>(R.id.txtTaskDescription)
        val taskCreatedDateText = popupView.findViewById<TextView>(R.id.txtTaskCreatedDate)
        val taskCompletedDateText = popupView.findViewById<TextView>(R.id.txtTaskCompletedDate)

        numberPlateText.text = task.vehicleNumberPlate
        taskDescriptionText.text = "Task Description: ${task.taskDescription}"
        taskCreatedDateText.text = "Date Created: ${dateFormat.format(Date(task.creationDate ?: 0))}"
        taskCompletedDateText.text = "Date Completed: ${dateFormat.format(Date(task.completedDate ?: 0))}"

        popupDialog.show()
    }

    private fun showDatePickerDialog(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val selectedDate = calendar.time
                if (isStartDate) {
                    startDate = selectedDate.time
                    dpStartDate.text = dateFormat.format(selectedDate)
                } else {
                    endDate = selectedDate.time
                    dpEndDate.text = dateFormat.format(selectedDate)
                }
                filterTasksByDate()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun filterTasksByDate() {
        val filteredTasks = completedTasks?.filter { task ->
            val taskCompletedDate = task.completedDate

            val isWithinRange = taskCompletedDate != null &&
                    (startDate?.let { taskCompletedDate >= it } ?: true) &&
                    (endDate?.let { taskCompletedDate <= it } ?: true)

            isWithinRange
        }

        taskContainer.removeAllViews() // Clear current views

        filteredTasks?.forEach { task ->
            val taskView = layoutInflater.inflate(R.layout.old_tasks_item, taskContainer, false)

            val numberPlateText = taskView.findViewById<TextView>(R.id.numberPlateText)
            val taskDescriptionText = taskView.findViewById<TextView>(R.id.taskDescriptionText)
            val taskCompletedDateText = taskView.findViewById<TextView>(R.id.taskDateDone)

            numberPlateText.text = task.vehicleNumberPlate ?: "No Number Plate"
            taskDescriptionText.text = task.taskDescription ?: "No Description"
            taskCompletedDateText.text = "Date Completed: ${dateFormat.format(Date(task.completedDate ?: 0))}"

            taskView.setOnClickListener { showTaskPopup(task) }

            taskContainer.addView(taskView)
        }
    }
}
