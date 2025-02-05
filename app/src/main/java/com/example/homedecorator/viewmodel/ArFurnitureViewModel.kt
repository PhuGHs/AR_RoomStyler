package com.example.homedecorator.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homedecorator.data.model.Dimensions
import com.example.homedecorator.data.model.FurnitureItem
import com.example.homedecorator.data.repo.FurnitureRepository
import com.example.homedecorator.util.ModelDimensionsCalculator
import io.github.sceneview.loaders.ModelLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Objects

class ArFurnitureViewModel(
    private val repository: FurnitureRepository,
) : ViewModel() {
    private val _selectedFurniture = MutableStateFlow<FurnitureItem?>(repository.getFurnitureItems()[0])
    private val _placedFurnitureList = MutableStateFlow(listOf<FurnitureItem>())
    private val _furnitureItems = MutableStateFlow(repository.getFurnitureItems())

    val selectedFurniture: StateFlow<FurnitureItem?> = _selectedFurniture;
    val placedFurnitureList: StateFlow<List<FurnitureItem>> = _placedFurnitureList;
    val data: StateFlow<List<FurnitureItem>> = _furnitureItems;

    private var currentModelIndex = 0

    fun updateDimension(modelLoader: ModelLoader): List<FurnitureItem> {
        viewModelScope.launch {
            val calculator = ModelDimensionsCalculator(modelLoader)
            val itemsWithDimensions = repository.getFurnitureItems().map { item ->
                item.copy(
                    dimensions = calculator.calculateDimensions(item.modelResId)
                )
            }
            _furnitureItems.emit(itemsWithDimensions)
            _selectedFurniture.emit(itemsWithDimensions.firstOrNull())
        }
        return _furnitureItems.value
    }

    fun navigateToNextModel() {
        viewModelScope.launch {
            val modelsList = _furnitureItems.value
            if (modelsList.isNotEmpty()) {
                currentModelIndex = (currentModelIndex + 1) % modelsList.size
                _selectedFurniture.emit(modelsList[currentModelIndex])
            }
        }
    }

    fun navigateToPreviousModel() {
        viewModelScope.launch {
            val modelsList = _furnitureItems.value
            if (modelsList.isNotEmpty()) {
                currentModelIndex = if (currentModelIndex > 0) {
                    currentModelIndex - 1
                } else {
                    modelsList.size - 1
                }
                _selectedFurniture.emit(modelsList[currentModelIndex])
            }
        }
    }

    fun selectFurniture(item: FurnitureItem) {
        viewModelScope.launch {
            _selectedFurniture.emit(item)
            // Update currentModelIndex to match the selected item
            currentModelIndex = _furnitureItems.value.indexOf(item).coerceAtLeast(0)
        }
    }

    fun placeFurniture(item: FurnitureItem) {
        viewModelScope.launch {
            if (!placedFurnitureList.value.contains(item)) {
               _placedFurnitureList.emit(_placedFurnitureList.value + item)
               _selectedFurniture.emit(null)
            }
        }
    }

    fun removeFurniture(item: FurnitureItem) {
        viewModelScope.launch {
            if (!placedFurnitureList.value.contains(item)) {
                _placedFurnitureList.emit(_placedFurnitureList.value - item)
            }
        }
    }

    fun isSelected(id: Int): Boolean {
        return _selectedFurniture.value?.id == id
    }
}