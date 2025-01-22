package com.example.homedecorator.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.homedecorator.data.model.FurnitureItem
import com.google.ar.core.Config
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.rememberARCameraStream
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes

@Composable
fun ARSceneView(
    selectedFurniture: FurnitureItem?,
    modifier: Modifier = Modifier,
    onError: (Exception) -> Unit = {}
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine = engine)
    val materialLoader = rememberMaterialLoader(engine = engine)

    ARScene(
        modifier,
        engine,
        modelLoader,
        planeRenderer = true,
        sessionConfiguration = {
            session, config ->
            config.apply {
                depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    true -> Config.DepthMode.AUTOMATIC
                    else -> Config.DepthMode.DISABLED
                }
                instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            }
        },
        cameraStream = rememberARCameraStream(materialLoader = materialLoader),
        onSessionFailed = {
            exception -> onError(exception)
        },
        childNodes = rememberNodes {
            selectedFurniture?.let { item ->
                add(ModelNode(
                    modelInstance = modelLoader.createModelInstance(
                        rawResId = item.modelResId
                    ),
                    scaleToUnits = 1.0f
                ).apply {
                    isEditable = true
                }
                )
            }
        }
    )
}