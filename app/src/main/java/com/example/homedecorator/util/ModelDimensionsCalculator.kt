package com.example.homedecorator.util

import com.example.homedecorator.data.model.Dimensions
import com.google.android.filament.utils.Float3
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.node.ModelNode
import kotlin.math.abs


class ModelDimensionsCalculator(
    private val modelLoader: ModelLoader
) {
    fun calculateDimensions(modelResId: Int): Dimensions {
        val modelInstance = modelLoader.createModelInstance(rawResId = modelResId)
        val modelNode = ModelNode(modelInstance = modelInstance)

        val bounds = modelNode.boundingBox
        val halfExtent = bounds.halfExtent.let { v -> Float3(v[0], v[1], v[2]) }

        val dimensions = Dimensions(
            width = abs(halfExtent.x * 2f),
            height = abs(halfExtent.y * 2f),
            depth = abs(halfExtent.z * 2f)
        )

        modelNode.destroy()
        return dimensions
    }
}