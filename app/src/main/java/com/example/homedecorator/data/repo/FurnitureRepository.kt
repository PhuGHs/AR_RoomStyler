package com.example.homedecorator.data.repo

import com.example.homedecorator.R
import com.example.homedecorator.data.model.FurnitureItem

class FurnitureRepository {
    fun getFurnitureItems(): List<FurnitureItem> = listOf(
        FurnitureItem(1, "Bed", R.drawable.bed, R.raw.bed),
        FurnitureItem(2, "Chair", R.drawable.chair, R.raw.chair),
        FurnitureItem(3, "Monitor", R.drawable.monitor, R.raw.monitor)
    )
}