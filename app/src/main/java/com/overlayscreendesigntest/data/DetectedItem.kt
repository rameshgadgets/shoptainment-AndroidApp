package com.overlayscreendesigntest.data

data class DetectedItem(
    val area: Double,
    val bounding_box: BoundingBox,
    val category: Any,
    val detection_confidence: Double,
    val item_image: Any,
    val name: String
)