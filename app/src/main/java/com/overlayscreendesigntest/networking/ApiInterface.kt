package com.overlayscreendesigntest.networking

import com.overlayscreendesigntest.data.OverlayListResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST("v1/global/search")
    fun fetchOverLayScreenItems(
        @Part("api_key") apiKey: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<OverlayListResponse>
}