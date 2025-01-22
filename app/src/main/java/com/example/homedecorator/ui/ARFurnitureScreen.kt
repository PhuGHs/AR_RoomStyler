package com.example.homedecorator.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.homedecorator.ui.component.ARSceneView
import com.example.homedecorator.ui.component.FurnitureBottomSheet
import com.example.homedecorator.viewmodel.ArFurnitureViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARFurnitureScreen(
    viewModel: ArFurnitureViewModel
) {
    val selectedFurniture by viewModel.selectedFurniture
    val sheetState = rememberModalBottomSheetState()

    Box(modifier = Modifier.fillMaxSize()) {
        ARSceneView(
            selectedFurniture = selectedFurniture,
            modifier = Modifier.fillMaxSize()
        )

        FurnitureBottomSheet(
            furnitureItems = viewModel.furnitureItems,
            onFurnitureSelected = { viewModel.selectFurniture(it) },
            onDismiss = { viewModel.clearSelection() },
            sheetState = sheetState
        )
    }
}