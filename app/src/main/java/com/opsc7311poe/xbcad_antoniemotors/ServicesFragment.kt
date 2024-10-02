package com.opsc7311poe.xbcad_antoniemotors

import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

class ServicesFragment : Fragment() {

    private lateinit var imgPlus: ImageView
    private lateinit var linLay: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_services, container, false)

        imgPlus = view.findViewById(R.id.imgPlus)

        imgPlus.setOnClickListener(){
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            replaceFragment(AddServiceFragment())
        }

        //loading services in database
        linLay = view.findViewById(R.id.linlayServiceCards)
        loadServices()

        return view
    }

    private fun loadServices() {

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = Firebase.database
            val servicesRef = database.getReference(userId).child("Services")

            servicesRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    linLay.removeAllViews()

                    for (pulledOrder in snapshot.children) {

                        val service = pulledOrder.getValue(ServiceData::class.java)

                        val cardView = LayoutInflater.from(requireContext()).inflate(R.layout.card_service, linLay, false) as CardView
                        // Populate the card with service data
                        //populate the customer data
                        fetchCustNameAndSurname(service!!.custID!!, cardView)
                        //populate vehicle data
                        fetchVehicleNameAndModel(service!!.vehicleID!!, cardView)
                        //cardView.findViewById<TextView>(R.id.txtVehicleName).text = fetchVehicleNameAndModel
                        cardView.findViewById<TextView>(R.id.txtServiceName).text = service.name
                        cardView.findViewById<TextView>(R.id.txtCost).text = "R ${service.totalCost.toString()}"

                        //changing date values to string
                        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

                        cardView.findViewById<TextView>(R.id.txtDateTakenIn).text = formatter.format(service?.dateReceived)
                        cardView.findViewById<TextView>(R.id.txtDateGivenBack).text = formatter.format(service?.dateReturned)
                        // Set the status ImageView based on status
                        val statusImageView = cardView.findViewById<ImageView>(R.id.imgStatus)
                        when (service?.status) {
                            "Completed" -> statusImageView.setImageResource(R.drawable.vectorstatuscompleted)
                            "Busy" -> statusImageView.setImageResource(R.drawable.vectorstatusbusy)
                            "Not Started" -> statusImageView.setImageResource(R.drawable.vectorstatusnotstrarted)
                        }

                        //functionality to go details page when card is tapped
                        cardView.setOnClickListener {

                            val serviceInfoFragment = ServiceDetailsFragment()
                            //transferring service info using a bundle
                            val bundle = Bundle()
                            bundle.putString("serviceID", pulledOrder.key)
                            serviceInfoFragment.arguments = bundle
                            //changing to service info fragment
                            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            replaceFragment(serviceInfoFragment)
                        }


                        // Add the card to the container
                        linLay.addView(cardView)

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error here
                }
            })
        }
    }

    private fun fetchCustNameAndSurname(custID: String, cv: CardView )
    {
        //fetching name and surname of cust using cust ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val database = Firebase.database
            val custRef = database.getReference(userId).child("Customers")

            custRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (customerSnapshot in snapshot.children) {
                        if(customerSnapshot.key == custID){
                            Log.d("CustomerIDForServiceCard", "CustIDInput: $custID CustIDFound: ${customerSnapshot.key}")
                            var custName = customerSnapshot.child("customerName").getValue(String::class.java)
                            var custSurname = customerSnapshot.child("customerSurname").getValue(String::class.java)
                            cv.findViewById<TextView>(R.id.txtCustName).text = "$custName $custSurname"
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error here
                }
            })
        }
    }

    private fun fetchVehicleNameAndModel(vehID: String, cv: CardView){
        //fetching name and surname of cust using cust ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val database = Firebase.database
            val vehRef = database.getReference(userId).child("Vehicles")

            vehRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (vehSnapshot in snapshot.children) {
                        if(vehSnapshot.key == vehID){
                            Log.d("VehicleIDForServiceCard", "VehIDInput: $vehID VehIDFound: ${vehSnapshot.key}")
                            var vehNumPlate = vehSnapshot.child("vehicleNumPlate").getValue(String::class.java)
                            var vehicleModel = vehSnapshot.child("vehicleModel").getValue(String::class.java)
                            cv.findViewById<TextView>(R.id.txtVehicleName).text = "$vehNumPlate ($vehicleModel)"
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error here
                }
            })
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        Log.d("ServicesFragment", "Replacing fragment: ${fragment::class.java.simpleName}")
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}