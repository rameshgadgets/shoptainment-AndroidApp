package com.overlayscreendesigntest.data

data class GlobalSearchResponse(
    val results: List<GlobalProduct>,
    val time: Double
)

data class GlobalProduct(
    val title: String,
    val price: String,
    val buy_link: String,
    val image: String
)