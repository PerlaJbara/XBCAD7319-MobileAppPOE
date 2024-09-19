package com.opsc7311poe.xbcad_antoniemotors

import android.Manifest
import android.app.Activity
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

    private lateinit var spinCustomer: Spinner
    private lateinit var edtVehicleNoPlate: EditText
    private lateinit var edtVehicleModel: EditText
    private lateinit var edtVinNumber: EditText
    private lateinit var edtVehicleKms: EditText
    private lateinit var btnSubmitRegVehicle: Button
    private lateinit var imgCars: ImageView
    private lateinit var galleryCars: ImageView
    private lateinit var display: ImageView
    private var selectedCustomerId: String = ""
    private val imageUris = mutableListOf<Uri>() // To store image URIs
    private lateinit var btnBack: ImageView

    // Firebase references
    // Reference to the Customers node in your database
    val databaseRef = FirebaseDatabase.getInstance().getReference("Customers")
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
        spinCustomer = view.findViewById(R.id.customerspinner)
        edtVehicleNoPlate = view.findViewById(R.id.edttxtVehicleNoPlate)
        edtVehicleModel = view.findViewById(R.id.edttxtVehicleModel)
        edtVinNumber = view.findViewById(R.id.edttxtVinNumber)
        edtVehicleKms = view.findViewById(R.id.edttxtVehicleKms)
        btnSubmitRegVehicle = view.findViewById(R.id.btnSubmitRegVehicle)
        btnBack = view.findViewById(R.id.ivBackButton)
        //to display the images that the user captures or uploads
        display = view.findViewById(R.id.capturedImage)
        galleryCars = view.findViewById(R.id.imgAttachImage)
        imgCars= view.findViewById(R.id.imgCamera)

        // Populate Spinner with customer names
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { populateCustomerSpinner() } //it


        // Image upload click listeners
        galleryCars.setOnClickListener { selectImagesFromGallery() }

        //imgCars.setOnClickListener { captureImageWithCamera() }
        imgCars.setOnClickListener { handleCameraPermission() }

        // Submit vehicle registration
        btnSubmitRegVehicle.setOnClickListener {
            registerVehicle()
        }

        btnBack.setOnClickListener {
            replaceFragment(HomeFragment())
        }

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



        return view
    }



    private fun populateCustomerSpinner() {
        // Get the current logged-in user ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Query the database for customers directly under the current user's ID
        val customerRef = FirebaseDatabase.getInstance().getReference(userId).child("Customers")

        customerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val customerMap = mutableMapOf<String, String>()
                val customerNames = mutableListOf<String>()

                // Check if there are customers associated with the user
                if (!snapshot.exists()) {
                    Toast.makeText(requireContext(), "No customers found for this user", Toast.LENGTH_SHORT).show()
                    return
                }

                // Loop through all customers associated with this user
                for (customerSnapshot in snapshot.children) {
                    val customerId = customerSnapshot.key // Get customer ID
                    val firstName = customerSnapshot.child("customerName").getValue(String::class.java)
                    val lastName = customerSnapshot.child("customerSurname").getValue(String::class.java)

                    // Log data to check if it's being fetched correctly
                    Log.d("FirebaseData", "Customer: $firstName $lastName, ID: $customerId")

                    // Only add customer if names are not null or empty
                    if (!firstName.isNullOrEmpty() && !lastName.isNullOrEmpty() && customerId != null) {
                        val fullName = "$firstName $lastName"
                        customerMap[customerId] = fullName
                        customerNames.add(fullName) // Add full name to spinner options
                    }
                }

                // Check if the list is empty
                if (customerNames.isEmpty()) {
                    Toast.makeText(requireContext(), "No customers found", Toast.LENGTH_SHORT).show()
                    return
                }

                // Set up the spinner with the list of customer names
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, customerNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinCustomer.adapter = adapter

                // Handle customer selection from the spinner
                spinCustomer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedCustomerName = parent.getItemAtPosition(position).toString()
                        selectedCustomerId = customerMap.filterValues { it == selectedCustomerName }.keys.firstOrNull().orEmpty()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Handle case where nothing is selected if necessary
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error loading customer data: ${error.message}")
                Toast.makeText(requireContext(), "Error loading customer data", Toast.LENGTH_SHORT).show()
            }
        })
    }





    private fun registerVehicle() {
        val vehicleNoPlate = edtVehicleNoPlate.text.toString().trim()
        val vehicleModel = edtVehicleModel.text.toString().trim()
        val vinNumber = edtVinNumber.text.toString().trim()
        val vehicleKms = edtVehicleKms.text.toString().trim()

        val vehicleOwner = spinCustomer.selectedItem.toString() // Get the selected customer's name

        if (vehicleNoPlate.isEmpty() || vehicleModel.isEmpty() || vehicleKms.isEmpty()) {
            Toast.makeText(context, "Please fill in all mandatory fields", Toast.LENGTH_SHORT).show()
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

        // Constructing the VehicleData object
        val vehicle = VehicleData(
            VehicleOwner = vehicleOwner, // Customer's full name selected from spinner
            customerID = selectedCustomerId, // Customer ID from spinner
            VehicleNumPlate = vehicleNoPlate,
            VehicleModel = vehicleModel,
            VinNumber = if (vinNumber.isEmpty()) "N/A" else vinNumber,
            VehicleKms = vehicleKms
        )

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            // Save the vehicle directly under the userId -> Vehicles path
            val vehicleRef = FirebaseDatabase.getInstance().getReference(userId)  // Use the userId as the parent
                .child("Vehicles")
                .push()  // Create a unique ID for the vehicle

            vehicleRef.setValue(vehicle).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    uploadVehicleImages(vehicleRef.key!!)
                    Toast.makeText(context, "Vehicle registered successfully", Toast.LENGTH_SHORT).show()
                    // Clear the fields after successful registration
                    edtVehicleNoPlate.text.clear()
                    edtVehicleModel.text.clear()
                    edtVinNumber.text.clear()
                    edtVehicleKms.text.clear()
                } else {
                    Toast.makeText(context, "Failed to register vehicle", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }





    private fun uploadVehicleImages(vehicleId: String) {
        if (imageUris.isNotEmpty()) {
            for (uri in imageUris) {
                val imageRef = storage.child(vehicleId).child(UUID.randomUUID().toString())

                imageRef.putFile(uri).addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { url ->
                        // Save the download URL to the vehicle node in the database
                        FirebaseDatabase.getInstance().getReference("Vehicles")
                            .child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .child(vehicleId)  // Corrected path here: vehicleId refers to the vehicle
                            .child("images")
                            .push().setValue(url.toString())
                    }
                }.addOnFailureListener {
                    Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
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
