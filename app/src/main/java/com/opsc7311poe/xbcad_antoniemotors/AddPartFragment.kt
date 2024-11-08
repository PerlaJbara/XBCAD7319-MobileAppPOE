package com.opsc7311poe.xbcad_antoniemotors

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.widget.ImageView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
class AddPartFragment : Fragment() {

    private lateinit var partNameEditText: EditText
    private lateinit var partDescriptionEditText: EditText
    private lateinit var imgCamera: ImageView  // ImageView used as camera button
    private lateinit var capturedImage: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var stockCountPicker: NumberPicker
    private lateinit var minStockPicker: NumberPicker
    private lateinit var costPriceEditText: EditText
    private lateinit var addPartButton: Button
    private lateinit var database: DatabaseReference
    private val auth = FirebaseAuth.getInstance()

    private val cameraRequestCode = 1001  // Request code for camera intent
    private var imageUri: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_part, container, false)

        // Initialize UI elements
        partNameEditText = view.findViewById(R.id.edtPartName)
        partDescriptionEditText = view.findViewById(R.id.edtPartDescription)
        stockCountPicker = view.findViewById(R.id.npStockCounter)
        minStockPicker = view.findViewById(R.id.npMinStockCounter)
        costPriceEditText = view.findViewById(R.id.edttxtVinNumber)
        addPartButton = view.findViewById(R.id.btnAddPart)
        btnBack = view.findViewById(R.id.ivBackButton)
        imgCamera = view.findViewById(R.id.imgCamera)
        capturedImage = view.findViewById(R.id.capturedImage)


        // Set picker values
        stockCountPicker.minValue = 0
        stockCountPicker.maxValue = 100
        minStockPicker.minValue = 0
        minStockPicker.maxValue = 100

        // Set database reference
        database = Firebase.database.reference

        // Set button click listener
        addPartButton.setOnClickListener {
            savePart()
        }

        btnBack.setOnClickListener {
            replaceFragment(ViewInventoryFragment())
        }
        imgCamera.setOnClickListener {
            openCamera()
        }


        return view
    }

    private fun openCamera() {
        // Create an intent to open the camera app
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, cameraRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == cameraRequestCode && resultCode == Activity.RESULT_OK) {
            // Get the image as a Bitmap and display it
            val photo: Bitmap? = data?.extras?.get("data") as Bitmap
            capturedImage.setImageBitmap(photo)

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve arguments and populate fields if data is available
        arguments?.let { bundle ->
            partNameEditText.setText(bundle.getString("partName", ""))
            partDescriptionEditText.setText(bundle.getString("partDescription", ""))
            stockCountPicker.value = bundle.getInt("stockCount", 0)
            minStockPicker.value = bundle.getInt("minStock", 0)
            costPriceEditText.setText(bundle.getDouble("costPrice", 0.0).toString())
        }
    }

    private fun savePart() {
        val partName = partNameEditText.text.toString().trim()
        val partDescription = partDescriptionEditText.text.toString().trim()
        val stockCount = stockCountPicker.value
        val minStock = minStockPicker.value
        val costPrice = costPriceEditText.text.toString().trim().toDoubleOrNull()

        if (partName.isEmpty() || costPrice == null) {
            Toast.makeText(context, "Please enter a valid part name and cost price", Toast.LENGTH_SHORT).show()
            return
        }

        // Get current user ID
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = currentUser.uid

        // Create part data to save
        val partData = mapOf(
            "partName" to partName,
            "partDescription" to partDescription,
            "stockCount" to stockCount,
            "minStock" to minStock,
            "costPrice" to costPrice
        )

        // Save under the user's ID in the database
        database.child("Users").child(userId).child("parts").push()
            .setValue(partData)
            .addOnSuccessListener {
                Toast.makeText(context, "Part added successfully", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to add part: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        partNameEditText.text.clear()
        partDescriptionEditText.text.clear()
        costPriceEditText.text.clear()
        stockCountPicker.value = 0
        minStockPicker.value = 0
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}