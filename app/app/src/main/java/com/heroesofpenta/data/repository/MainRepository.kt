package com.heroesofpenta.data.repository

import android.graphics.Bitmap
//import android.net.Uri
import com.heroesofpenta.BuildConfig
import com.heroesofpenta.data.models.DeleteResponse
import com.heroesofpenta.data.models.NftHero
//import com.heroesofpenta.data.models.NonceResponse
import com.heroesofpenta.data.models.User
//import com.heroesofpenta.data.models.WalletResponse
import com.heroesofpenta.data.network.BasicResponse
import com.heroesofpenta.data.network.LoginResponse
import com.heroesofpenta.data.network.RetrofitClient
import com.heroesofpenta.data.network.TikTokLoginRequest
import com.heroesofpenta.data.network.TrainingResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
//import okhttp3.OkHttpClient
//import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

object MainRepository {
  private var user: User? = null

  // region TikTok / Auth

  fun exchangeTikTokCode(codeVerifier: String, code: String, callback: (Boolean) -> Unit) {
    // Suppose your endpoint is POST /auth/tiktok-login
    val request = TikTokLoginRequest(codeVerifier, code)
    RetrofitClient.instance.tiktokLogin(request).enqueue(object : Callback<LoginResponse> {
      override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
        if (response.isSuccessful) {
          response.body()?.let { loginResp ->
            // Save the server session token
            RetrofitClient.tokenProvider().saveToken(loginResp.accessToken)
            //saveUserToken(loginResp.token)
            callback(true)
            return
          }
        }
        callback(false)
      }

      override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
        callback(false)
      }
    })
  }

  // endregion

  // region User Info
fun createDummyUser() {
  RetrofitClient.tokenProvider().saveToken(BuildConfig.dummyUserToken)
  user = User(BuildConfig.dummyUserId.toUInt(), "demo")
}
  /**
   * 1. Fetch current user data from the server.
   */
  fun getUser(callback: (User?) -> Unit, force: Boolean = false) {
//        val token = getStoredToken() ?: run {
//            callback(null)
//            return
//        }
    if (!RetrofitClient.tokenProvider().hasToken()) {
      callback(null)
    } else {
      if (user != null && !force) {
        callback(user)
      } else {
        RetrofitClient.instance.getUser() //"Bearer $token")
          .enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
              if (response.isSuccessful) {
                callback(response.body())
              } else {
                callback(null)
              }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
              callback(null)
            }
          })
      }
    }
  }

  fun updateUserName(name: String, callback: (Boolean) -> Unit) {
//        val token = getStoredToken()
    val body = mapOf("name" to name)
    RetrofitClient.instance.updateUserName(body)//"Bearer $token", body)
      .enqueue(object : Callback<BasicResponse> {
        override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
          if (response.isSuccessful) {
            getUser(
              { u ->
                if (u != null) {
                  user = User(u.id, name, null, u.maxTrainees)
                  callback(true)
                } else {
                  callback(false)
                }
              },
              false
            )
          } else {
            callback(false)
          }
        }

        override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
          callback(false)
        }
      })
  }

  // endregion

  // region Wallet

//  /**
//   * 2. Request a nonce for wallet signing
//   */
//  fun getNonceForWallet(callback: (String?) -> Unit) {
////        val token = getStoredToken() ?: run {
////            callback(null)
////            return
////        }
//    RetrofitClient.instance.getNonceForWallet()//"Bearer $token")
//      .enqueue(object : Callback<NonceResponse> {
//        override fun onResponse(call: Call<NonceResponse>, response: Response<NonceResponse>) {
//          if (response.isSuccessful) {
//            callback(response.body()?.nonce)
//          } else {
//            callback(null)
//          }
//        }
//
//        override fun onFailure(call: Call<NonceResponse>, t: Throwable) {
//          callback(null)
//        }
//      })
//  }

  /**
   * 3. Register wallet with signature
   * Suppose the server expects JSON with { "signature": "...", "walletAddress": "0x..." }
   */
  fun registerWallet(/*signature: String, */walletAddress: String, callback: (Boolean) -> Unit) {
//        val token = getStoredToken() ?: run {
//            callback(false)
//            return
//        }

    // we cannot get the signed data => workaround: store on blockchain => server needs to fetch from there
    // just update local cache
    getUser(
      { u ->
        if (u != null) {
          user = User(u.id, u.name, null, u.maxTrainees)
          callback(true)
        } else {
          callback(false)
        }
      },
      false
    )
//    val body = mapOf(
//      "signature" to signature,
//      "walletAddress" to walletAddress
//    )
//
//    RetrofitClient.instance.registerWallet(body)//"Bearer $token", body)
//      .enqueue(object : Callback<WalletResponse> {
//        override fun onResponse(call: Call<WalletResponse>, response: Response<WalletResponse>) {
//          if (response.isSuccessful) {
//            // If needed, parse server's response to check success status
//            val walletResp = response.body()
//            callback(walletResp?.success == true)
//          } else {
//            callback(false)
//          }
//        }
//
//        override fun onFailure(call: Call<WalletResponse>, t: Throwable) {
//          callback(false)
//        }
//      })
  }

  /**
   * 4. Disconnect wallet
   */
  fun disconnectWallet(callback: (Boolean) -> Unit) {
//        val token = getStoredToken() ?: run {
//            callback(false)
//            return
//        }
    getUser(
      { u ->
        if (u != null) {
          user = User(u.id, u.name, null, u.maxTrainees)
          callback(true)
        } else {
          callback(false)
        }
      },
      false
    )
//    RetrofitClient.instance.disconnectWallet() //"Bearer $token")
//      .enqueue(object : Callback<WalletResponse> {
//        override fun onResponse(call: Call<WalletResponse>, response: Response<WalletResponse>) {
//          if (response.isSuccessful) {
//            val walletResp = response.body()
//            callback(walletResp?.success == true)
//          } else {
//            callback(false)
//          }
//        }
//
//        override fun onFailure(call: Call<WalletResponse>, t: Throwable) {
//          callback(false)
//        }
//      })
  }

  // endregion


  // region Account

  /**
   * 5. Delete account
   */
  fun deleteAccount(callback: (Boolean) -> Unit) {
//        val token = getStoredToken() ?: run {
//            callback(false)
//            return
//        }
    RetrofitClient.instance.deleteAccount()//"Bearer $token")
      .enqueue(object : Callback<DeleteResponse> {
        override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
          if (response.isSuccessful) {
            val delResp = response.body()
            if (delResp?.success == true) {
              // Clear local token if you want to log out the user
              RetrofitClient.tokenProvider().clearToken()
              //clearLocalToken()
              callback(true)
            } else {
              callback(false)
            }
          } else {
            callback(false)
          }
        }

        override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
          callback(false)
        }
      })
  }

  // endregion

