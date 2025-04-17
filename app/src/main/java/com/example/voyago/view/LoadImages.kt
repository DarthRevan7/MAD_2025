package com.example.voyago.view


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.voyago.activities.BottomBar
import com.example.voyago.activities.TopBar
import java.io.File

//import com.example.voyago.model.romeTrip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadImages() {

    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(false)
        }
    ) { innerPadding ->
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            item{
                Spacer(modifier = Modifier.height(3.dp))
            }

            item {
                Hero()
            }

            item{
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

var hasPermission = false

@SuppressLint("DiscouragedApi")
@Composable
fun Hero() {

    RequestStoragePermission { hasPermission = true }

    Log.d("PERMISSION:","Has Permission T: ${hasPermission}")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {

        //Load Images from emulator gallery
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "barcelona.jpg"
        )

        val uri = Uri.fromFile(file)

        val context = LocalContext.current
        val imageLoader = ImageLoader.Builder(context)
            .crossfade(true)
            .build()
        //val uri = "content:/Pictures/CameraX-Image/barcelona.jpg".toUri()
        AsyncImage(file,
            null,
            modifier = Modifier.fillMaxSize(),
            imageLoader = imageLoader

        )

        Log.d("URI-CHECK", "Uri usato per l'immagine: $uri")

        Log.d("URI-CHECK", "Percorso assoluto: ${file.absolutePath}")
        Log.d("URI-CHECK", "Esiste il file? ${file.exists()}")


        val permission = Manifest.permission.READ_EXTERNAL_STORAGE

        val hasPermission = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

        Log.d("PERMISSION", "READ_EXTERNAL_STORAGE granted? $hasPermission")


        //Load Images from drawable folder
        val drawableId = remember("paris") {
            context.resources.getIdentifier("paris", "drawable", context.packageName)
        }

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(drawableId)
                .crossfade(true)
                .build(),
            contentDescription = "paris",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp).offset(y = 300.dp)
        )


        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(vertical = 30.dp, horizontal = 10.dp)
                .background(
                    color = Color(0xAA444444), // semi-transparent dark grey
                    shape = MaterialTheme.shapes.small
                )

        ) {
        }
    }
}

@Composable
fun RequestStoragePermission(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("PERM", "Permesso concesso.")
            onPermissionGranted()
        } else {
            Log.e("PERM", "Permesso NEGATO.")
        }
    }

    LaunchedEffect(Unit) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(permission)
        } else {
            onPermissionGranted()
        }
    }
}

