package com.example.homedecorator.ui.component

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.homedecorator.data.model.Dimensions
import com.example.homedecorator.data.model.FurnitureItem
import com.example.homedecorator.util.Constants
import com.example.homedecorator.viewmodel.ArFurnitureViewModel
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
import timber.log.Timber

@Composable
fun ARSceneView(
    selectedFurniture: FurnitureItem?,
    viewModel: ArFurnitureViewModel,
    modifier: Modifier = Modifier,
    onError: (Exception) -> Unit = {},
    onFurniturePlaced: ((FurnitureItem) -> Unit)? = null,
    onInvalidPlane: (() -> Unit)? = null
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

    LaunchedEffect(Unit) {
        viewModel.updateDimension(modelLoader)
    }

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

            updatedFrame.getUpdatedPlanes()
                .firstOrNull { plane ->
                    isPlaneAppropriateForFurniture(plane, selectedFurniture)
                }?.let { plane ->
                    if (selectedFurniture != null) {
                        plane.createAnchorOrNull(plane.centerPose)?.let { anchor ->
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
                } ?: run {
                Log.i("ARSceneView","No suitable plane found for furniture")
                onInvalidPlane?.invoke()
            }
        }
//        ,
//        onGestureListener = rememberOnGestureListener(
//            onSingleTapConfirmed = { motionEvent, node ->
//                if (selectedFurniture != null) {
//                    if (node == null) {
//                        val hitResults = frame?.hitTest(motionEvent.x, motionEvent.y)
//                        hitResults?.firstOrNull { hit ->
//                            hit.isValid(depthPoint = false, point = false) &&
//                                    hit.trackable is Plane
//                        }?.let { hit ->
//                            val plane = hit.trackable as Plane
//                            if (isPlaneAppropriateForFurniture(plane, selectedFurniture)) {
//                                hit.createAnchorOrNull()?.let { anchor ->
//                                    planeRenderer = false
//                                    val anchorNode = createAnchorNode(
//                                        engine = engine,
//                                        modelLoader = modelLoader,
//                                        materialLoader = materialLoader,
//                                        modelInstances = modelInstances,
//                                        anchor = anchor,
//                                        selectedFurniture = selectedFurniture
//                                    )
//                                    childNodes += anchorNode
//                                    placedFurnitureCount++
//                                    onFurniturePlaced?.invoke(selectedFurniture)
//                                }
//                            } else {
//                                    Log.i("ARSceneView","Tapped plane not suitable for furniture")
//                                onInvalidPlane?.invoke()
//                            }
//                        }
//                    }
//                }
//            }
//        )
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
        scaleToUnits = 1.0f
    ).apply {
        isEditable = true
    }

    val boundingBoxNode = CubeNode(
        engine,
        size = modelNode.extents,
        center = modelNode.center,
        materialInstance = materialLoader.createColorInstance(Color.White.copy(alpha = 1.0f))
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

private fun isPlaneAppropriateForFurniture(plane: Plane, furniture: FurnitureItem?): Boolean {
    if (furniture == null) return false

    // Ensure plane is horizontal and facing upward
    if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) return false

    // Get furniture dimensions
    val furnitureDimensions = furniture.dimensions ?: return false
    val requiredWidth = furnitureDimensions.width
    val requiredDepth = furnitureDimensions.depth

    // Check if plane is large enough to accommodate furniture
    val isWidthSufficient = plane.extentX * Constants.PLANE_SIZE_BUFFER_RATIO >= requiredWidth
    val isDepthSufficient = plane.extentZ * Constants.PLANE_SIZE_BUFFER_RATIO >= requiredDepth

    Log.i("ARSceneView", "Plane suitability check - " +
            "Furniture: ${furniture.name}, " +
            "Required Width: $requiredWidth (Plane: ${plane.extentX * Constants.PLANE_SIZE_BUFFER_RATIO}), " +
            "Required Depth: $requiredDepth (Plane: ${plane.extentZ * Constants.PLANE_SIZE_BUFFER_RATIO}), " +
            "Suitable: ${isWidthSufficient && isDepthSufficient}")
    return isWidthSufficient && isDepthSufficient
}