package com.heroesofpenta.data.network
import com.heroesofpenta.data.models.DeleteResponse
import com.heroesofpenta.data.models.NftHero
import com.heroesofpenta.data.models.NonceResponse
import com.heroesofpenta.data.models.User
import com.heroesofpenta.data.models.WalletResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

// Example request/response data classes
data class TikTokLoginRequest(val codeVerifier: String, val code: String)
data class LoginResponse(val accessToken: String, val expiresIn: Long)
data class RefreshTokenResponse(val token: String, val expiresIn: Long)
data class BasicResponse(val data: String)
data class TrainingResponse(val xp: Int, val phrase: String)

interface ApiService {
    // 1) Get current user info
    @GET("user/me")
    fun getUser(): Call<User>

    // 2) Request a nonce for wallet signing
    @GET("wallet/nonce")
    fun getNonceForWallet(): Call<NonceResponse>

    // 3) Register wallet with signature
    @POST("wallet/register")
    fun registerWallet(
        //@Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Call<WalletResponse>

    // 4) Disconnect wallet
    @POST("wallet/disconnect")
    fun disconnectWallet(): Call<WalletResponse>

    // 5) Delete account
    @DELETE("user")
    fun deleteAccount(): Call<DeleteResponse>

    // 6) (Optional) If you have a dedicated endpoint for "deleteWallet"
    //   Add it if you want that specifically.
    // @DELETE("wallet")
    // fun deleteWallet(): Call<DeleteResponse>

    // Exchange TikTok code for server session token
    @POST("auth/login/tiktok")
    fun tiktokLogin(@Body request: TikTokLoginRequest): Call<LoginResponse>

    // Optionally handle refresh
    @POST("auth/refresh")
    fun refreshToken(): Call<RefreshTokenResponse>

    // Example: fetch user’s NFT heroes
    @GET("user/nft-heroes")
    fun getNftHeroes(): Call<List<NftHero>>

    @GET("user/nft-hero/{id}")
    fun getNftHeroById(
        //,
        @Path("id") id: String
    ): Call<NftHero>

    @POST("user/update-name")
    fun updateUserName(
        //,
        @Body body: Map<String, String>
    ): Call<BasicResponse>

    // Add all other endpoints e.g. training, metamask, etc.
    @Multipart
    @POST("training/selfie")
    fun uploadSelfie(
      @Part selfie: MultipartBody.Part,
      @Part("selectedHeroIds") selectedHeroIds: RequestBody
    ): Call<TrainingResponse>
}
