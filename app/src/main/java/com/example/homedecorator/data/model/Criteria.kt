package com.example.homedecorator.data.model

data class PlacementCriteria(
    val minPlaneSize: Float = 0.5f, // Minimum plane dimension (meters)
    val maxModelToPlaneRatio: Float = 0.8f, // Max model size relative to plane
    val bufferRatio: Float = 0.9f, // Buffer around the model (relative to plane)
    val minClearance: Float = 0.1f // Minimum clearance around the model (meters)
)