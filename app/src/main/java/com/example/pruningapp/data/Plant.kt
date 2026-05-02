package com.example.pruningapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,
    val instructions: String,
    val owned: Boolean = false,
    val isUserAdded: Boolean = false,
    val pinned: Boolean = false,
    val harvestStart: String? = null,
    val harvestEnd: String? = null,
    val harvestAppearance: String? = null
)
