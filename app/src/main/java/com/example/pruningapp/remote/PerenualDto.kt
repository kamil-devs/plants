package com.example.pruningapp.remote

import com.google.gson.annotations.SerializedName

data class SpeciesDetails(
    @SerializedName("id") val id: Int,
    @SerializedName("common_name") val commonName: String,
    @SerializedName("scientific_name") val scientificName: List<String>?,
    @SerializedName("pruning_month") val pruningMonth: List<String>?,
    @SerializedName("pruning_count") val pruningCount: PruningCount?,
    @SerializedName("watering") val watering: String?,
    @SerializedName("maintenance") val maintenance: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("default_image") val defaultImage: DefaultImage?
)

data class DefaultImage(
    @SerializedName("original_url") val originalUrl: String?,
    @SerializedName("regular_url") val regularUrl: String?,
    @SerializedName("medium_url") val mediumUrl: String?,
    @SerializedName("small_url") val smallUrl: String?,
    @SerializedName("thumbnail") val thumbnail: String?
)

data class PruningCount(
    @SerializedName("amount") val amount: Int?,
    @SerializedName("interval") val interval: String?
)
