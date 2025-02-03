package com.example.homedecorator.ui.component

import android.content.Context
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.homedecorator.R
import com.example.homedecorator.data.model.Dimensions
import com.example.homedecorator.data.model.FurnitureItem
import com.example.homedecorator.data.model.PlacementCriteria
import com.example.homedecorator.util.Constants
import com.example.homedecorator.util.enableGestures
import com.example.homedecorator.util.endBouncingEffect
import com.example.homedecorator.util.startBouncingEffect
import com.example.homedecorator.viewmodel.ArFurnitureViewModel
import com.google.android.filament.Box
import com.google.android.filament.Engine
import com.google.android.filament.Material
import com.google.ar.core.Anchor
import com.google.ar.core.CameraConfig
import com.google.ar.core.CameraConfigFilter
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.arcore.position
import io.github.sceneview.ar.arcore.rotation
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.ar.rememberARCameraStream
import io.github.sceneview.collision.Matrix
import io.github.sceneview.collision.Quaternion
import io.github.sceneview.collision.Vector3
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import io.github.sceneview.math.Size
import io.github.sceneview.math.times
import io.github.sceneview.math.toFloat3
import io.github.sceneview.math.toMatrix
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.LightNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.PlaneNode
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView
import io.github.sceneview.utils.getResourceUri
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector4f
import timber.log.Timber
import java.util.EnumSet
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.sign

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

    val modelInstanceCache = remember { mutableMapOf<Int, ModelInstance>() }
    val polygonVerticesMap = remember { ConcurrentHashMap<Plane, List<Point>>() }

    var planeRenderer by remember { mutableStateOf(true) }
    val modelInstances = remember { mutableListOf<ModelInstance>() }
    var trackingFailureReason by remember { mutableStateOf<TrackingFailureReason?>(null) }
    var frame by remember { mutableStateOf<Frame?>(null) }
    var placedFurnitureCount by remember { mutableStateOf(0) }
    var model by remember { mutableStateOf<ModelNode?>(null) }
    var animateModel by remember { mutableStateOf(false) }
    var showCoachingOverlay by remember { mutableStateOf(true) }
    var isModelAnchored by remember { mutableStateOf(false) }
    var temporaryPosition by remember { mutableStateOf<Pose?>(null) }

    // Performance optimization: Cache model instances
    fun getOrCreateModelInstance(modelResId: Int): ModelInstance {
        return modelInstanceCache.getOrPut(modelResId) {
            modelLoader.createModelInstance(rawResId = modelResId)
        }
    }

    LaunchedEffect(Unit) {
        view.setShadowingEnabled(true)
    }

    LaunchedEffect(Unit) {
        viewModel.updateDimension(modelLoader)
    }

    LaunchedEffect(key1 = animateModel) {
        model?.apply {
            if (animateModel) startBouncingEffect() else endBouncingEffect()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
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
                    setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE)
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
                        isPlaneAppropriateForFurniture(plane, selectedFurniture, modelLoader, polygonVerticesMap)
                    }?.let { plane ->
                        if (selectedFurniture != null) {
                            if (childNodes.isEmpty()) {
                                plane.createAnchorOrNull(plane.centerPose)?.let { anchor ->
                                    val anchorNode = createAnchorNode(
                                        engine = engine,
                                        modelLoader = modelLoader,
                                        materialLoader = materialLoader,
                                        modelInstances = modelInstances,
                                        anchor = anchor,
                                        selectedFurniture = selectedFurniture,
                                        getOrCreateModelInstance = ::getOrCreateModelInstance
                                    )
                                    childNodes += anchorNode
                                    placedFurnitureCount++
                                    onFurniturePlaced?.invoke(selectedFurniture)
                                    showCoachingOverlay = false
                                }
                            }
                        }
                    } ?: run {
                    Log.i("ARSceneView", "No suitable plane found for furniture")
                    onInvalidPlane?.invoke()
                }
            },
            onGestureListener = rememberOnGestureListener(
                onMove = { _, motionEvent, _ ->
                    if (isModelAnchored) return@rememberOnGestureListener
                    frame?.hitTest(motionEvent)?.firstOrNull()?.hitPose?.let { hitPose ->
                        temporaryPosition = hitPose
                    }
                },
                onSingleTapConfirmed = { motionEvent, _ ->
                    if (isModelAnchored) return@rememberOnGestureListener
                    frame?.hitTest(motionEvent)?.firstOrNull()?.hitPose?.let { hitPose ->
                        temporaryPosition = hitPose
                        isModelAnchored = true
                    }
                }
            )
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

