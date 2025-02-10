package com.example.homedecorator.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.homedecorator.ui.component.ARSceneView
import com.example.homedecorator.ui.component.CircularModelNavigation
import com.example.homedecorator.viewmodel.ArFurnitureViewModel
import kotlinx.coroutines.launch

@Composable
fun ARFurnitureScreen(
    viewModel: ArFurnitureViewModel,
    modifier: Modifier = Modifier
) {
    val selectedFurniture by viewModel.selectedFurniture.collectAsState()
    val placedFurnitureList by viewModel.placedFurnitureList.collectAsState()
    val context = LocalContext.current


    Box(modifier = modifier
        .fillMaxSize()
        .systemGestureExclusion()) {
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
            },
            onMoveModelOutOfRange = {
                Toast.makeText(context, "Model is outside the detected plane", Toast.LENGTH_SHORT).show()
            },
            onModelTooLarge = {
                Toast.makeText(context, "Model is too large for this detected area", Toast.LENGTH_SHORT).show()
            }
        )
        
        if (placedFurnitureList.contains(selectedFurniture)) {
            Button(
                onClick = {},
                modifier = modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            ) {
                Text(text = "Placed")
            }
        }

        CircularModelNavigation(
            viewModel = viewModel,
            modifier = modifier.align(Alignment.BottomCenter)
        )
    }
}
