package com.example.voyago.activities

import android.Manifest
import android.content.ContentValues
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
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.voyago.user1
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Locale


object ImageHolder {
    var selectedImage: ImageBitmap? = null
}

class GalleryActivity : AppCompatActivity() {

    /*private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                val imageBitmap = bitmap?.asImageBitmap()
                if (imageBitmap != null) {
                    ImageHolder.selectedImage = imageBitmap
                    setResult(RESULT_OK)
                } else {
                    setResult(RESULT_CANCELED)
                }
            } ?: run {
                setResult(RESULT_CANCELED)
            }

            finish()
        }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Non serve layout, lanciamo subito la galleria
        //pickImage.launch("image/*")
    }
}
