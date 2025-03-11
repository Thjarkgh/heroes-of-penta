package com.heroesofpenta.ui.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.heroesofpenta.MainActivity
import com.heroesofpenta.data.repository.MainRepository

class TiktokAuthCallbackActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data: Uri? = intent?.data

        if (data != null && data.toString().startsWith("myapp://auth")) {
            val code = data.getQueryParameter("code")
            // Send `code` to backend to exchange for a user access token or session.
            if (code != null) {
                exchangeCodeForToken(code)
            }
        } else {
            // Handle error or no code found
            finish()
        }
    }

    private fun exchangeCodeForToken(code: String) {
        // Make a network call to your backend, e.g. POST /api/tiktok/login
        // with the code in the request body or query params.
        // The backend calls TikTok’s server to exchange for an access token.

        // onSuccess:
        // 1. Save token locally (SharedPreferences, etc.)
        // 2. Start MainActivity
        MainRepository.exchangeTikTokCode(code) { success ->
          if (success) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
          } else {
            Toast.makeText(this, "Failed to exchange TikTok code for token", Toast.LENGTH_SHORT).show()
            finish()
          }

//            if (success && token != null) {
//                // Save token
//                saveUserToken(token)
//                // Go to main
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
//                finish()
//            } else {
//                // Handle error
//                finish()
//            }
        }
    }

    private fun saveUserToken(token: String) {
        // Save to preferences
        val sharedPrefs = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("user_token", token).apply()
    }
}
