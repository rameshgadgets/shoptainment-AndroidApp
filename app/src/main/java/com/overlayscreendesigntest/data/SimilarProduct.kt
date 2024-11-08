package com.overlayscreendesigntest.data

data class SimilarProduct(
    val brand_name: Any,
    val category: String,
    val currency: String,
    val gender: String,
    val id: String,
    val images: List<String>,
    val matching_image: String,
    val name: String,
    val price: String,
    val reduced_price: String,
    val score: Double,
    val sub_category: String,
    val url: String,
    val vendor: Any
)