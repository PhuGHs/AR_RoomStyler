package com.example.homedecorator.ui

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.homedecorator.ui.component.ARSceneView
import com.example.homedecorator.ui.component.FurnitureBottomSheet
import com.example.homedecorator.ui.component.HDSnackbar
import com.example.homedecorator.viewmodel.ArFurnitureViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Objects

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ARFurnitureScreen(
    viewModel: ArFurnitureViewModel
) {
    val selectedFurniture by viewModel.selectedFurniture.collectAsState()
    val placedFurnitureList by viewModel.placedFurnitureList.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    
    var textEnabled by remember {
        mutableStateOf(false);
    }

    val noFurnitureItemSelected by remember {
        derivedStateOf { Objects.isNull(viewModel.selectedFurniture) }
    }

    val showGestureOverlay by remember {
        mutableStateOf(false)
    }

    val showCoachingOverlay by remember {
        mutableStateOf(true)
    }



    Box(modifier = Modifier.fillMaxSize()) {
        ARSceneView(
            selectedFurniture = selectedFurniture,
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize(),
            onError = { error ->
                Log.i("ARSceneView", "Error: ${error.message}")
            },
            onFurniturePlaced = { furniture ->
                viewModel.placeFurniture(furniture)
            },
            onInvalidPlane = {
                Log.i("Invalid plane", "Bug")
            }
        )

        if (noFurnitureItemSelected) {
            Log.i("ARFurnitureScreen","No")
            HDSnackbar()
        }

        FurnitureBottomSheet(
            viewModel = viewModel,
            onDismiss = {
                //do nothing
            },
            sheetState = sheetState
        )
        
        if (!noFurnitureItemSelected) {
            Text(text = "No furniture", modifier = Modifier.align(Alignment.Center))
        } else {
            Text(text = "Already have", modifier = Modifier.align(Alignment.Center))
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    Log.i("HD Button", "button clicked");
                    sheetState.show()
                    textEnabled = true;
                }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text(text = "Furniture List (${placedFurnitureList.size})")
        }
    }
}
