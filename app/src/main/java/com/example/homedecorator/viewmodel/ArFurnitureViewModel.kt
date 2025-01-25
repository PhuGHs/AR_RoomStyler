package com.example.homedecorator.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homedecorator.data.model.FurnitureItem
import com.example.homedecorator.data.repo.FurnitureRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArFurnitureViewModel(
    private val repository: FurnitureRepository
) : ViewModel() {
    private val _selectedFurniture = mutableStateOf<FurnitureItem?>(null)
    val selectedFurniture: State<FurnitureItem?> = _selectedFurniture

    private val _placedFurniture = MutableStateFlow<List<FurnitureItem>>(emptyList())
    val placedFurniture: StateFlow<List<FurnitureItem>> = _placedFurniture.asStateFlow()

    val furnitureItems: List<FurnitureItem> = repository.getFurnitureItems()

    fun selectFurniture(item: FurnitureItem) {
        _selectedFurniture.value = item
    }

    fun clearSelection() {
        _selectedFurniture.value = null
    }

    fun addPlacedFurniture(item: FurnitureItem) {
        viewModelScope.launch {
            _placedFurniture.emit(_placedFurniture.value + item)
        }
    }

    fun removePlacedFurniture(item: FurnitureItem) {
        viewModelScope.launch {
            _placedFurniture.emit(_placedFurniture.value.filter { it.id != item.id })
        }
    }
}