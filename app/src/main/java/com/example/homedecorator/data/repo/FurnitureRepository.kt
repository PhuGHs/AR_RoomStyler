package com.example.homedecorator.data.repo

import com.example.homedecorator.R
import com.example.homedecorator.data.model.FurnitureItem

class FurnitureRepository {
    fun getFurnitureItems(): List<FurnitureItem> = listOf(
        FurnitureItem(1, "Chair", R.drawable.chair, R.raw.chair),
        FurnitureItem(2, "Bed", R.drawable.bed, R.raw.bed),
        FurnitureItem(3, "Monitor", R.drawable.monitor, R.raw.monitor),
        FurnitureItem(4, "Standing Desk", R.drawable.desk, R.raw.desk),
        FurnitureItem(5, "Starbucks coffee", R.drawable.starbuck, R.raw.starbuck),
        FurnitureItem(6, "Tea cup", R.drawable.cup, R.raw.tea_cup),
    )
}