//package com.heroesofpenta.data.tiktok.network
//
//import com.heroesofpenta.data.tiktok.model.AccessTokenResponse
//import com.heroesofpenta.data.tiktok.model.UserInfoResponse
//import retrofit2.Call
//import retrofit2.http.Field
//import retrofit2.http.FormUrlEncoded
//import retrofit2.http.GET
//import retrofit2.http.Header
//import retrofit2.http.POST
//import retrofit2.http.Query
//
//interface TikTokApiService {
//  @FormUrlEncoded
//  @POST("/v2/oauth/token/")
//  fun getAccessToken(
//    @Field("code") code: String,
//    @Field("client_key") clientKey: String,
//    @Field("client_secret") clientSecret: String,
//    @Field("grant_type") grantType: String,
//    @Field("redirect_uri") redirectUri: String,
//    //@Field("code_verifier") codeVerifier: String,
//  ): Call<AccessTokenResponse>
//
//  @GET("/v2/user/info/")
//  fun getUserInfo(
//    @Header("Authorization") accessToken: String,
//    @Query("fields") fields: String = "open_id,union_id,avatar_url,display_name"
//  ): Call<UserInfoResponse>
//}