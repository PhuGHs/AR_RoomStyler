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
import com.google.ar.core.Pose
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.ar.rememberARCameraStream
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Scale
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
    onInvalidPlane: (() -> Unit)? = null,
    onModelTooLarge: (() -> Unit)? = null
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
                    isPlaneAppropriateForFurniture(plane, selectedFurniture, plane.centerPose)
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
        scaleToUnits = 1.0f // Start with a scale of 1.0f
    ).apply {
        isEditable = true
    }

    // Get the pre-calculated dimensions of the model (in meters)
    val modelDimensions = selectedFurniture?.dimensions ?: Dimensions(1.0f, 1.0f, 1.0f)

    // Get the plane dimensions from the anchor
    val planeDimensions = if (anchor.trackingState == TrackingState.TRACKING) {
        val trackable = anchor.pose
        if (trackable is Plane) {
            Dimensions(trackable.extentX, 1.0f, trackable.extentZ)
        } else {
            null
        }
    } else {
        null
    } ?: Dimensions(1.0f, 1.0f, 1.0f)

    // Calculate the scale required to fit the model within the plane
    val scaleX = planeDimensions.width / modelDimensions.width
    val scaleZ = planeDimensions.depth / modelDimensions.depth
    val scale = minOf(scaleX, scaleZ, 1.0f) // Ensure the model fits within the plane

    // Apply the calculated scale to the model
    modelNode.scale = Scale(scale, scale, scale)

    // Add a bounding box for debugging (optional)
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

    // Enable editing (optional)
    listOf(modelNode, anchorNode).forEach {
        it.onEditingChanged = { editingTransforms ->
            boundingBoxNode.isVisible = editingTransforms.isNotEmpty()
        }
    }

    return anchorNode
}

private fun isPlaneAppropriateForFurniture(
    plane: Plane,
    furniture: FurnitureItem?,
    anchorPose: Pose? = null
): Boolean {
    if (furniture == null) return false

    // Ensure plane is horizontal and facing upward
    if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) return false

    // Optional: Check if the anchor's pose is within the plane's polygon
    if (anchorPose != null && !plane.isPoseInPolygon(anchorPose)) {
        Log.i("ARSceneView", "Anchor's pose is not within the plane's polygon")
        return false
    }

    // Get furniture dimensions (in meters)
    val furnitureDimensions = furniture.dimensions ?: return false
    val requiredWidth = furnitureDimensions.width
    val requiredDepth = furnitureDimensions.depth

    // Check if the model is larger than the plane
    val isModelLargerThanPlane = requiredWidth > plane.extentX || requiredDepth > plane.extentZ
    if (isModelLargerThanPlane) {
        Log.i("ARSceneView", "Model is too large for the plane - " +
                "Required Width: $requiredWidth (Plane: ${plane.extentX}), " +
                "Required Depth: $requiredDepth (Plane: ${plane.extentZ})")
        return false
    }

    // Check if plane is large enough to accommodate furniture (with buffer)
    val isWidthSufficient = plane.extentX * Constants.PLANE_SIZE_BUFFER_RATIO >= requiredWidth
    val isDepthSufficient = plane.extentZ * Constants.PLANE_SIZE_BUFFER_RATIO >= requiredDepth

    Log.i("ARSceneView", "Plane suitability check - " +
            "Furniture: ${furniture.name}, " +
            "Required Width: $requiredWidth (Plane: ${plane.extentX * Constants.PLANE_SIZE_BUFFER_RATIO}), " +
            "Required Depth: $requiredDepth (Plane: ${plane.extentZ * Constants.PLANE_SIZE_BUFFER_RATIO}), " +
            "Suitable: ${isWidthSufficient && isDepthSufficient}")
    return isWidthSufficient && isDepthSufficient
}