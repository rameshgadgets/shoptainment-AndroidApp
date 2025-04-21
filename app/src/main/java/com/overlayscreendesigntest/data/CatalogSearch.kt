package com.overlayscreendesigntest.data

import com.google.gson.JsonElement

data class CatalogSearchResponse(
    val results: JsonElement? = null
)

data class CatalogItem(
    val name: String,
    val image: String,
    val link: String,
    val description: String
)