//    // region Token Management
//
//    /**
//     * 6. Retrieve the stored server session token (e.g., from SharedPreferences)
//     */
//    private fun getStoredToken(): String? {
//        // Example approach if you’re not using a TokenProvider or interceptors:
//        val prefs = MyApp.context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
//        return prefs.getString("server_token", null)
//    }
//
//    /**
//     * Save a new token after login, refresh, etc.
//     */
//    fun saveUserToken(token: String) {
//        val prefs = MyApp.context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
//        prefs.edit().putString("server_token", token).apply()
//    }
//
//    /**
//     * Clear local token after logout or account deletion
//     */
//    fun clearLocalToken() {
//        val prefs = MyApp.context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
//        prefs.edit().remove("server_token").apply()
//    }
//
//    // endregion

  fun getNftHeroes(callback: (List<NftHero>) -> Unit) {
//        val token = getStoredToken()
    RetrofitClient.instance.getNftHeroes()//"Bearer $token")
      .enqueue(object : Callback<List<NftHero>> {
        override fun onResponse(
          call: Call<List<NftHero>>,
          response: Response<List<NftHero>>
        ) {
          callback(response.body() ?: emptyList())
        }

        override fun onFailure(call: Call<List<NftHero>>, t: Throwable) {
          callback(emptyList())
        }
      })
  }

  fun getNftHeroById(id: String, callback: (NftHero?) -> Unit) {
//        val token = getStoredToken()
    RetrofitClient.instance.getNftHeroById(id)//"Bearer $token", id)
      .enqueue(object : Callback<NftHero> {
        override fun onResponse(call: Call<NftHero>, response: Response<NftHero>) {
          callback(response.body())
        }

        override fun onFailure(call: Call<NftHero>, t: Throwable) {
          callback(null)
        }
      })
  }

//  fun postSelfieToTikTok(heroIds: Array<String>?, uri: Uri, callback: (Boolean) -> Unit) {
//    // 1. Convert the selfie to multipart
//    // 2. POST /api/training
//    // 3. On success => callback(true)
//  }

  fun checkTrainingCooldown(callback: (Boolean) -> Unit) {
    // e.g. GET /api/training/status
    // callback(true) if can train, false if on cooldown
    // TODO: Implement for production => fetch from server
    callback(true)
  }

//    private fun getStoredToken(): String {
//        // read from SharedPreferences or similar
//        return "..."
//    }

  /**
   * Uploads the given [bitmap] to the server using a multipart POST request.
   * The server endpoint is "http://heroesofpenta.com/api/training/selfie".
   *
   * @param bitmap the user's captured selfie
   * @param heroIds optional, comma-separated string of hero IDs
   * @return true if the upload was successful, false otherwise
   */
  fun uploadSelfie(
    bitmap: Bitmap,
    heroIds: String,
    onResult: (Throwable?, TrainingResponse?) -> Unit
  ) {
    // 1) Convert the Bitmap to a JPEG byte array
    val byteStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteStream)
    val imageBytes = byteStream.toByteArray()

    // 2) Build a multipart form request body
    val imageRequestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
    val multipartBody = MultipartBody.Part.createFormData(
        name = "selfie",
        filename = "selfie.jpg",
        body = imageRequestBody
      )

    // 3) Create a RequestBody for the selectedHeroIds
    val idsRequestBody = heroIds
      .toRequestBody("text/plain".toMediaTypeOrNull())

    // 4) Execute the Retrofit call
    val call = RetrofitClient.instance.uploadSelfie(multipartBody, idsRequestBody)
    call.enqueue(object : Callback<TrainingResponse> {
      override fun onResponse(
        call: Call<TrainingResponse>,
        response: Response<TrainingResponse>
      ) {
        if (response.isSuccessful) {
          onResult(null, response.body() ?: TrainingResponse(0, "Better luck next time!"))
        } else {
          val exception = response.errorBody()?.toString() ?: "Empty Training Error"
          onResult(RuntimeException(exception), null)
        }
      }

      override fun onFailure(call: Call<TrainingResponse>, t: Throwable) {
        onResult(t, null)
      }
    })
  }
}
