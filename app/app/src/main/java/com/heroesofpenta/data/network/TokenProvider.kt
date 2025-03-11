package com.heroesofpenta.data.network

import android.content.Context

class TokenProvider(private val context: Context) {
  fun hasToken(): Boolean {
    val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    return prefs.getString("server_token", null) != null
  }

  fun getToken(): String? {
    val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    return prefs.getString("server_token", null)
  }

  fun saveToken(token: String) {
    val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("server_token", token).apply()
  }

  fun refreshToken(): String? {
    // Synchronous network call to backend /api/refresh
    // For example (using Retrofit’s .execute()):
    val refreshResponse = RetrofitClient.instance.refreshToken().execute()
    return if (refreshResponse.isSuccessful) {
      val newToken = refreshResponse.body()?.token
      if (!newToken.isNullOrEmpty()) {
        saveToken(newToken)
      }
      newToken
    } else {
      null
    }
  }

  fun clearToken() {
    val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    prefs.edit().remove("server_token").apply()
  }
}
