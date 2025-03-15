//package com.heroesofpenta.data.tiktok
//
//import android.app.Activity
//import com.tiktok.open.sdk.auth.AuthApi
//import com.tiktok.open.sdk.auth.AuthRequest
//import com.heroesofpenta.BuildConfig
//
//object TikTokAdapter {
//  fun request(activity: Activity) {
//    // STEP 1: Create an instance of AuthApi
//    val authApi = AuthApi(
//      activity = activity
//    )
//
//// STEP 2: Create an AuthRequest and set parameters
//    val request = AuthRequest(
//      clientKey = BuildConfig.tikTokClientId,
//      scope = "user.info.basic",
//      redirectUri = "https://heroesofpenta.com/auth/login/tiktok",
//      codeVerifier = codeVerifier
//    )
//
//// STEP 3: Invoke the authorize method
//    authApi.authorize(
//      request = request,
//      authMethod = AuthApi.AuthMethod.TikTokApp / AuthApi.AuthMethod.ChromeTab
//    );
//  }
//}