package com.example.homedecorator.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.homedecorator.ui.component.ARSceneView
import com.example.homedecorator.ui.component.CircularModelNavigation
import com.example.homedecorator.viewmodel.ArFurnitureViewModel
import java.util.Objects

@Composable
fun ARFurnitureScreen(
    viewModel: ArFurnitureViewModel,
    modifier: Modifier = Modifier
) {
    val selectedFurniture by viewModel.selectedFurniture.collectAsState()
    val context = LocalContext.current
    val noFurnitureItemSelected by remember {
        derivedStateOf { Objects.isNull(viewModel.selectedFurniture) }
    }


    Box(modifier = modifier.fillMaxSize().systemGestureExclusion()) {
        ARSceneView(
            selectedFurniture = selectedFurniture,
            viewModel = viewModel,
            modifier = modifier,
            onError = { error ->
                Log.i("ARSceneView", "Error: ${error.message}")
            },
            onFurniturePlaced = { furniture ->
                viewModel.placeFurniture(furniture)
            },
            onInvalidPlane = {
                Log.i("Invalid plane", "Invalid plane")
            }
        )

        CircularModelNavigation(
            viewModel = viewModel,
            modifier = modifier.align(Alignment.BottomCenter)
        )
    }
}
