package com.example.homedecorator.ui.component


import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.homedecorator.data.model.Dimensions
import com.example.homedecorator.data.model.FurnitureItem
import com.example.homedecorator.util.Constants
import com.example.homedecorator.util.enableGestures
import com.example.homedecorator.util.endBouncingEffect
import com.example.homedecorator.util.startBouncingEffect
import com.example.homedecorator.viewmodel.ArFurnitureViewModel
import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.TrackingFailureReason
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.arcore.position
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.CylinderNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView

@Composable
internal fun ARSceneView(
    selectedFurniture: FurnitureItem?,
    viewModel: ArFurnitureViewModel,
    modifier: Modifier = Modifier,
    onError: (Exception) -> Unit = {},
    onFurniturePlaced: ((FurnitureItem) -> Unit)? = null,
    onInvalidPlane: (() -> Unit)? = null,
    onModelTooLarge: (() -> Unit)? = null,
    onMoveModelOutOfRange: (() -> Unit)? = null
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine = engine)
    val materialLoader = rememberMaterialLoader(engine = engine)
    val childNodes = rememberNodes()
    val cameraNode = rememberARCameraNode(engine)
    val view = rememberView(engine = engine)
    val collisionSystem = rememberCollisionSystem(view)

    val modelInstanceCache = remember { mutableMapOf<Int, ModelInstance>() }

    var planeRenderer by remember { mutableStateOf(true) }
    val modelInstances = remember { mutableListOf<ModelInstance>() }
    var trackingFailureReason by remember { mutableStateOf<TrackingFailureReason?>(null) }
    var frame by remember { mutableStateOf<Frame?>(null) }
    var placedFurnitureCount by remember { mutableIntStateOf(0) }
    var model by remember { mutableStateOf<ModelNode?>(null) }
    var animateModel by remember { mutableStateOf(false) }
    var showCoachingOverlay by remember { mutableStateOf(true) }
    val furnitureDimensions = remember {
        mutableMapOf<Int, Dimensions>()
    }
    var furnitureDimensionsLoaded by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var floorIndicator by remember { mutableStateOf<CylinderNode?>(null) }

        fun getOrCreateModelInstance(modelResId: Int): ModelInstance {
        return modelInstanceCache.getOrPut(modelResId) {
            modelLoader.createModelInstance(rawResId = modelResId)
        }
    }
    var placedFurniture by remember {
        mutableStateOf<FurnitureItem?>(null)
    }
    var currentPlane by remember { mutableStateOf<Plane?>(null) }

    LaunchedEffect(Unit) {
        val items = viewModel.updateDimension(modelLoader)

        items.forEachIndexed { _, furnitureItem ->
            furnitureItem.dimensions?.let { furnitureDimensions[furnitureItem.id] = it }
        }
        furnitureDimensionsLoaded = true
    }

    LaunchedEffect(Unit) {
        view.setShadowingEnabled(true)
    }

    LaunchedEffect(key1 = animateModel) {
        model?.apply {
            if (animateModel) startBouncingEffect() else endBouncingEffect()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        ARScene(
            modifier = modifier.fillMaxSize(),
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
                    updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                    instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                    lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                }
            },
            cameraNode = cameraNode,
            onTrackingFailureChanged = { trackingFailureReason = it },
            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { _, _ ->
                    animateModel = false
                },
                onMoveBegin = { _, _, node ->
                    if (model !== null && node is ModelNode) {
                        isDragging = true
                        floorIndicator = createFloorIndicator(engine, materialLoader, node, 0.25f)
                        floorIndicator?.apply {
                            parent = node.parent
                        }
                    }
                },
                onMove = { _, e, node ->
                    if (node == null) return@rememberOnGestureListener
                    if (isDragging && floorIndicator != null && currentPlane != null) {
                        frame?.hitTest(e)?.firstOrNull()?.hitPose?.let { hitPose ->
                            if (currentPlane!!.isPoseInPolygon(hitPose)) {
                                val anchor = (node.parent as? AnchorNode) ?: node
                                anchor.worldPosition = hitPose.position
                                floorIndicator!!.worldPosition = hitPose.position
                            } else {
                                onMoveModelOutOfRange?.invoke()
                            }
                        }
                    }
                },
                onMoveEnd = { _, _, _ ->
                    isDragging = false
                    floorIndicator?.let {
                        it.parent?.removeChildNode(it)
                        floorIndicator = null
                    }
                }
            ),
            onSessionUpdated = { _, updatedFrame ->
                frame = updatedFrame

                updatedFrame.getUpdatedPlanes()
                    .firstOrNull { plane ->
                        selectedFurniture?.let { furniture ->
                            val furnitureSize = furnitureDimensions[furniture.id]
                            if (furnitureSize != null) {
                                var fit = isPlaneLargeEnough(plane, furnitureSize)
                                if (!fit) {
                                    onModelTooLarge?.invoke()
                                }
                                fit
                            } else {
                                false
                            }
                        } ?: false
                    }?.let { plane ->
                        currentPlane = plane
                        if (selectedFurniture != null) {
                            if (childNodes.isEmpty()) {
                                plane.createAnchorOrNull(plane.centerPose)?.let { anchor ->
                                    animateModel = true
                                    val pair = createAnchorNode(
                                        engine = engine,
                                        modelInstances = modelInstances,
                                        anchor = anchor,
                                        selectedFurniture = selectedFurniture,
                                        getOrCreateModelInstance = ::getOrCreateModelInstance,
                                    )
                                    childNodes += pair.first
                                    model = pair.second
                                    placedFurnitureCount++
                                    placedFurniture = selectedFurniture
                                    onFurniturePlaced?.invoke(selectedFurniture)
                                    showCoachingOverlay = false
                                }
                            }
                            placedFurniture?.let {
                                if (it.id != selectedFurniture.id) {
                                    childNodes.clear()
                                    model = null
                                    placedFurnitureCount--
                                    showCoachingOverlay = true
                                    animateModel = false
                                }
                            }
                        }
                    } ?: run {
                    onInvalidPlane?.invoke()
                    currentPlane = null
                }
            }
        )

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color.Black.copy(alpha = 0.5F)),
            visible = showCoachingOverlay,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CoachingOverlay(
                message = trackingFailureReason?.getDescription(LocalContext.current) ?: "Scan to find a suitable place to put the item on"
            )
        }
    }

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

