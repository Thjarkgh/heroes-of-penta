package com.heroesofpenta.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.heroesofpenta.BuildConfig
import com.heroesofpenta.MainActivity
import com.heroesofpenta.R
import com.heroesofpenta.data.repository.MainRepository
import com.tiktok.open.sdk.auth.AuthApi
import com.tiktok.open.sdk.auth.AuthRequest
import com.tiktok.open.sdk.auth.utils.PKCEUtils

class TiktokAuthCallbackActivity : AppCompatActivity() {
  private val authApi: AuthApi = AuthApi(this)
  //    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val data: Uri? = intent?.data
//
//        if (data != null && data.toString().startsWith("https://h")) {
//            val code = data.getQueryParameter("code")
//            // Send `code` to backend to exchange for a user access token or session.
//            if (code != null) {
//                exchangeCodeForToken(code)
//            }
//        } else {
//            // Handle error or no code found
//            finish()
//        }
//    }
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    //handleIntent(intent)

    if (intent.action == Intent.ACTION_VIEW &&
      intent.data?.lastPathSegment == "tiktok" &&
      intent.data?.getQueryParameter("code") != null) {
      handleIntent(intent)
    } else {
      val prefs = getSharedPreferences(KEY_CODE_VERIFIER, Context.MODE_PRIVATE)
      val editor = prefs.edit()
      editor.putString(KEY_CODE_VERIFIER, PKCEUtils.generateCodeVerifier())
      editor.apply()

      val codeVerifier = prefs.getString(KEY_CODE_VERIFIER, "").toString()
      authorize(codeVerifier)
    }
  }

  private fun authorize(codeVerifier: String) {
//    val currentStateValue: MainViewModelViewState = _viewState.value ?: getDefaultViewState()
//    val currentScopeStates = currentStateValue.scopeStates
//    val browserAuthEnabled = currentStateValue.browserAuthEnabled
//    val autoAuthDisabled = currentStateValue.autoAuthDisabled
//    val enabledScopes: MutableList<String> = mutableListOf()
//    currentScopeStates.forEach {
//      if (it.value.isOn) {
//        enabledScopes.add(it.key.value)
//      }
//    }
//
//    if (enabledScopes.size == 0) {
//      sendViewEffect(
//        ViewEffect.ShowGeneralAlert(
//          R.string.invalid_scope,
//          R.string.invalid_scope_description
//        )
//      )
//      return
//    }
    val request = AuthRequest(
      clientKey = BuildConfig.tikTokClientId,
      scope = "user.info.basic,video.upload",
      redirectUri = BuildConfig.tikTokRedirectUrl,
      codeVerifier = codeVerifier,
      // autoAuthDisabled = autoAuthDisabled,
    )
//    val authType = if (browserAuthEnabled) {
//      AuthApi.AuthMethod.ChromeTab
//    } else {
//      AuthApi.AuthMethod.TikTokApp
//    }
    authApi.authorize(request, AuthApi.AuthMethod.ChromeTab)
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    handleIntent(intent)
  }

//  private fun handleIntent(intent: Intent) {
//    val appLinkAction = intent.action
//    val appLinkData: Uri? = intent.data
//    // not sure whether to filter this like in the example, leaving filter out for now
//
////    if (Intent.ACTION_VIEW == appLinkAction) {
////      appLinkData?.lastPathSegment?.also { recipeId ->
////        Uri.parse("content://com.recipe_app/recipe/")
////          .buildUpon()
////          .appendPath(recipeId)
////          .build().also { appData ->
////            showRecipe(appData)
////          }
////      }
////    }
//  }

  private fun handleIntent(intent: Intent) {
    val prefs = getSharedPreferences(KEY_CODE_VERIFIER, Context.MODE_PRIVATE)
    val codeVerifier = prefs.getString(KEY_CODE_VERIFIER, "").toString()
    authApi.getAuthResponseFromIntent(intent, BuildConfig.tikTokRedirectUrl)?.let {
      val authCode = it.authCode
      if (authCode.isNotEmpty()) {
        // TODO: Verify all necessary permissions were granted!
        // viewModel.updateGrantedScope(it.grantedPermissions)
        // viewModel.getUserBasicInfo(authCode, it.grantedPermissions, codeVerifier)
        exchangeCodeForToken(codeVerifier, authCode)
      } else if (it.errorCode != 0) {
        val description = if (it.errorMsg != null) {
          getString(
            R.string.error_code_with_message,
            it.errorCode,
            it.errorMsg
          )
        } else {
          if (it.authErrorDescription != null) {
            getString(
              R.string.error_code_with_error_description,
              it.authError,
              it.authErrorDescription
            )
          } else {
            getString(
              R.string.error_code_with_error,
              it.errorCode,
              it.authError,
            )
          }
        }
        showAlert(
          getString(R.string.error_dialog_title),
          description
        )
      }
    }
  }
  private fun exchangeCodeForToken(codeVerifier: String, code: String) {
    // Make a network call to your backend, e.g. POST /api/tiktok/login
    // with the code in the request body or query params.
    // The backend calls TikTok’s server to exchange for an access token.

    // onSuccess:
    // 1. Save token locally (SharedPreferences, etc.)
    // 2. Start MainActivity
    MainRepository.exchangeTikTokCode(codeVerifier, code) { success ->
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

  private fun showAlert(title: String, desc: String) {
    AlertDialog
      .Builder(this)
      .setTitle(title)
      .setMessage(desc)
      .setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.cancel() }
      .create()
      .show()
  }

  companion object {
    private const val KEY_CODE_VERIFIER = "key_code_verifier"
  }
}
