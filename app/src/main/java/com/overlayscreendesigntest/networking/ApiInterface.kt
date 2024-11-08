package com.overlayscreendesigntest.networking

import com.overlayscreendesigntest.data.OverlayListResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    @FormUrlEncoded
    @POST("v1/search")
    fun fetchOverLayScreenItems(
        @Field("api_key") apiKey: String,
        @Field("catalog_name") catalogName: String,
        @Field("image_url") imageUrl: String
    ): Call<OverlayListResponse>
}