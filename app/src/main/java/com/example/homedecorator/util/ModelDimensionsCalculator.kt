package com.example.homedecorator.util

import com.example.homedecorator.data.model.Dimensions
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.model.model
import io.github.sceneview.node.ModelNode


class ModelDimensionsCalculator(
    private val modelLoader: ModelLoader
) {
    fun calculateDimensions(modelResId: Int, desiredScale: Float = 0.5f): Dimensions {
        val modelInstance = modelLoader.createModelInstance(rawResId = modelResId)
        val modelNode = ModelNode(
            modelInstance = modelInstance,
            scaleToUnits = 1.0f // Use a scale of 1.0f to get raw dimensions
        )

        val modelUnitsToMeters = 0.01f

        val bounds = modelNode.boundingBox // Get the bounding box

        val dimensions = Dimensions(
            width = bounds.halfExtent[0] * 2f * modelUnitsToMeters,  // Width is 2x halfExtent
            height = bounds.halfExtent[1] * 2f * modelUnitsToMeters, // Height is 2x halfExtent
            depth = bounds.halfExtent[2] * 2f * modelUnitsToMeters // Depth is 2x halfExtent
        )

        // Clean up the model node
        modelNode.destroy()

        return dimensions
    }
}