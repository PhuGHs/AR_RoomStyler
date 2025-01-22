package com.example.homedecorator.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.homedecorator.data.model.FurnitureItem
import com.example.homedecorator.data.repo.FurnitureRepository

class ArFurnitureViewModel(
    private val repository: FurnitureRepository
): ViewModel() {
    private val _selectedFurniture = mutableStateOf<FurnitureItem?>(null)
    val selectedFurniture: State<FurnitureItem?> = _selectedFurniture
    val furnitureItems: List<FurnitureItem> = repository.getFurnitureItems()

    fun selectFurniture(item: FurnitureItem) {
        _selectedFurniture.value = item
    }

    fun clearSelection() {
        _selectedFurniture.value = null
    }
}