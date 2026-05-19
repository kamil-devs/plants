package com.example.pruningapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pruningapp.data.Plant
import com.example.pruningapp.navigation.Screen
import com.example.pruningapp.viewmodel.CollectionViewModel
import com.example.pruningapp.viewmodel.PlantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    navController: NavController,
    collectionId: Long,
    collectionViewModel: CollectionViewModel = viewModel(),
    plantViewModel: PlantViewModel = viewModel()
) {
    val collectionWithPlants by remember(collectionId) {
        collectionViewModel.getCollectionWithPlantsFlow(collectionId)
    }.collectAsState(initial = null)

    val allPlants by plantViewModel.allPlants.collectAsState()
    val plantIdsInCollection by remember(collectionId) {
        collectionViewModel.getPlantIdsForCollection(collectionId)
    }.collectAsState(initial = emptyList())

    var showPicker by remember { mutableStateOf(false) }
    var pickerSearch by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val collection = collectionWithPlants?.collection
    val plantsInCollection = collectionWithPlants?.plants ?: emptyList()
    val idsSet = plantIdsInCollection.toSet()

    if (showPicker) {
        ModalBottomSheet(
            onDismissRequest = { showPicker = false; pickerSearch = "" },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    "Dodaj rosliny do kolekcji",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = pickerSearch,
                    onValueChange = { pickerSearch = it },
                    placeholder = { Text("Szukaj...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.large
                )
                Spacer(Modifier.height(8.dp))

                val filteredForPicker = allPlants.filter {
                    pickerSearch.isBlank() || it.name.contains(pickerSearch, ignoreCase = true)
                }

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredForPicker, key = { it.id }) { plant ->
                        val isInCollection = plant.id in idsSet
                        PlantPickerRow(
                            plant = plant,
                            isChecked = isInCollection,
                            onToggle = {
                                collectionViewModel.togglePlantInCollection(plant.id, collectionId, isInCollection)
                            }
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(collection?.name ?: "Kolekcja") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróc")
                    }
                },
                actions = {
                    if (collection != null) {
                        IconButton(onClick = { navController.navigate(Screen.EditCollection.route(collection.id)) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edytuj")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj rosliny")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
        ) {
            if (collection != null && (collection.description.isNotBlank() || collection.type.isNotBlank())) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (collection.type.isNotBlank()) {
                                Text(
                                    text = collection.type,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (collection.description.isNotBlank()) {
                                Text(
                                    text = collection.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Rosliny w kolekcji (${plantsInCollection.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (plantsInCollection.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Brak roslin. Dodaj przyciskiem +",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(plantsInCollection, key = { it.id }) { plant ->
                    CollectionPlantRow(
                        plant = plant,
                        onClick = { navController.navigate(Screen.PlantDetail.route(plant.id)) },
                        onRemove = {
                            collectionViewModel.togglePlantInCollection(plant.id, collectionId, true)
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun CollectionPlantRow(
    plant: Plant,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clickable { onClick() }
        ) {
            Icon(
                Icons.Default.Eco,
                contentDescription = null,
                tint = if (plant.owned) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = plant.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = plant.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Usun z kolekcji",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PlantPickerRow(
    plant: Plant,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isChecked, onCheckedChange = { onToggle() })
        Icon(
            Icons.Default.Eco,
            contentDescription = null,
            tint = if (plant.owned) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(text = plant.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(text = plant.type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
