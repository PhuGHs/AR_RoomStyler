package com.example.homedecorator.util

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node


fun ModelNode.enableGestures() {
    // Set rendering priority higher to properly load occlusion.
    setPriority(Constants.MODEL_RENDER_LOWEST_PRIORITY)
    // Model Node needs to be editable for independent rotation from the anchor rotation
    isEditable = true
    isRotationEditable = true
    isTouchable = true
}

suspend fun Node.startBouncingEffect(
    targetValue: Float = Constants.MODEL_BOUNCING_HEIGHT
) {
    animate(
        initialValue = Constants.MODEL_NO_HEIGHT,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = Constants.MODEL_BOUNCING_DURATION),
            repeatMode = RepeatMode.Reverse
        )
    ) { value, _ ->
        position = Position(y = value)
    }
}

suspend fun Node.endBouncingEffect() {
    animate(
        initialValue = position.y,
        targetValue = Constants.MODEL_NO_HEIGHT,
        animationSpec = tween()
    ) { value, _ ->
        position = Position(y = value)
    }
}
