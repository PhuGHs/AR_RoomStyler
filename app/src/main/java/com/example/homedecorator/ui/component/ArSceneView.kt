package com.example.homedecorator.ui.component

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.homedecorator.data.model.FurnitureItem
import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingFailureReason
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.ar.rememberARCameraStream
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView

@Composable
fun ARSceneView(
    selectedFurniture: FurnitureItem?,
    modifier: Modifier = Modifier,
    onError: (Exception) -> Unit = {},
    onFurniturePlaced: ((FurnitureItem) -> Unit)? = null
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine = engine)
    val materialLoader = rememberMaterialLoader(engine = engine)
    val childNodes = rememberNodes()
    val cameraNode = rememberARCameraNode(engine)
    val view = rememberView(engine = engine)
    val collisionSystem = rememberCollisionSystem(view)

    var planeRenderer by remember { mutableStateOf(true) }
    val modelInstances = remember { mutableListOf<ModelInstance>() }
    var trackingFailureReason by remember { mutableStateOf<TrackingFailureReason?>(null) }
    var frame by remember { mutableStateOf<Frame?>(null) }
    var placedFurnitureCount by remember { mutableStateOf(0) }

    ARScene(
        modifier = modifier,
        engine = engine,
        modelLoader = modelLoader,
        childNodes = childNodes,
        view = view,
        collisionSystem = collisionSystem,
        planeRenderer = planeRenderer,
        sessionConfiguration = { session, config ->
            config.apply {
                depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    true -> Config.DepthMode.AUTOMATIC
                    else -> Config.DepthMode.DISABLED
                }
                instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            }
        },
        cameraNode = cameraNode,
        onTrackingFailureChanged = { trackingFailureReason = it },
        onSessionUpdated = { session, updatedFrame ->
            frame = updatedFrame

            if (childNodes.isEmpty() && selectedFurniture != null) {
                updatedFrame.getUpdatedPlanes()
                    .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                    ?.let { it.createAnchorOrNull(it.centerPose) }?.let { anchor ->
                        val anchorNode = createAnchorNode(
                            engine = engine,
                            modelLoader = modelLoader,
                            materialLoader = materialLoader,
                            modelInstances = modelInstances,
                            anchor = anchor,
                            selectedFurniture = selectedFurniture
                        )
                        childNodes += anchorNode
                        placedFurnitureCount++
                        onFurniturePlaced?.invoke(selectedFurniture)
                    }
            }
        },
        onGestureListener = rememberOnGestureListener(
            onSingleTapConfirmed = { motionEvent, node ->
                if (selectedFurniture != null) {
                    Log.i("Furniture selected", selectedFurniture.name)
                } else {
                    Log.i("Status", "None selected");
                }
                if (node == null && selectedFurniture != null) {
                    val hitResults = frame?.hitTest(motionEvent.x, motionEvent.y)
                    hitResults?.firstOrNull {
                        it.isValid(depthPoint = false, point = false)
                    }?.createAnchorOrNull()?.let { anchor ->
                        planeRenderer = false
                        val anchorNode = createAnchorNode(
                            engine = engine,
                            modelLoader = modelLoader,
                            materialLoader = materialLoader,
                            modelInstances = modelInstances,
                            anchor = anchor,
                            selectedFurniture = selectedFurniture
                        )
                        childNodes += anchorNode
                        placedFurnitureCount++
                        onFurniturePlaced?.invoke(selectedFurniture)
                    }
                }
            }
        )
    )

    trackingFailureReason?.let { reason ->
        when (reason) {
            TrackingFailureReason.BAD_STATE -> onError(Exception("Tracking in bad state"))
            TrackingFailureReason.INSUFFICIENT_LIGHT -> onError(Exception("Insufficient light for tracking"))
            TrackingFailureReason.EXCESSIVE_MOTION -> onError(Exception("Too much device motion"))
            TrackingFailureReason.NONE -> {} // No error
            else -> onError(Exception("Unknown tracking failure"))
        }
    }
}

fun createAnchorNode(
    engine: Engine,
    modelLoader: ModelLoader,
    materialLoader: MaterialLoader,
    modelInstances: MutableList<ModelInstance>,
    selectedFurniture: FurnitureItem?,
    anchor: Anchor
): AnchorNode {
    val anchorNode = AnchorNode(engine = engine, anchor = anchor)
    val modelInstance = if (modelInstances.isNotEmpty()) {
        modelInstances.removeLast()
    } else if (selectedFurniture != null) {
        modelLoader.createModelInstance(rawResId = selectedFurniture.modelResId).also {
            modelInstances.add(it)
        }
    } else {
        throw IllegalStateException("No model instances available and no furniture selected.")
    }

    val modelNode = ModelNode(
        modelInstance = modelInstance,
        scaleToUnits = 0.5f
    ).apply {
        isEditable = true
    }

    val boundingBoxNode = CubeNode(
        engine,
        size = modelNode.extents,
        center = modelNode.center,
        materialInstance = materialLoader.createColorInstance(Color.White.copy(alpha = 0.5f))
    ).apply {
        isVisible = false
    }
    modelNode.addChildNode(boundingBoxNode)
    anchorNode.addChildNode(modelNode)

    listOf(modelNode, anchorNode).forEach {
        it.onEditingChanged = { editingTransforms ->
            boundingBoxNode.isVisible = editingTransforms.isNotEmpty()
        }
    }
    return anchorNode
}