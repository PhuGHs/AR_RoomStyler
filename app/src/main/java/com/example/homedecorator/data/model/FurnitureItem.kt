package com.example.homedecorator.data.model

data class FurnitureItem(
    val id: Int,
    val name: String,
    val thumbnailResId: Int,
    val modelResId: Int,
    var dimensions: Dimensions? = null
)

data class Dimensions(
    val width: Float,
    val height: Float,
    val depth: Float
)
