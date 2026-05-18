package com.example.pruningapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.pruningapp.data.PlantDatabase
import com.example.pruningapp.viewmodel.PlantViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val polishMonths = listOf(
    "stycznia", "lutego", "marca", "kwietnia", "maja", "czerwca",
    "lipca", "sierpnia", "września", "października", "listopada", "grudnia"
)

private fun formatMonthDay(monthDay: String): String {
    return try {
        val parts = monthDay.split("-")
        "${parts[1].toInt()} ${polishMonths[parts[0].toInt() - 1]}"
    } catch (e: Exception) {
        monthDay
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    navController: NavController,
    plantId: Long,
    viewModel: PlantViewModel = viewModel()
) {
    val plant by remember(plantId) { viewModel.getPlantByIdFlow(plantId) }.collectAsState(initial = null)
    val pruningRules by remember(plantId) { viewModel.getPruningRulesFlow(plantId) }.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plant?.name ?: "Szczegóły rośliny") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    if (plant?.isUserAdded == true) {
                        IconButton(onClick = { navController.navigate("edit_plant/$plantId") }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edytuj")
                        }
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

            val syncPending = currentPlant.owned
                && !currentPlant.apiDataSynced
                && (currentPlant.perenualId != null
                    || PlantDatabase.plants.any { it.polishName == currentPlant.name })

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Hero: API image jeśli dostępne, inaczej gradient
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        if (!currentPlant.apiImageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = currentPlant.apiImageUrl,
                                contentDescription = currentPlant.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            0f to Color.Transparent,
                                            0.5f to Color.Transparent,
                                            1f to Color(0xCC000000)
                                        )
                                    )
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF1B5E20), Color(0xFF2D5A27), Color(0xFF4CAF50))
                                        )
                                    )
                            )
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(120.dp)
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 24.dp),
                                tint = Color.White.copy(alpha = 0.12f)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Text(
                                text = currentPlant.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = currentPlant.type.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                        }
                    }
                }

                // Sync status — widoczny gdy dane są pobierane w tle
                if (syncPending) {
                    item {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Pobieranie danych pielęgnacyjnych w tle...",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (pruningRules.isNotEmpty()) {
                    item {
                        Text(
                            "Terminy cięcia",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    items(pruningRules) { rule ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ContentCut,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        rule.type.replaceFirstChar { it.uppercase() },
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Text(
                                    "${formatMonthDay(rule.startMonthDay)} – ${formatMonthDay(rule.endMonthDay)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                if (currentPlant.harvestStart != null) {
                    item {
                        Text(
                            "Zbiory",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Termin zbioru",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        "${formatMonthDay(currentPlant.harvestStart)} – ${formatMonthDay(currentPlant.harvestEnd ?: "")}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                if (!currentPlant.harvestAppearance.isNullOrBlank()) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
                                    Text(
                                        "Jak wyglądają gotowe owoce:",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        currentPlant.harvestAppearance,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                if (instructions.isNotEmpty()) {
                    item {
                        Text(
                            "Instrukcje cięcia",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                instructions.forEachIndexed { index, instruction ->
                                    TimelineStep(
                                        number = index + 1,
                                        text = instruction,
                                        isLast = index == instructions.size - 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Sekcja danych z Perenual API (lokalnie zapisane)
                if (currentPlant.apiDataSynced) {
                    item {
                        Text(
                            "Pielęgnacja (Perenual)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (!currentPlant.apiWatering.isNullOrBlank()
                                    || !currentPlant.apiMaintenance.isNullOrBlank()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        if (!currentPlant.apiWatering.isNullOrBlank()) {
                                            ApiChip(
                                                label = "Podlewanie",
                                                value = translateWatering(currentPlant.apiWatering),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (!currentPlant.apiMaintenance.isNullOrBlank()) {
                                            ApiChip(
                                                label = "Pielęgnacja",
                                                value = translateMaintenance(currentPlant.apiMaintenance),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                if (!currentPlant.apiDescription.isNullOrBlank()) {
                                    if (!currentPlant.apiWatering.isNullOrBlank()
                                        || !currentPlant.apiMaintenance.isNullOrBlank()
                                    ) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                                        )
                                    }
                                    Text(
                                        text = currentPlant.apiDescription,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = "Źródło: perenual.com",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun ApiChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TimelineStep(number: Int, text: String, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$number",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f),
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 8.dp else 16.dp)
        )
    }
}

private fun translateWatering(value: String): String = when (value.lowercase()) {
    "minimum"   -> "Minimalne"
    "average"   -> "Umiarkowane"
    "frequent"  -> "Częste"
    "maximum"   -> "Intensywne"
    else        -> value
}

private fun translateMaintenance(value: String): String = when (value.lowercase()) {
    "low"      -> "Niska"
    "moderate" -> "Umiarkowana"
    "high"     -> "Wysoka"
    else       -> value
}
