package com.heroesofpenta.data.network

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var _tokenProvider: TokenProvider? = null

//    fun tokenProvider() {
//        if (tokenProvider == null) {
//            throw error("")
//        }
//    }

    fun tokenProvider(): TokenProvider {
        if (_tokenProvider == null) {
            throw IllegalStateException("RetrofitClient not initialized")
        }
        return _tokenProvider as TokenProvider
    }

    fun init(context: Context) {
        // Initialize our TokenProvider
        _tokenProvider = TokenProvider(context.applicationContext)
    }

    private val authInterceptor: AuthInterceptor by lazy {
        AuthInterceptor(tokenProvider())
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://heroesofpenta.com/app-api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}


//object RetrofitClient {
//
//    lateinit var tokenProvider: TokenProvider
//
////    fun tokenProvider() {
////        if (tokenProvider == null) {
////            throw error("")
////        }
////    }
//
//    fun init(context: Context) {
//        // Initialize our TokenProvider
//        tokenProvider = TokenProvider(context.applicationContext)
//    }
//
//    private val authInterceptor: AuthInterceptor by lazy {
//        AuthInterceptor(tokenProvider)
//    }
//
//    private val client: OkHttpClient by lazy {
//        OkHttpClient.Builder()
//            .addInterceptor(authInterceptor)
//            .build()
//    }
//
//    val instance: ApiService by lazy {
//        Retrofit.Builder()
//            .baseUrl("https://heroesofpenta.com/app-api/")
//            .client(client)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(ApiService::class.java)
//    }
//}
