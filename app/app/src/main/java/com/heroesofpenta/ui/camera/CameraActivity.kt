package com.heroesofpenta.ui.camera

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.heroesofpenta.data.repository.MainRepository
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

@Composable
fun CameraScreen(selectedHeroIds: String?, navController: NavController) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  // We'll store the captured bitmap in state
  var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

  // Set up a launcher for taking a picture preview
  val takePictureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: Bitmap? ->
    capturedBitmap = bitmap
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

    Text(
      text = "Selected hero IDs: $selectedHeroIds",
      modifier = Modifier.padding(bottom = 16.dp)
    )

    // Button to open the camera
    Button(onClick = {
      takePictureLauncher.launch(null)
    }) {
      Text("Take Selfie")
    }

    Spacer(modifier = Modifier.height(16.dp))

    // If we have a bitmap, preview it and show an upload button
    capturedBitmap?.let { bitmap ->
      // Simple image preview using Coil’s AsyncImage or ImageBitmap conversion
      // Here, we convert the Bitmap to a Coil ImageRequest
      val imageRequest = ImageRequest.Builder(context)
        .data(bitmap) // Pass the Bitmap directly
        .build()

      Image(
        painter = rememberAsyncImagePainter(model = imageRequest),
        contentDescription = "Selfie Preview",
        modifier = Modifier
          .fillMaxWidth()
          .height(300.dp)
      )

      Spacer(modifier = Modifier.height(16.dp))

      Button(onClick = {
        // Upload the captured selfie in a coroutine
        coroutineScope.launch {
          MainRepository.uploadSelfie(
            bitmap,
            selectedHeroIds.orEmpty()
          ) { success ->
            if (success) {
              Toast.makeText(context, "Selfie uploaded successfully!", Toast.LENGTH_SHORT).show()
              // Navigate back or show a success screen
              navController.popBackStack()
            } else {
              Toast.makeText(context, "Failed to upload selfie.", Toast.LENGTH_SHORT).show()
            }
          }
        }
      }) {
        Text("Upload Selfie")
      }
    }
  }
}



//import android.net.Uri
//import android.os.Bundle
//import android.widget.Button
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.camera.view.PreviewView
//import com.heroesofpenta.R
//import com.heroesofpenta.data.repository.MainRepository
//
//class CameraActivity : AppCompatActivity() {
//
//    private lateinit var captureButton: Button
//    private lateinit var cameraPreviewView: PreviewView
//    private var selectedHeroIds: Array<String>? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_camera)
//
//        captureButton = findViewById(R.id.btnCapture)
//        cameraPreviewView = findViewById(R.id.previewView)
//        selectedHeroIds = intent.getStringArrayExtra("selectedHeroIds")
//
//        // TODO: Setup camera (CameraX)
//        startCamera()
//
//        captureButton.setOnClickListener {
//            takePhoto()
//        }
//    }
//
//    private fun startCamera() {
//        // Implement CameraX logic: bind Preview + ImageCapture use cases
//    }
//
//    private fun takePhoto() {
//        // Use CameraX’s ImageCapture to capture an image
//        // Save locally or store in memory.
//        // For example:
//        /*
//        val photoFile = ...
//        imageCapture.takePicture(
//            executor,
//            object : ImageCapture.OnImageSavedCallback {
//                override fun onImageSaved(outputFileResults: OutputFileResults) {
//                    val savedUri = outputFileResults.savedUri
//                    postSelfieToBackend(savedUri)
//                }
//                override fun onError(exception: ImageCaptureException) { ... }
//            }
//        )
//        */
//    }
//
//    private fun postSelfieToBackend(uri: Uri) {
//        // 1. Convert the image to file or base64
//        // 2. Send to backend => backend calls TikTok Content API with the user’s token
//        MainRepository.postSelfieToTikTok(selectedHeroIds, uri) { success ->
//            if (success) {
//                Toast.makeText(this, "Training posted!", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this, "Training failed!", Toast.LENGTH_SHORT).show()
//            }
//            finish()
//        }
//    }
//}
