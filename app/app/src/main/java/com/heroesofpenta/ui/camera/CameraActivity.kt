package com.heroesofpenta.ui.camera

import android.graphics.Bitmap
// import android.widget.Toast
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
//import coil.compose.rememberAsyncImagePainter
//import coil.compose.rememberImagePainter
//import coil.request.ImageRequest
import com.heroesofpenta.data.repository.MainRepository
import kotlinx.coroutines.launch
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import okhttp3.MultipartBody
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.RequestBody
//import okhttp3.RequestBody.Companion.toRequestBody
//import java.io.ByteArrayOutputStream
import android.Manifest
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
//import androidx.camera.core.impl.utils.MatrixExt.postRotate
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File


@OptIn(ExperimentalPermissionsApi::class, ExperimentalCoroutinesApi::class)
@Composable
fun CameraScreen(selectedHeroIds: String?, navController: NavController) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  // We'll store the captured bitmap in state
  var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var canShootSelfie by remember { mutableStateOf(false) }

  // State for showing a dialog (and controlling its content)
  var dialogMessage by remember { mutableStateOf<String?>(null) }
  var showLoading by remember { mutableStateOf(false) } // do we show the swirl?

  // Keep track of whether the button is enabled
  var isButtonEnabled by remember { mutableStateOf(true) }

  // Set up a launcher for taking a picture preview
  val takePictureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: Bitmap? ->
    capturedBitmap = bitmap
  }

  val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
  val requestPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      // Permission granted
      canShootSelfie = true
    } else {
      // Handle permission denial
      canShootSelfie = false
    }
  }

  LaunchedEffect(cameraPermissionState) {
    if (!cameraPermissionState.status.isGranted && cameraPermissionState.status.shouldShowRationale) {
      // Show rationale if needed
//      Text(
//        text = "You cannot play without a camera.",
//        modifier = Modifier.padding(all = 8.dp)
//      )
    } else {
      requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
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
    Button(
      enabled = canShootSelfie,
      onClick = {
        takePictureLauncher.launch(null)
      }
    ) {
      Text("Take Selfie")
    }

    Spacer(modifier = Modifier.height(16.dp))

    // If we have a bitmap, preview it and show an upload button
    capturedBitmap?.let { bitmap ->
      val rotatedBitmap = forcePortraitIfNeeded(bitmap)
      ImagePreview(bitmap = rotatedBitmap)
//      // Simple image preview using Coil’s AsyncImage or ImageBitmap conversion
//      // Here, we convert the Bitmap to a Coil ImageRequest
//      val imageRequest = ImageRequest.Builder(context)
//        .data(bitmap) // Pass the Bitmap directly
//        .build()
//
//      Image(
//        painter = rememberAsyncImagePainter(model = imageRequest),
//        contentDescription = "Selfie Preview",
//        modifier = Modifier
//          .fillMaxWidth()
//          .height(300.dp)
//      )

      Spacer(modifier = Modifier.height(16.dp))

      Button(onClick = {
        // 1) Show loading swirl, show dialog, disable button
        showLoading = true
        dialogMessage = "Uploading your selfie, please wait..."
        isButtonEnabled = false

        // 2) Launch a coroutine that attempts the upload
        coroutineScope.launch {
          // We'll use a 10-second timeout. If the server
          // doesn't respond, we show a timed-out message.
          val result = withTimeoutOrNull(10_000) {
            // Our repository callback-based method
            suspendCancellableCoroutine { cont ->
              MainRepository.uploadSelfie(bitmap, selectedHeroIds.orEmpty()) { error, success ->
                cont.resume(Pair(error, success)) {}
              }
            }
          }

          // 3) Hide the swirl
          showLoading = false

          // 4) Evaluate the result
          dialogMessage = if (result == null) {
            // Timed out
            "Server took too long. Please try again."
          } else {
            val (error, success) = result
            if (error != null) {
              "Failed to upload selfie:\n$error"
            } else if (success == null) {
              "Error: got neither success nor error."
            } else {
              "Congrats!\n${success.phrase}\nYou earned ${success.xp} XP!"
            }
          }

          // Re-enable the button
          isButtonEnabled = true
        }
      }) {
        Text("Upload Selfie")
      }
    }

    // Optional: show the swirling progress while uploading
    if (showLoading) {
      Spacer(modifier = Modifier.height(16.dp))
      CircularProgressIndicator()
    }
  }

  // Show a “popup” dialog if dialogMessage != null
  if (dialogMessage != null) {
    CustomPopupDialog(
      message = dialogMessage!!,
      onDismiss = {
        dialogMessage = null
        // If success, you might navigate here
        if (!showLoading && dialogMessage?.contains("Congrats!") == true) {
          navController.popBackStack()
        }
      }
    )
  }
}

/**
 * If the bitmap is in "landscape" orientation, rotate it 90°.
 * This forcibly displays it in portrait.
 */
fun forcePortraitIfNeeded(original: Bitmap): Bitmap {
  return if (original.width > original.height) {
    // Create a Matrix that will rotate the image by 90 degrees
    val matrix = Matrix().apply {
      postRotate(-90f)
    }
    // Use the matrix to create a new, correctly-oriented Bitmap
    Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
  } else {
    original
  }
}

fun rotateBitmapFromFile(original: Bitmap, file: File): Bitmap {
  val exif = ExifInterface(file.absolutePath)
  val orientation = exif.getAttributeInt(
    ExifInterface.TAG_ORIENTATION,
    ExifInterface.ORIENTATION_NORMAL
  )

  val rotationDegrees = when (orientation) {
    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
    else -> 0f
  }

  if (rotationDegrees != 0f) {
    val matrix = Matrix().apply { postRotate(rotationDegrees) }
    return Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
  } else {
    return original
  }
}

// Simple composable to preview the captured bitmap
@Composable
fun ImagePreview(bitmap: Bitmap) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(300.dp),
    contentAlignment = Alignment.Center
  ) {
    Image(
      bitmap = bitmap.asImageBitmap(),
      contentDescription = "Selfie Preview",
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Fit
    )
  }
}

@Composable
fun CustomPopupDialog(
  message: String,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = { onDismiss() }, // triggered if user taps outside or back button
    title = { Text("Information") },
    text = {
      Text(message)
    },
    confirmButton = {
      Button(onClick = { onDismiss() }) {
        Text("OK")
      }
    },
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp) // to ensure a bit of margin
  )
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
