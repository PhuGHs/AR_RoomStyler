package com.example.homedecorator.util

import com.example.homedecorator.data.model.Dimensions
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.model.model
import io.github.sceneview.node.ModelNode


class ModelDimensionsCalculator(
    private val modelLoader: ModelLoader
) {
    fun calculateDimensions(modelResId: Int, desiredScale: Float = 1.0f): Dimensions {
        val modelInstance = modelLoader.createModelInstance(rawResId = modelResId);
        val modelNode = ModelNode(
            modelInstance = modelInstance,
            scaleToUnits = desiredScale
        )

        val rawExtents = modelNode.extents;
        val dimensions = Dimensions(
            width = (rawExtents.x * desiredScale),
            height = (rawExtents.y * desiredScale),
            depth = (rawExtents.z * desiredScale)
        )

        modelNode.destroy();

        return dimensions;
    }
}