private fun isPlaneLargeEnough(plane: Plane, furnitureSize: Dimensions): Boolean {
    if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) return false

    val planeCenterPose = plane.centerPose

    val halfWidth = furnitureSize.width / 2f
    val halfDepth = furnitureSize.depth / 2f

    Log.i("stat", "halfWidth: ${halfWidth}, halfDepth: ${halfDepth}")

    val corners = arrayOf(
        Pose(floatArrayOf(-halfWidth, 0f, -halfDepth), floatArrayOf(0f, 0f, 0f, 1f)), // Bottom-left corner
        Pose(floatArrayOf(halfWidth, 0f, -halfDepth), floatArrayOf(0f, 0f, 0f, 1f)),  // Bottom-right corner
        Pose(floatArrayOf(halfWidth, 0f, halfDepth), floatArrayOf(0f, 0f, 0f, 1f)),   // Top-right corner
        Pose(floatArrayOf(-halfWidth, 0f, halfDepth), floatArrayOf(0f, 0f, 0f, 1f))  // Top-left corner
    )

    return corners.all { corner ->
        val worldCornerPose = planeCenterPose.compose(corner);
        plane.isPoseInPolygon(worldCornerPose)
    }
}

fun createAnchorNode(
    engine: Engine,
    modelInstances: MutableList<ModelInstance>,
    selectedFurniture: FurnitureItem?,
    anchor: Anchor,
    getOrCreateModelInstance: (Int) -> ModelInstance
): Pair<AnchorNode, ModelNode> {
    val anchorNode = AnchorNode(engine = engine, anchor = anchor)

    val modelInstance = if (modelInstances.isNotEmpty()) {
        modelInstances.removeLast()
    } else if (selectedFurniture != null) {
        getOrCreateModelInstance(selectedFurniture.modelResId)
    } else {
        throw IllegalStateException("No model instances available and no furniture selected.")
    }

    val modelNode = ModelNode(
        modelInstance = modelInstance,
        scaleToUnits = Constants.DESIRED_SCALE
    ).apply {
        enableGestures()
        rotation = Rotation()
        isShadowCaster = true
        isShadowReceiver = true
    }

    val bounds = modelNode.boundingBox

//    val boundingBoxNode = CubeNode(
//        engine,
//        size = Size(
//            bounds.halfExtent[0] * 2f, // Width
//            bounds.halfExtent[1] * 2f, // Height
//            bounds.halfExtent[2] * 2f // Depth
//        ),
//        materialInstance = materialLoader.createColorInstance(Color.Red.copy(alpha = 0.5f))
//    ).apply {
//        isShadowCaster = false // Bounding box shouldn't cast shadows
//        isShadowReceiver = false // Bounding box shouldn't receive shadows
//        position = Position(bounds.center[0], bounds.center[1], bounds.center[2]) // Center the bounding box
//    }

//    modelNode.addChildNode(boundingBoxNode)
    anchorNode.addChildNode(modelNode)

    return Pair(anchorNode, modelNode)
}

private fun Frame.getPose(x: Float, y: Float): Pose? =
    hitTest(x, y).firstOrNull()?.hitPose

fun createFloorIndicator(
    engine: Engine,
    materialLoader: MaterialLoader,
    modelNode: ModelNode,
    radius: Float
): CylinderNode {
    val ringHeight = 0.002f

    val ring = CylinderNode(
        engine = engine,
        radius = radius,
        height = ringHeight,
        materialInstance = materialLoader.createColorInstance(
            Color.White.copy(alpha = 1.0f)
        )
    ).apply {
        position = Position(
            modelNode.worldPosition.x,
            0.001f,
            modelNode.worldPosition.z
        )
        isShadowCaster = false
        isShadowReceiver = false
    }

    return ring
}