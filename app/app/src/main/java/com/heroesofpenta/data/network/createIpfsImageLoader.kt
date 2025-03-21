package com.heroesofpenta.data.network

import android.content.Context
import coil.ImageLoader
//import coil.request.ImageRequest
//import coil.compose.AsyncImage
import okhttp3.OkHttpClient
//import okhttp3.Interceptor
//import okhttp3.Response

fun createIpfsImageLoader(context: Context): ImageLoader {
  // 1) Build a custom OkHttp client
  val okHttpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
      // 2) Modify the request to add a custom User-Agent
      val newRequest = chain.request()
        .newBuilder()
        .header("Host", "ipfs.io")           // replicate cURL
        .header("User-Agent", "curl/8.5.0")  // replicate cURL
        .header("Accept", "*/*")            // replicate cURL
        .build()
      chain.proceed(newRequest)
    }
    .build()

  // 3) Create a Coil ImageLoader with your custom OkHttp client
  return ImageLoader.Builder(context)
    .okHttpClient(okHttpClient)
    .build()
}
