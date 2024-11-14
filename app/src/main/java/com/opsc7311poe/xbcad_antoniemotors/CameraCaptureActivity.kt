package com.opsc7311poe.xbcad_antoniemotors

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity

class CameraCaptureActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK, data) // Return captured image data
            } else {
                setResult(resultCode) // Return any error or cancellation code as is
            }
            finish() // Close CameraCaptureActivity immediately
        }
    }

    override fun onBackPressed() {
        // Ensure proper handling of back press to avoid camera re-invoking
        setResult(RESULT_CANCELED)
        super.onBackPressed()
        finish()
    }
}
