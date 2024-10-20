package com.opsc7311poe.xbcad_antoniemotors

import android.Manifest
import android.app.Activity
import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.opsc7311poe.xbcad_antoniemotors.CustomerData
import java.io.ByteArrayOutputStream
import java.util.*

class RegisterVehicleFragment : Fragment() {

    //private lateinit var spinCustomer: Spinner
    private lateinit var edtCustomer: EditText
    private lateinit var edtVehicleNoPlate: EditText
    private lateinit var edtVehicleMake: EditText
    private lateinit var spnVehiclePOR: Spinner
    private lateinit var edtVehicleModel: EditText
    private lateinit var edtVinNumber: EditText
    private lateinit var edtVehicleKms: EditText
    private lateinit var btnSubmitRegVehicle: Button
    private lateinit var imgCars: ImageView
    private lateinit var galleryCars: ImageView
    private lateinit var display: ImageView
    private var selectedCustomerId: String = ""
    private val imageUris = mutableListOf<Uri>()
    private lateinit var btnBack: ImageView
    private lateinit var vehiclePORList: ArrayList<String> // To store the values (e.g., "CA")
    private lateinit var vehiclePORAdapter: ArrayAdapter<String>
    private lateinit var ynpYearPicker : NumberPicker


    // Firebase references
    // Reference to the Customers node in your database
    val databaseRef = FirebaseDatabase.getInstance().getReference("Customers")
    val databaseVRef = FirebaseDatabase.getInstance().getReference("VehicleMake")
    // List to hold vehicle makes
    val vehicleMakesList = mutableListOf<String>()

    private val database = FirebaseDatabase.getInstance().getReference("Vehicles")
    private val storage = FirebaseStorage.getInstance().getReference("VehicleImages")

    private val PICK_IMAGES_REQUEST = 100

