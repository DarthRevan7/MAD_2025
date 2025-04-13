package com.example.voyago.activities

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.core.content.ContextCompat
import com.example.voyago.databinding.ActivityCameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.app.ActivityCompat
import com.example.voyago.user1
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Locale

class GalleryActivity : AppCompatActivity() {

    // Request code for permissions
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if permissions are granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If permissions are not granted, request them
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // Permissions already granted, proceed to open photo picker
            startPhotoPicker()
        }
    }
        // Handle the result of permission request
        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)

            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed to open the photo picker
                    startPhotoPicker()
                } else {
                    // Permission denied, show a message
                    Toast.makeText(
                        this,
                        "Permission denied. Cannot access the gallery.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    private fun startPhotoPicker() {
        val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
            // Callback invoked after the user selects a media item or closes the photo picker
            if (uri != null) {
                user1.profileImage = uri
                Log.d("PhotoPicker", "Selected URI: $uri")
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
            finish() // Finish the activity after the selection
        }

        pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
    }
}
