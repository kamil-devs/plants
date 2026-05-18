package com.example.pruningapp.remote

import com.google.gson.annotations.SerializedName

data class WikipediaResponse(
    @SerializedName("query") val query: WikiQuery?
)

data class WikiQuery(
    @SerializedName("pages") val pages: Map<String, WikiPage>?
)

data class WikiPage(
    @SerializedName("pageid") val pageId: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("thumbnail") val thumbnail: WikiThumbnail?
)

data class WikiThumbnail(
    @SerializedName("source") val source: String?,
    @SerializedName("width") val width: Int?,
    @SerializedName("height") val height: Int?
)
