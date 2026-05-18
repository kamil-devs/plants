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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pruningapp.R
import com.example.pruningapp.data.Plant
import com.example.pruningapp.ui.components.MagazineCard
import com.example.pruningapp.ui.components.PlantCardItem
import com.example.pruningapp.viewmodel.PlantViewModel

private val typeLabels = mapOf(
    "owocowa" to "Owocowe",
    "ozdobna" to "Ozdobne",
    "ozdobne drzewo" to "Ozdobne drzewa"
)

private fun Plant.categoryLabel(): String =
    typeLabels[type] ?: type.replaceFirstChar { it.uppercase() }

private fun Plant.hasPendingSync(): Boolean =
    owned && !apiDataSynced && perenualId != null

private fun Plant.toCardItem(): PlantCardItem = PlantCardItem(
    title = name,
    subtitle = categoryLabel(),
    imageUrl = wikiImageUrl ?: apiImageUrl,
    category = categoryLabel()
)

// Alternating aspect ratios create natural stagger between columns.
private fun aspectRatioFor(index: Int): Float = when (index % 3) {
    0 -> 0.65f
    1 -> 0.82f
    else -> 0.72f
}

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
        if (selectedType != null && selectedType !in availableTypes) selectedType = null
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

    val sortedPlants = remember(plants, filteredPlants) {
        filteredPlants.sortedWith(
            compareByDescending<Plant> { it.pinned }.thenBy { it.name }
        )
    }

    plantToDeleteState.value?.let { plant ->
        AlertDialog(
            onDismissRequest = { plantToDeleteState.value = null },
            title = { Text(stringResource(R.string.dialog_delete_plant_title)) },
            text = { Text(stringResource(R.string.dialog_delete_plant_message, plant.name)) },
            confirmButton = {
                TextButton(onClick = {
                    plantViewModel.deletePlant(plant)
                    plantToDeleteState.value = null
                }) {
                    Text(
                        stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { plantToDeleteState.value = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_my_plants)) },
                actions = {
                    IconButton(onClick = { navController.navigate("collections") }) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = stringResource(R.string.cd_collections)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_plant") }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_add_plant)
                )
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
                placeholder = { Text(stringResource(R.string.search_plants_hint)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.cd_search))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear))
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
                        onClick = { showUserOnly = false; selectedType = null },
                        label = { Text(stringResource(R.string.filter_all)) }
                    )
                }
                item {
                    FilterChip(
                        selected = showUserOnly,
                        onClick = { showUserOnly = !showUserOnly; selectedType = null },
                        label = { Text(stringResource(R.string.filter_mine)) }
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
                            Text(
                                stringResource(R.string.state_no_plants),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.state_no_plants_hint),
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
                                searchQuery.isNotBlank() ->
                                    stringResource(R.string.state_no_results_for, searchQuery)
                                showUserOnly ->
                                    stringResource(R.string.state_no_owned_plants)
                                else ->
                                    stringResource(R.string.state_no_plants_in_category)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalItemSpacing = 12.dp,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp)
                    ) {
                        itemsIndexed(sortedPlants, key = { _, plant -> plant.id }) { index, plant ->
                            MagazineCard(
                                item = plant.toCardItem(),
                                aspectRatio = aspectRatioFor(index),
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
