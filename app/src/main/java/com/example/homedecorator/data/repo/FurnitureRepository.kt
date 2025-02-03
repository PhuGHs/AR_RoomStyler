package com.example.homedecorator.data.repo

import com.example.homedecorator.R
import com.example.homedecorator.data.model.FurnitureItem

class FurnitureRepository {
    fun getFurnitureItems(): List<FurnitureItem> = listOf(
        FurnitureItem(1, "Chair", R.drawable.chair, R.raw.chair),
        FurnitureItem(3, "Monitor", R.drawable.monitor, R.raw.monitor),
        FurnitureItem(4, "Table", R.drawable.table, R.raw.table),
        FurnitureItem(5, "Starbucks coffee", R.drawable.starbuck, R.raw.starbuck),
        FurnitureItem(7, "Mugs", R.drawable.mugs, R.raw.mugs),
    )
}