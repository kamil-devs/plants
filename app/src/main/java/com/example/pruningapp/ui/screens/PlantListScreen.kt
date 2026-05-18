package com.example.pruningapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pruningapp.data.Plant
import com.example.pruningapp.data.PlantDatabase
import com.example.pruningapp.ui.components.MagazineCard
import com.example.pruningapp.viewmodel.PlantViewModel

private val typeLabels = mapOf(
    "owocowa" to "Owocowe",
    "ozdobna" to "Ozdobne",
    "ozdobne drzewo" to "Ozdobne drzewa"
)

private fun Plant.categoryLabel(): String = typeLabels[type] ?: type.replaceFirstChar { it.uppercase() }

private fun Plant.hasPendingSync(): Boolean =
    owned && !apiDataSynced &&
            (perenualId != null || PlantDatabase.plants.any { it.polishName == name })

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantListScreen(
    navController: NavController,
    plantViewModel: PlantViewModel = viewModel()
) {
    val plants by plantViewModel.allPlants.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showUserOnly by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf<String?>(null) }
    val plantToDeleteState = remember { mutableStateOf<Plant?>(null) }

    val basePlants = remember(plants, showUserOnly) {
        if (showUserOnly) plants.filter { it.owned } else plants
    }

    val availableTypes = remember(basePlants) {
        basePlants.map { it.type }.distinct().sorted()
    }

    LaunchedEffect(availableTypes) {
        if (selectedType != null && selectedType !in availableTypes) {
            selectedType = null
        }
    }

    val filteredPlants = remember(basePlants, searchQuery, selectedType) {
        basePlants
            .let { list -> if (selectedType != null) list.filter { it.type == selectedType } else list }
            .let { list ->
                if (searchQuery.isBlank()) list
                else list.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.type.contains(searchQuery, ignoreCase = true)
                }
            }
    }

    // Przypniety na gorze, potem alfabetycznie.
    // Uzywamy 'plants' jako klucza remember, aby reagowac na zmiany pol wikiImageUrl
    val sortedPlants = remember(plants, filteredPlants) {
        filteredPlants.sortedWith(compareByDescending<Plant> { it.pinned }.thenBy { it.name })
    }

    plantToDeleteState.value?.let { plant ->
        AlertDialog(
            onDismissRequest = { plantToDeleteState.value = null },
            title = { Text("Usun rosline") },
            text = { Text("Czy na pewno chcesz usunac \"${plant.name}\"? Tej operacji nie mozna cofnac.") },
            confirmButton = {
                TextButton(onClick = {
                    plantViewModel.deletePlant(plant)
                    plantToDeleteState.value = null
                }) {
                    Text("Usun", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { plantToDeleteState.value = null }) {
                    Text("Anuluj")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moje rośliny") },
                actions = {
                    IconButton(onClick = { navController.navigate("collections") }) {
                        Icon(Icons.Default.Folder, contentDescription = "Kolekcje")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_plant") }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj rosline")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Szukaj roslin...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Wyczysc")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = !showUserOnly && selectedType == null,
                        onClick = {
                            showUserOnly = false
                            selectedType = null
                        },
                        label = { Text("Wszystkie") }
                    )
                }
                item {
                    FilterChip(
                        selected = showUserOnly,
                        onClick = {
                            showUserOnly = !showUserOnly
                            selectedType = null
                        },
                        label = { Text("Moje") }
                    )
                }
                items(availableTypes) { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = if (selectedType == type) null else type },
                        label = { Text(typeLabels[type] ?: type.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            when {
                plants.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Brak roslin", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Dodaj pierwsza rosline przyciskiem +",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                sortedPlants.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = when {
                                searchQuery.isNotBlank() -> "Brak wynikow dla: $searchQuery"
                                showUserOnly -> "Nie masz jeszcze posiadanych roslin"
                                else -> "Brak roslin w tej kategorii"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp)
                    ) {
                        gridItems(sortedPlants, key = { it.id }) { plant ->
                            MagazineCard(
                                title = plant.name,
                                subtitle = plant.categoryLabel(),
                                category = plant.categoryLabel(),
                                imageUrl = plant.wikiImageUrl ?: plant.apiImageUrl,
                                owned = plant.owned,
                                pinned = plant.pinned,
                                syncPending = plant.hasPendingSync(),
                                onClick = { navController.navigate("plant_detail/${plant.id}") },
                                onToggleOwned = { plantViewModel.toggleOwned(plant) },
                                onTogglePinned = { plantViewModel.togglePinned(plant) }
                            )
                        }
                    }
                }
            }
        }
    }
}
