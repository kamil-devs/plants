package com.example.pruningapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pruningapp.data.Plant
import com.example.pruningapp.data.PruningRule
import com.example.pruningapp.viewmodel.PlantViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    navController: NavController,
    plantId: Long,
    viewModel: PlantViewModel = viewModel()
) {
    var plant by remember { mutableStateOf<Plant?>(null) }
    var pruningRules by remember { mutableStateOf<List<PruningRule>>(emptyList()) }

    LaunchedEffect(plantId) {
        plant = viewModel.getPlantById(plantId)
        pruningRules = viewModel.getPruningRules(plantId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plant?.name ?: "Szczegóły rośliny") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        val currentPlant = plant
        if (currentPlant != null) {
            val instructions: List<String> = remember(currentPlant.instructions) {
                runCatching {
                    val type = object : TypeToken<List<String>>() {}.type
                    Gson().fromJson<List<String>>(currentPlant.instructions, type)
                }.getOrDefault(emptyList())
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = currentPlant.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Typ: ${currentPlant.type}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                if (pruningRules.isNotEmpty()) {
                    item {
                        Text(
                            "Terminy cięcia",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(pruningRules.size) { index ->
                        val rule = pruningRules[index]
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    rule.type.replaceFirstChar { it.uppercase() },
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    "${rule.startMonthDay} – ${rule.endMonthDay}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                if (instructions.isNotEmpty()) {
                    item {
                        Text(
                            "Instrukcje",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    itemsIndexed(instructions) { index, instruction ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "${index + 1}.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    instruction,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            if (index < instructions.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
