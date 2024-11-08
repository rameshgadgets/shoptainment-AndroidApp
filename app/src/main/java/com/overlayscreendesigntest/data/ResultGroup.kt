package com.overlayscreendesigntest.data

data class ResultGroup(
    val average_score: Double,
    val detected_item: DetectedItem,
    val max_score: Int,
    val rank_score: Double,
    val similar_products: List<SimilarProduct>
)