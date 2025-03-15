package com.heroesofpenta.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.heroesofpenta.MainActivity
import com.heroesofpenta.R
import com.heroesofpenta.data.repository.MainRepository
import android.widget.VideoView

class AuthActivity : AppCompatActivity() {

  private lateinit var loginButton: Button
  private lateinit var registerButton: Button
  private lateinit var demoButton: Button
  private lateinit var videoView: VideoView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_auth)

    loginButton = findViewById(R.id.btnLogin)
    registerButton = findViewById(R.id.btnRegister)
    demoButton = findViewById(R.id.btnDemo)
    videoView = findViewById(R.id.videoBackground)

    // Setup background video
    // Replace R.raw.background with your actual raw video resource.
    val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.background}")
    videoView.setVideoURI(videoUri)

    // Loop video when it's ready
    videoView.setOnPreparedListener { mp ->
      mp.isLooping = true
      videoView.start()
    }

    loginButton.setOnClickListener {
      // Start the TikTok OAuth flow. Could be a WebView or a custom tab approach.
      startTikTokAuthFlow()
    }

    registerButton.setOnClickListener {
      // Potentially the same or similar flow—depends on your server’s logic for “register”.
      startTikTokAuthFlow()
    }

    demoButton.setOnClickListener {
      // Skip TikTok. Hit the backend to create a dummy wallet. Then proceed to main UI.
      createDummyWalletAndProceed()
    }
  }

  private fun startTikTokAuthFlow() {
    // Construct your TikTok OAuth URL (with your client key and redirect_uri).
    // E.g.: "https://www.tiktok.com/auth/authorize?client_key=XXX&redirect_uri=YYY&response_type=code&scope=user.info.basic"

    //val tiktokAuthUrl = "https://www.tiktok.com/auth/authorize?...your_params..."

    // Launch a browser or WebView. In production, consider using something like Custom Tabs or the official TikTok SDK if available.
    //val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tiktokAuthUrl))
    val intent = Intent(this, TiktokAuthCallbackActivity::class.java)
    startActivity(intent)
    // finish()
    // The user is then redirected back to TiktokAuthCallbackActivity via your registered redirect URI scheme.
  }

  private fun createDummyWalletAndProceed() {
    MainRepository.createDummyUser()
    goToMainActivity()
  }

  private fun goToMainActivity() {
    val intent = Intent(this, MainActivity::class.java)
    startActivity(intent)
    finish()
  }
}
