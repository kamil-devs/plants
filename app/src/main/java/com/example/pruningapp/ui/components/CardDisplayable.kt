package com.example.pruningapp.ui.components

interface CardDisplayable {
    val title: String
    val subtitle: String
    val imageUrl: String?
    val category: String
    val localDrawableResId: Int
}

data class PlantCardItem(
    override val title: String,
    override val subtitle: String,
    override val imageUrl: String?,
    override val category: String,
    override val localDrawableResId: Int = 0
) : CardDisplayable
