package com.opsc7311poe.xbcad_antoniemotors

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AdminAnalyticsFragment : Fragment() {

    private lateinit var empBarChart: BarChart

    private lateinit var businessId: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_admin_analytics, container, false)

        businessId = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getString("business_id", null)!!

        empBarChart = view.findViewById(R.id.barChart)
        loadBarChart(empBarChart)

        return view
    }

    private fun loadBarChart(barChart: BarChart) {
        val database = Firebase.database.reference.child("Users/$businessId/EmployeeTasks")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(requireContext(), "No tasks available", Toast.LENGTH_SHORT).show()
                    return
                }

                val taskCounts = mutableMapOf<String, Int>() // Map to store task counts per employee
                val employeeIDs = mutableSetOf<String>()     // Set to collect unique employee IDs

                // Loop through tasks and count tasks for each employee
                for (taskSnapshot in snapshot.children) {
                    val employeeID = taskSnapshot.child("employeeID").getValue(String::class.java)
                    if (employeeID != null) {
                        taskCounts[employeeID] = taskCounts.getOrDefault(employeeID, 0) + 1
                        employeeIDs.add(employeeID)
                    }
                }

                // Fetch employee names in bulk and update the chart once all names are retrieved
                fetchEmployeeNames(employeeIDs) { employeeNamesMap ->
                    val barEntries = mutableListOf<BarEntry>()
                    val employeeNames = mutableListOf<String>() // For x-axis labels
                    var index = 0f

                    for ((employeeID, count) in taskCounts) {
                        barEntries.add(BarEntry(index, count.toFloat()))
                        employeeNames.add(employeeNamesMap[employeeID] ?: "Unknown")
                        index++
                    }

                    // Set up the BarDataSet
                    val dataSet = BarDataSet(barEntries, "Tasks Assigned").apply {
                        colors = generateColors(barEntries.size)
                        valueTextSize = 12f
                    }

                    // Set up the BarData
                    val barData = BarData(dataSet)
                    barData.barWidth = 0.9f

                    // Set up the BarChart
                    barChart.apply {
                        data = barData
                        description.isEnabled = false
                        setFitBars(true)
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            valueFormatter = IndexAxisValueFormatter(employeeNames)
                            granularity = 1f
                            setDrawGridLines(false)
                        }
                        axisLeft.apply {
                            axisMinimum = 0f
                            granularity = 1f
                        }
                        axisRight.isEnabled = false
                        animateY(1000)
                        invalidate() // Refresh the chart
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TaskGraphError", "Error loading task data: ${error.message}")
                Toast.makeText(requireContext(), "Failed to load task data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to fetch all employee names
    private fun fetchEmployeeNames(
        employeeIDs: Set<String>,
        callback: (Map<String, String?>) -> Unit
    ) {
        val empRef = Firebase.database.reference.child("Users/$businessId/Employees")
        empRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val employeeNamesMap = mutableMapOf<String, String?>()
                for (employeeID in employeeIDs) {
                    val employeeSnapshot = snapshot.child(employeeID)
                    val firstName = employeeSnapshot.child("firstName").getValue(String::class.java)
                    val lastName = employeeSnapshot.child("lastName").getValue(String::class.java)
                    employeeNamesMap[employeeID] = "$firstName $lastName"
                }
                callback(employeeNamesMap)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("EmployeeNameError", "Error fetching employee names: ${error.message}")
                callback(emptyMap())
            }
        })
    }

    // Function to generate random colors for the bars
    fun generateColors(size: Int): List<Int> {
        val baseColors = listOf(
            "#CA2F2E",
            "#506c7a",
            "#038A39"
        )
        // Repeat colors if the dataset is larger than the base color list
        return List(size) { index -> Color.parseColor(baseColors[index % baseColors.size]) }
    }
}
