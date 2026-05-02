package com.example.pruningapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pruningapp.viewmodel.PlantViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class EditableRule(val type: String = "", val start: String = "", val end: String = "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlantScreen(
    navController: NavController,
    plantId: Long,
    viewModel: PlantViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    val instructions: SnapshotStateList<String> = remember { mutableListOf<String>().toMutableStateList() }
    val rules: SnapshotStateList<EditableRule> = remember { mutableListOf<EditableRule>().toMutableStateList() }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(plantId) {
        if (initialized) return@LaunchedEffect
        val plant = viewModel.getPlantById(plantId) ?: return@LaunchedEffect
        val pruningRules = viewModel.getPruningRules(plantId)
        name = plant.name
        type = plant.type
        val parsed = runCatching {
            val t = object : TypeToken<List<String>>() {}.type
            Gson().fromJson<List<String>>(plant.instructions, t) ?: emptyList()
        }.getOrDefault(emptyList())
        instructions.addAll(parsed)
        pruningRules.forEach { rules.add(EditableRule(it.type, it.startMonthDay, it.endMonthDay)) }
        initialized = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edytuj roślinę") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (name.isBlank()) {
                            nameError = true
                            return@IconButton
                        }
                        val instructionsJson = Gson().toJson(
                            instructions.filter { it.isNotBlank() }
                        )
                        val validRules = rules
                            .filter { it.type.isNotBlank() && it.start.isNotBlank() && it.end.isNotBlank() }
                            .map { Triple(it.type.trim(), it.start.trim(), it.end.trim()) }
                        viewModel.saveEditedPlantById(
                            plantId = plantId,
                            newName = name.trim(),
                            newType = type.trim().ifBlank { "ogólna" },
                            instructionsJson = instructionsJson,
                            rules = validRules
                        )
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Zapisz")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            item {
                Text(
                    "Podstawowe informacje",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Nazwa rośliny *") },
                    isError = nameError,
                    supportingText = if (nameError) ({ Text("Nazwa jest wymagana") }) else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Typ (np. ozdobna, owocowa)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            item {
                Text(
                    "Instrukcje pielęgnacji",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            itemsIndexed(instructions) { index, instruction ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = instruction,
                        onValueChange = { instructions[index] = it },
                        label = { Text("Instrukcja ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = { instructions.removeAt(index) }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Usuń instrukcję",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { instructions.add("") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Dodaj instrukcję")
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            item {
                Text(
                    "Terminy cięcia",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            itemsIndexed(rules) { index, rule ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = rule.type,
                            onValueChange = { rules[index] = rule.copy(type = it) },
                            label = { Text("Nazwa terminu (np. główne, letnie)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = rule.start,
                                onValueChange = { rules[index] = rule.copy(start = it) },
                                label = { Text("Od (MM-dd)") },
                                placeholder = { Text("np. 03-01") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = rule.end,
                                onValueChange = { rules[index] = rule.copy(end = it) },
                                label = { Text("Do (MM-dd)") },
                                placeholder = { Text("np. 03-25") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        TextButton(
                            onClick = { rules.removeAt(index) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Usuń termin", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { rules.add(EditableRule()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Dodaj termin cięcia")
                }
            }

            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}
