package com.overlayscreendesigntest.data

data class DetectedItem(
    val area: Double,
    val bounding_box: BoundingBox,
    val category: String,
    val detection_confidence: Double,
    val item_image: String,
    val name: String
)