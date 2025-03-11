package com.heroesofpenta.ui.camera

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import com.heroesofpenta.R
import com.heroesofpenta.data.repository.MainRepository

class CameraActivity : AppCompatActivity() {

    private lateinit var captureButton: Button
    private lateinit var cameraPreviewView: PreviewView
    private var selectedHeroIds: Array<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        captureButton = findViewById(R.id.btnCapture)
        cameraPreviewView = findViewById(R.id.previewView)
        selectedHeroIds = intent.getStringArrayExtra("selectedHeroIds")

        // TODO: Setup camera (CameraX)
        startCamera()

        captureButton.setOnClickListener {
            takePhoto()
        }
    }

    private fun startCamera() {
        // Implement CameraX logic: bind Preview + ImageCapture use cases
    }

    private fun takePhoto() {
        // Use CameraX’s ImageCapture to capture an image
        // Save locally or store in memory.
        // For example:
        /*
        val photoFile = ...
        imageCapture.takePicture(
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: OutputFileResults) {
                    val savedUri = outputFileResults.savedUri
                    postSelfieToBackend(savedUri)
                }
                override fun onError(exception: ImageCaptureException) { ... }
            }
        )
        */
    }

    private fun postSelfieToBackend(uri: Uri) {
        // 1. Convert the image to file or base64
        // 2. Send to backend => backend calls TikTok Content API with the user’s token
        MainRepository.postSelfieToTikTok(selectedHeroIds, uri) { success ->
            if (success) {
                Toast.makeText(this, "Training posted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Training failed!", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}
