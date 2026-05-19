package com.example.pruningapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pruningapp.viewmodel.CollectionViewModel

private val collectionTypePresets = listOf(
    "balkon",
    "ogrod przed domem",
    "ogrod za domem",
    "szklarnia",
    "ogrod zimowy",
    "sad"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCollectionScreen(
    navController: NavController,
    collectionId: Long?,
    viewModel: CollectionViewModel = viewModel()
) {
    val isEdit = collectionId != null

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    val collections by viewModel.allCollectionsWithPlants.collectAsState()

    LaunchedEffect(collectionId) {
        if (isEdit && !initialized) {
            collections.find { it.collection.id == collectionId }?.collection?.let { c ->
                name = c.name
                description = c.description
                type = c.type
                initialized = true
            }
        }
    }

    // Retry load after collections arrive
    LaunchedEffect(collections) {
        if (isEdit && !initialized) {
            collections.find { it.collection.id == collectionId }?.collection?.let { c ->
                name = c.name
                description = c.description
                type = c.type
                initialized = true
            }
        }
    }

    fun save() {
        if (name.isBlank()) { nameError = true; return }
        viewModel.saveCollection(
            id = collectionId,
            name = name.trim(),
            description = description.trim(),
            type = type.trim()
        )
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edytuj kolekcje" else "Nowa kolekcja") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróc")
                    }
                },
                actions = {
                    IconButton(onClick = { save() }) {
                        Icon(Icons.Default.Check, contentDescription = "Zapisz", modifier = Modifier.size(24.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text("Nazwa kolekcji *") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError,
                supportingText = if (nameError) { { Text("Nazwa jest wymagana") } } else null,
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Opis (opcjonalny)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Column {
                Text(
                    "Typ miejsca",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(collectionTypePresets) { preset ->
                        FilterChip(
                            selected = type == preset,
                            onClick = { type = if (type == preset) "" else preset },
                            label = { Text(preset) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Lub wpisz wlasny typ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("np. taras, piwnica, parapet") }
                )
            }
        }
    }
}
