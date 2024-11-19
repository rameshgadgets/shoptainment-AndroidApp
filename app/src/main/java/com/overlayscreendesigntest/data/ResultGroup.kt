package com.overlayscreendesigntest.data

data class ResultGroup(
    val average_score: Double,
    val detected_item: DetectedItem,
    val max_score: Double,
    val rank_score: Double,
    val similar_products: List<SimilarProduct>
)