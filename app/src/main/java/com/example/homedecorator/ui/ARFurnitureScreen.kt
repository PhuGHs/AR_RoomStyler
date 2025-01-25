package com.example.homedecorator.ui

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.homedecorator.ui.component.ARSceneView
import com.example.homedecorator.ui.component.FurnitureBottomSheet
import com.example.homedecorator.viewmodel.ArFurnitureViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARFurnitureScreen(
    viewModel: ArFurnitureViewModel
) {
    val selectedFurniture by viewModel.selectedFurniture
    val placedFurniture by viewModel.placedFurniture.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        ARSceneView(
            selectedFurniture = selectedFurniture,
            modifier = Modifier.fillMaxSize(),
            onError = { error ->
                Log.e("ARSceneView", "Error: ${error.message}")
            },
            onFurniturePlaced = { furniture ->
                viewModel.addPlacedFurniture(furniture)
            }
        )

        Button(
            onClick = {
                coroutineScope.launch {
                    Log.i("HD Button", "button clicked");
                    sheetState.show()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text(text = "Furniture List (${placedFurniture.size})")
        }

        FurnitureBottomSheet(
            furnitureItems = viewModel.furnitureItems,
            onFurnitureSelected = { viewModel.selectFurniture(it) },
            onDismiss = { viewModel.clearSelection() },
            sheetState = sheetState
        )
    }
}