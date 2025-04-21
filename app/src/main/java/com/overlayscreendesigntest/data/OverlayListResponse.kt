package com.overlayscreendesigntest.data

data class OverlayListResponse(
    val `data`: Data
)

data class Data(
    val query_image: String,
    val result_groups: List<ResultGroup>
)

data class ResultGroup(
    val average_score: Double,
    val detected_item: DetectedItem,
    val max_score: Double,
    val rank_score: Double,
    val similar_products: List<SimilarProduct>
)

data class DetectedItem(
    val area: Double,
    val bounding_box: BoundingBox,
    val category: Any,
    val detection_confidence: Double,
    val item_image: Any,
    val name: String
)

data class SimilarProduct(
    val brand_name: String,
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
    val vendor: String
)

data class BoundingBox(
    val bottom: Double,
    val left: Double,
    val right: Double,
    val top: Double
)