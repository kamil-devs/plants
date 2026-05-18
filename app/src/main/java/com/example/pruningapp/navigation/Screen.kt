package com.example.pruningapp.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Plants : Screen("plants")
    object Calendar : Screen("calendar")
    object Encyclopedia : Screen("encyclopedia")
    object Collections : Screen("collections")
    object Settings : Screen("settings")
    object Stats : Screen("stats")
    object AddPlant : Screen("add_plant")

    object PlantDetail : Screen("plant_detail/{plantId}") {
        fun route(plantId: Long) = "plant_detail/$plantId"
    }
    object EditPlant : Screen("edit_plant/{plantId}") {
        fun route(plantId: Long) = "edit_plant/$plantId"
    }
    object EncyclopediaDetail : Screen("encyclopedia/{id}") {
        fun route(id: Int) = "encyclopedia/$id"
    }
    object AddCollection : Screen("add_collection")
    object EditCollection : Screen("edit_collection/{collectionId}") {
        fun route(collectionId: Long) = "edit_collection/$collectionId"
    }
    object CollectionDetail : Screen("collection_detail/{collectionId}") {
        fun route(collectionId: Long) = "collection_detail/$collectionId"
    }
}
