//package com.heroesofpenta.data.tiktok.model
//
//import com.google.gson.annotations.SerializedName
//
//data class UserInfoResponse(
//  @SerializedName("error")val error: Map<String, String>,
//  @SerializedName("data")val data: Map<String, UserInfo>
//) {
//  fun getUserInfoData(): UserInfo? {
//    return data["user"]
//  }
//}