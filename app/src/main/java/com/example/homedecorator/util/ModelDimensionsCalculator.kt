package com.example.homedecorator.util

import com.example.homedecorator.data.model.Dimensions
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.model.model
import io.github.sceneview.node.ModelNode


class ModelDimensionsCalculator(
    private val modelLoader: ModelLoader
) {
    fun calculateDimensions(modelResId: Int): Dimensions {
        val modelInstance = modelLoader.createModelInstance(rawResId = modelResId)
        val modelNode = ModelNode(
            modelInstance = modelInstance,
            scaleToUnits = 1.0f // Use a scale of 1.0f to get raw dimensions
        )

        // Get the raw extents of the model
        val rawExtents = modelNode.extents

        // Assuming the model's extents are in meters, use them directly
        // If the model's extents are not in meters, apply a conversion factor here
        val dimensions = Dimensions(
            width = rawExtents.x,
            height = rawExtents.y,
            depth = rawExtents.z
        )

        // Clean up the model node
        modelNode.destroy()

        return dimensions
    }
}