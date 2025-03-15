//package com.heroesofpenta.data.tiktok.network
//
//import okhttp3.OkHttpClient
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import java.util.concurrent.TimeUnit
//
//object TikTokRetrofitClient {
//  private lateinit var baseUrl: String
//
//  fun <T> createApi(apiClass: Class<T>): T {
//    baseUrl = "https://open.tiktokapis.com"
//
//    val retrofitBuilder = Retrofit.Builder()
//
//    val okHttpClient = OkHttpClient().newBuilder()
//      .connectTimeout(60, TimeUnit.SECONDS)
//      .readTimeout(60, TimeUnit.SECONDS)
//      .writeTimeout(60, TimeUnit.SECONDS)
//      .build()
//    retrofitBuilder.baseUrl(baseUrl)
//    val retrofit = retrofitBuilder
//      .addConverterFactory(GsonConverterFactory.create())
//      .client(okHttpClient)
//      .build()
//    return retrofit.create(apiClass)
//  }
//}