    //accessing the camera
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val CAMERA_REQUEST_CODE = 101


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register_vehicle, container, false)

        // Initialize views
        //spinCustomer = view.findViewById(R.id.customerspinner)
        edtCustomer = view.findViewById(R.id.edtCustomerNames)
        edtVehicleNoPlate = view.findViewById(R.id.edttxtVehicleNoPlate)
        edtVehicleModel = view.findViewById(R.id.edttxtVehicleModel)
        edtVehicleMake = view.findViewById(R.id.edttxtVehicleMake)
        spnVehiclePOR = view.findViewById(R.id.spinnerPOR)
        edtVinNumber = view.findViewById(R.id.edttxtVinNumber)
        edtVehicleKms = view.findViewById(R.id.edttxtVehicleKms)
        btnSubmitRegVehicle = view.findViewById(R.id.btnSubmitRegVehicle)
        btnBack = view.findViewById(R.id.ivBackButton)
        display = view.findViewById(R.id.capturedImage)
        galleryCars = view.findViewById(R.id.imgAttachImage)
        imgCars = view.findViewById(R.id.imgCamera)
        ynpYearPicker = view.findViewById(R.id.npYearPicker)


        edtCustomer.setOnClickListener {
            showCustomerSelectionDialog()
        }


        // Populate Spinner with customer names
       // val userId = FirebaseAuth.getInstance().currentUser?.uid
        //userId?.let { populateCustomerSpinner() } //it

        vehiclePORList = ArrayList()

        // Set up adapter for Spinner
        vehiclePORAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, vehiclePORList)
        vehiclePORAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnVehiclePOR.adapter = vehiclePORAdapter

        fetchVehiclePORData()

        fetchVehicleMakes()

        // Image upload click listeners
        galleryCars.setOnClickListener { selectImagesFromGallery() }
        //imgCars.setOnClickListener { captureImageWithCamera() }
        imgCars.setOnClickListener { handleCameraPermission() }



        edtVehicleMake.setOnClickListener {
            showVehicleMakeDialog()
        }

        setupYearPicker()

        setupEditorActions()


        edtVehicleModel.setOnClickListener {
            val selectedMake = edtVehicleMake.text.toString()
            if (selectedMake.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a vehicle make first.", Toast.LENGTH_SHORT).show()
            } else {
                // Call the function to show the pop-up for model selection
                showVehicleModelDialog(selectedMake)
            }
        }

        // Submit vehicle registration
        btnSubmitRegVehicle.setOnClickListener {
            registerVehicle()
        }

        btnBack.setOnClickListener {
            replaceFragment(HomeFragment())
        }



        return view
    }

    fun showVehicleMakeDialog() {
        // Inflate the dialog view layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_vehicle_make, null)
        val searchView = dialogView.findViewById<SearchView>(R.id.searchVehicleMake)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerVehicleMake)

        // Set up RecyclerView with a LinearLayoutManager
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Create the AlertDialog before setting up the adapter
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Select Vehicle Make")
            .setNegativeButton("Cancel", null)
            .setCancelable(true)
            .create()

        // Adapter for displaying vehicle makes
        val adapter = VehicleMakeAdapter(vehicleMakesList) { selectedMake ->
            // Handle the vehicle make selection
            edtVehicleMake.setText(selectedMake)
            // Dismiss the dialog when an item is selected
            alertDialog.dismiss()
        }

        recyclerView.adapter = adapter

        // Filter the vehicle makes based on search query
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        // Show the dialog
        alertDialog.show()
    }


    private fun showVehicleModelDialog(selectedMake: String) {
        // Create a dialog
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_vehicle_model) // Custom layout for the model selection

        // Reference UI components inside the dialog
        val listViewModels = dialog.findViewById<ListView>(R.id.listViewModels)
        val searchViewModels = dialog.findViewById<SearchView>(R.id.searchViewModels)

        val modelList = ArrayList<String>() // List to hold models for the selected make
        val modelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, modelList)
        listViewModels.adapter = modelAdapter

        // Fetch models from Firebase for the selected make
        fetchVehicleModels(selectedMake, modelList, modelAdapter)

        // Handle search functionality
        searchViewModels.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                modelAdapter.filter.filter(newText)
                return false
            }
        })

        // Handle model selection
        listViewModels.setOnItemClickListener { _, _, position, _ ->
            val selectedModel = modelList[position]
            edtVehicleModel.setText(selectedModel) // Set the selected model in the edit text
            dialog.dismiss() // Close the dialog
        }

        dialog.show() // Show the dialog
    }



    private fun fetchVehicleMakes(){
        // Fetch vehicle makes from Firebase
        databaseVRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                vehicleMakesList.clear()  // Clear list before adding
                for (snapshot in dataSnapshot.children) {
                    val vehicleMake = snapshot.key.toString()
                    vehicleMakesList.add(vehicleMake)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
                Log.e("FirebaseError", "Database error: ${databaseError.message}")
            }
        })
    }

    private fun fetchVehicleModels(selectedMake: String, modelList: ArrayList<String>, modelAdapter: ArrayAdapter<String>) {
        // Reference to VehicleData/VehicleMake/selectedMake/Models in Firebase
        val modelsRef = FirebaseDatabase.getInstance().getReference("VehicleMake/$selectedMake/Models")

        modelsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                modelList.clear() // Clear the list first
                for (modelSnapshot in snapshot.children) {
                    val model = modelSnapshot.getValue(String::class.java)
                    if (model != null) {
                        modelList.add(model)
                    }
                }
                // Notify adapter about data change
                modelAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error fetching vehicle models: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun fetchVehiclePORData() {
        // Reference to VehiclePOR node in Firebase
        val vehiclePORRef = FirebaseDatabase.getInstance().getReference("VehiclePOR")

        // Attach listener to fetch data
        vehiclePORRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear the list first
                vehiclePORList.clear()

                // Loop through the data to get values
                for (vehiclePORSnapshot in snapshot.children) {
                    val value = vehiclePORSnapshot.getValue(String::class.java)
                    if (value != null) {
                        vehiclePORList.add(value) // Add only the values to the list
                    }
                }

                // Notify adapter about the data change
                vehiclePORAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupYearPicker() {

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val startYear = 1900 // Define the starting year
        val endYear = currentYear // Define the end year as the current year

        ynpYearPicker?.minValue = startYear
        ynpYearPicker?.maxValue = endYear
        ynpYearPicker?.value = currentYear // Set the current year as default

        // Optional: Listener to handle year selection
        ynpYearPicker?.setOnValueChangedListener { picker, oldVal, newVal ->
            Log.d("SelectedYear", "Year: $newVal")
            if (newVal > currentYear) {
                Toast.makeText(requireContext(), "A car model year cannot be greater than the current year", Toast.LENGTH_SHORT).show()
                ynpYearPicker.value = currentYear
            }
        }
    }




    private fun setupEditorActions() {
        edtVehicleNoPlate.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                edtVehicleModel.requestFocus()
                true
            } else {
                false
            }
        }

        edtVehicleModel.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                edtVinNumber.requestFocus()
                true
            } else {
                false
            }
        }

        edtVinNumber.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                edtVehicleKms.requestFocus()
                true
            } else {
                false
            }
        }

        edtVehicleKms.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                galleryCars.requestFocus()
                true
            } else {
                false
            }
        }
    }






    private fun showCustomerSelectionDialog() {
        // Inflate the dialog view
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_customer_names, null)
        val searchView = dialogView.findViewById<SearchView>(R.id.searchCustomerName)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerCustomerName)

        // Set up the RecyclerView with the adapter
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Select Customer")
            .setNegativeButton("Cancel", null)
            .create()

        // Fetch customers from Firebase
        populateCustomerList { customers ->
            // Initialize the adapter
            var filteredCustomers = customers.toList()
            val adapter = VehicleCustomerAdapter(filteredCustomers) { selectedCustomer ->
                // When a customer is selected, set the text to EditText and close the dialog
                edtCustomer.setText(selectedCustomer)
                dialog.dismiss()  // Dismiss dialog on selection
            }

            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            // Search filtering logic
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = false

                override fun onQueryTextChange(newText: String?): Boolean {
                    val searchText = newText?.lowercase()?.trim()
                    filteredCustomers = if (searchText.isNullOrEmpty()) {
                        customers
                    } else {
                        customers.filter { it.lowercase().contains(searchText) }
                    }
                    adapter.updateCustomers(filteredCustomers)  // Update the adapter
                    return true
                }
            })

            dialog.show()  // Show the dialog
        }
    }


    private fun populateCustomerList(onCustomersFetched: (List<String>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Firebase reference to user's customers
        val customerRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Customers")

        customerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val customerNames = mutableListOf<String>()
                for (customerSnapshot in snapshot.children) {
                    val firstName = customerSnapshot.child("customerName").getValue(String::class.java)
                    val lastName = customerSnapshot.child("customerSurname").getValue(String::class.java)
                    if (!firstName.isNullOrEmpty() && !lastName.isNullOrEmpty()) {
                        customerNames.add("$firstName $lastName")
                    }
                }
                onCustomersFetched(customerNames)  // Pass the customer names back
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error loading customer data: ${error.message}")
            }
        })
    }


    private fun registerVehicle() {
        val vehicleNoPlate = edtVehicleNoPlate.text.toString().trim()
        val vehicleModel = edtVehicleModel.text.toString().trim()
        val vinNumber = edtVinNumber.text.toString().trim()
        val vehicleKms = edtVehicleKms.text.toString().trim()
        val vehiclePOR = spnVehiclePOR.selectedItem.toString() // POR from spinner
        val vehicleOwner = edtCustomer.text.toString() // Get the selected customer's name
        //val vehicleOwner = spinCustomer.selectedItem.toString() // Get the selected customer's name


        // Validate inputs
        if (vehicleNoPlate.isEmpty() || vehicleModel.isEmpty() || vehicleKms.isEmpty()) {
            Toast.makeText(context, "Please fill in all mandatory fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate vehicle number plate
        if (!vehicleNoPlate.matches(Regex("^[a-zA-Z0-9]{1,7}$"))) {
            Toast.makeText(context, "Invalid number plate. It must be alphanumeric and 1-7 characters long.", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate VIN number (exactly 17 characters, alphanumeric)
        if (!vinNumber.matches(Regex("^[a-zA-Z0-9]{17}$"))) {
            Toast.makeText(context, "Invalid VIN number. It must be exactly 17 characters long and alphanumeric.", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate vehicle kilometers
        if (!vehicleKms.matches(Regex("^[0-9]+$"))) {
            Toast.makeText(context, "Invalid kilometers. It must be a number.", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUris.isEmpty()) {
            Toast.makeText(context, "Please upload at least one image", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedCustomerId.isEmpty()) {
            Toast.makeText(context, "Please select a customer", Toast.LENGTH_SHORT).show()
            return
        }


        if (imageUris.isEmpty()) {
            Toast.makeText(context, "Please upload at least one image", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedCustomerId.isEmpty()) {
            Toast.makeText(context, "Please select a customer", Toast.LENGTH_SHORT).show()
            return
        }

        // Combine VehicleNumPlate and VehiclePOR
        val fullVehicleNumPlate = "$vehicleNoPlate $vehiclePOR"

        // Get the selected year from the NumberPicker
        val selectedYear = ynpYearPicker.value.toString()

        // Constructing the VehicleData object
        val vehicle = VehicleData(
            VehicleOwner = vehicleOwner, // Customer's full name selected from spinner
            customerID = selectedCustomerId, // Customer ID from spinner
            VehicleNumPlate = fullVehicleNumPlate,
            VehiclePOR = vehiclePOR,
            VehicleModel = vehicleModel,
            VehicleMake = edtVehicleMake.text.toString(),
            VehicleYear = selectedYear,
            //VehiclePOR = spnVehiclePOR.selectedItem.toString(),
            VinNumber = if (vinNumber.isEmpty()) "N/A" else vinNumber,
            VehicleKms = vehicleKms
        )

        // Get current user (admin/vehicle owner) ID
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            val userId = it.uid

            // Save the vehicle under the userId -> Vehicles path
            val vehicleRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(userId)
                .child("Vehicles")
                .push()  // Generate a unique ID for the vehicle

            vehicle.vehicleId = vehicleRef.key ?: ""

            vehicleRef.setValue(vehicle).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    uploadVehicleImages(vehicleRef.key!!)
                    Toast.makeText(context, "Vehicle registered successfully", Toast.LENGTH_SHORT).show()
                    clearInputFields()
                } else {
                    Toast.makeText(context, "Failed to register vehicle", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun clearInputFields() {
        edtVehicleNoPlate.text.clear()
        edtVehicleModel.text.clear()
        edtVinNumber.text.clear()
        edtVehicleKms.text.clear()
        imageUris.clear()
        display.setImageResource(R.drawable.camera) // Reset image display
    }



    private fun uploadVehicleImages(vehicleId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && imageUris.isNotEmpty()) {
            val userId = currentUser.uid

            for (uri in imageUris) {
                val uniqueImageId = UUID.randomUUID().toString()
                val storageRef = storage.child("$userId/Vehicles/$vehicleId/$uniqueImageId.jpg")

                storageRef.putFile(uri)
                    .addOnSuccessListener { taskSnapshot ->
                        // Get the download URL
                        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            // Save the image URL in the database under the vehicle node
                            val imageRef = FirebaseDatabase.getInstance().getReference("Users")
                                .child(userId)
                                .child("Vehicles")
                                .child(vehicleId)
                                .child("images")
                                .child(uniqueImageId)

                            imageRef.setValue(downloadUri.toString())
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }


    private fun handleGalleryImages(data: Intent?) {
        data?.let {
            if (it.clipData != null) {
                for (i in 0 until it.clipData!!.itemCount) {
                    imageUris.add(it.clipData!!.getItemAt(i).uri)
                }
            } else if (it.data != null) {
                imageUris.add(it.data!!)
            }
        }
    }

    private fun handleCameraImage(data: Intent?) {
        val photoBitmap = data?.extras?.get("data") as? Bitmap
        photoBitmap?.let {
            display.setImageBitmap(it)
            imageUris.add(getImageUriFromBitmap(requireContext(), it))
        }
    }


    private fun selectImagesFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, PICK_IMAGES_REQUEST)
    }

    private fun handleCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            captureImageWithCamera()
        }
    }

    private fun captureImageWithCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    private fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImageWithCamera()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGES_REQUEST -> {
                    if (data?.clipData != null) {
                        val count = data.clipData!!.itemCount
                        for (i in 0 until count) {
                            imageUris.add(data.clipData!!.getItemAt(i).uri)
                        }
                    } else if (data?.data != null) {
                        imageUris.add(data.data!!)
                    }
                }
                CAMERA_REQUEST_CODE -> {
                    val photoBitmap = data?.extras?.get("data") as? Bitmap
                    photoBitmap?.let {
                        // Display the captured image in the ImageView
                        display.setImageBitmap(it)

                        // Optionally, convert the Bitmap to Uri and add it to the imageUris list
                        val tempUri = getImageUriFromBitmap(requireContext(), it)
                        imageUris.add(tempUri)
                    }
                }
            }
        }
    }


    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
