package com.opsc7311poe.xbcad_antoniemotors

import android.app.DatePickerDialog
import android.app.Service
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class AddServiceFragment : Fragment() {
    private lateinit var spinStatus: Spinner
    private lateinit var btnBack: ImageView
    private lateinit var spinCust: Spinner
    private lateinit var txtName: TextView
    private lateinit var txtDateReceived: TextView
    private lateinit var txtDateReturned: TextView
    private lateinit var txtAllParts: TextView
    private lateinit var txtPartName: TextView
    private lateinit var txtPartCost: TextView
    private lateinit var txtLabourCost: TextView
    private lateinit var btnAddPart: Button
    private lateinit var btnAdd: Button

    //list of parts entered
    private var partsEntered: MutableList<Part> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view = inflater.inflate(R.layout.fragment_add_service, container, false)

        //handling back button
        btnBack = view.findViewById(R.id.ivBackButton)

        btnBack.setOnClickListener() {
            replaceFragment(ServicesFragment())
        }

        //populating spinners
        //status spinner
        spinStatus = view.findViewById(R.id.spinStatus)

        val statuses = arrayOf("Not Started", "Busy", "Completed")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statuses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinStatus.adapter = adapter

        //customer spinner
        // TODO: link customer spinner to DB

        //date picking functionality to date-based textviews
        txtDateReceived = view.findViewById(R.id.txtDateCarReceived)
        txtDateReturned = view.findViewById(R.id.txtDateCarReturned)

        txtDateReceived.setOnClickListener{
            pickDate(txtDateReceived)
        }
        txtDateReturned.setOnClickListener{
            pickDate(txtDateReturned)
        }

        //functionality to add a part
        btnAddPart = view.findViewById(R.id.btnAddPart)

        btnAddPart.setOnClickListener{

            //adding part to list of parts
            txtPartName = view.findViewById(R.id.txtPartName)
            txtPartCost = view.findViewById(R.id.txtPartCost)
            txtAllParts = view.findViewById(R.id.txtAllParts)

            //checking all fields are filled
            if(txtPartName.text.toString().isBlank() || txtPartCost.text.toString().isBlank())
            {
                Toast.makeText( requireContext(), "Please enter part name and cost.", Toast.LENGTH_SHORT).show()
            }
            else
            {

                partsEntered.add(Part(txtPartName.text.toString(), txtPartCost.text.toString().toDouble()))

                //displaying updated list to user
                var allPartsString: String = ""
                for(part in partsEntered)
                {
                    allPartsString += "${part.name}             R${String.format(Locale.getDefault(), "%.2f", part.cost)}"
                    allPartsString += "\n"
                }

                txtAllParts.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                txtAllParts.text = allPartsString
            }

        }

        //adding the service to the DB when add button is clicked
        btnAdd = view.findViewById(R.id.btnAdd)

        btnAdd.setOnClickListener{
            lateinit var serviceEntered: ServiceData

            txtName = view.findViewById(R.id.txtServiceName)
            txtLabourCost = view.findViewById(R.id.txtLabourCost)

            //checking all fields are filled
            if(txtName.text.toString().isBlank() ||
                txtDateReceived.text.toString().isBlank() ||
                txtDateReturned.text.toString().isBlank() ||
                txtAllParts.text.toString().isBlank() ||
                txtLabourCost.text.toString().isBlank() )
            {
                Toast.makeText( requireContext(), "Please ensure all service information is filled correctly.", Toast.LENGTH_SHORT).show()
            }
            else
            {
                //converting date texts to date values
                val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateReceived: Date? = dateFormatter.parse(txtDateReceived.text.toString())
                val dateReturned: Date? = dateFormatter.parse(txtDateReturned.text.toString())

                //totalling cost in order to save
                //totalling parts
                var totalCost: Double = 0.0
                for(part in partsEntered)
                {
                    totalCost += part.cost!!
                }
                //adding labour cost
                totalCost += txtLabourCost.text.toString().toDouble()

                //making service object
                serviceEntered = ServiceData( txtName.text.toString(), "<<CustomerID>>", spinStatus.selectedItem.toString(), dateReceived, dateReturned, partsEntered, txtLabourCost.text.toString().toDouble(), totalCost)

                //adding service object to db
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null)
                {
                    var database = Firebase.database
                    val empRef = database.getReference(userId).child("Services")

                    empRef.push().setValue(serviceEntered)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Service successfully added", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "An error occurred while adding a service:" + it.toString() , Toast.LENGTH_LONG).show()
                        }
                }


                //go back to service landing page
                it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                replaceFragment(ServicesFragment())
            }
        }

        return view


    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    //method for picking dates
    fun pickDate(edittxt: TextView)
    {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val datePickDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->

                val formattedDay = String.format("%02d", selectedDay)
                val formattedMonth = String.format("%02d", selectedMonth + 1)

                val selectedDate = "$formattedDay/${formattedMonth}/$selectedYear"
                edittxt.setText(selectedDate)
            }, year, month, day
        )

        datePickDialog.show()
    }



}