fun createAnchorNode(
    engine: Engine,
    modelLoader: ModelLoader,
    materialLoader: MaterialLoader,
    modelInstances: MutableList<ModelInstance>,
    selectedFurniture: FurnitureItem?,
    anchor: Anchor,
    getOrCreateModelInstance: (Int) -> ModelInstance
): AnchorNode {
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
        isShadowCaster = true
        isShadowReceiver = true
    }
    anchorNode.addChildNode(modelNode)

    return anchorNode
}

private fun isPlaneAppropriateForFurniture(
    plane: Plane,
    furniture: FurnitureItem?,
    modelLoader: ModelLoader,
    polygonVerticesMap: ConcurrentHashMap<Plane, List<Point>>
): Boolean {
    if (furniture == null || plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) return false

    val modelInstance = modelLoader.createModelInstance(rawResId = furniture.modelResId)
    val modelNode = ModelNode(
        modelInstance = modelInstance,
        scaleToUnits = Constants.DESIRED_SCALE
    )

    val bool = isModelPlaceableOnPlane(plane, modelNode, polygonVerticesMap)
    modelNode.destroy()
    return bool
}

private fun isModelPlaceableOnPlane(
    plane: Plane,
    model: ModelNode,
    polygonVerticesMap: ConcurrentHashMap<Plane, List<Point>>
): Boolean {
    val bounds = model.boundingBox
    val modelMatrix = model.worldTransform.toMatrix()

    val min = Vector3(
        bounds.center[0] - bounds.halfExtent[0],
        bounds.center[1] - bounds.halfExtent[1],
        bounds.center[2] - bounds.halfExtent[2]
    )

    val max = Vector3(
        bounds.center[0] + bounds.halfExtent[0],
        bounds.center[1] + bounds.halfExtent[1],
        bounds.center[2] + bounds.halfExtent[2]
    )

    val corners = arrayOf(
        Vector3(min.x, min.y, min.z),
        Vector3(max.x, min.y, min.z),
        Vector3(max.x, max.y, min.z),
        Vector3(min.x, max.y, min.z),
        Vector3(min.x, min.y, max.z),
        Vector3(max.x, min.y, max.z),
        Vector3(max.x, max.y, max.z),
        Vector3(min.x, max.y, max.z)
    )

    val polygonVertices = polygonVerticesMap.getOrPut(plane) {
        val polygonBuffer = plane.polygon
        List(polygonBuffer.limit() / 2) { i ->
            Point(polygonBuffer.get(i * 2), 0f, polygonBuffer.get(i * 2 + 1))
        }
    }

    for (corner in corners) {
        val worldCorner = modelMatrix.transformPoint(corner)
        val points = floatArrayOf(worldCorner.x, worldCorner.y, worldCorner.z)
        val localCorner = plane.centerPose.inverse().transformPoint(points)

        val point2D = Point(localCorner[0], 0f, localCorner[2])

        Log.i("AAAAAAAA", points.joinToString(","))

        //This make performance issues
//        if (!windingNumberAlgorithm(polygonVertices, point2D)) {
//            Log.i("Placeable", "false")
//            return false
//        }
    }
    return true
}

private fun windingNumberAlgorithm(vertices: List<Point>, point: Point): Boolean {
    var windingNumber = 0
    for (i in vertices.indices) {
        val current = vertices[i]
        val next = vertices[(i + 1) % vertices.size]

        if (current.z <= point.z) {
            if (next.z > point.z && isLeftOfEdge(current, next, point) > 0) {
                windingNumber++
            }
        } else {
            if (next.z <= point.z && isLeftOfEdge(current, next, point) < 0) {
                windingNumber--
            }
        }
    }

    return windingNumber != 0
}

private fun isLeftOfEdge(a: Point, b: Point, point: Point): Int {
    return ((b.x - a.x) * (point.z - a.z) - (point.x - a.x) * (b.z - a.z)).sign.toInt()
}

data class Point(val x: Float, val y: Float, val z: Float) //Helper data class