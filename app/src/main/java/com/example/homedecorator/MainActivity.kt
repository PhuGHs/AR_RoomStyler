package com.example.homedecorator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.homedecorator.data.repo.FurnitureRepository
import com.example.homedecorator.ui.ARFurnitureScreen
import com.example.homedecorator.ui.theme.HomeDecoratorTheme
import com.example.homedecorator.viewmodel.ArFurnitureViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ArFurnitureViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ArFurnitureViewModel(FurnitureRepository()) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeDecoratorTheme {
                ARFurnitureScreen(viewModel)
            }
        }
    }
}