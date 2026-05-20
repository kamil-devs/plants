package com.example.pruningapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pruningapp.R
import com.example.pruningapp.data.Plant
import com.example.pruningapp.viewmodel.PlantViewModel

private data class KatalogCategory(val label: String, val types: Set<String>)

private val KATALOG_CATEGORIES = listOf(
    KatalogCategory("Wszystkie", emptySet()),
    KatalogCategory("Domowe", setOf("domowe")),
    KatalogCategory("Zioła", setOf("ziolowe")),
    KatalogCategory("Ozdobne", setOf("ozdobna", "ozdobne drzewo")),
    KatalogCategory("Owocowe", setOf("owocowe", "owocowa", "sad")),
    KatalogCategory("Iglaki", setOf("iglaki", "iglak"))
)

private fun Plant.katalogDrawable(): Int = when (type) {
    "ozdobna", "ozdobne drzewo" -> R.drawable.plant_ozdobna
    "iglaki", "iglak" -> R.drawable.plant_iglaki
    "owocowe", "owocowa", "sad" -> R.drawable.plant_owocowe
    "domowe" -> R.drawable.plant_domowe
    "ziolowe" -> R.drawable.plant_ziolowe
    else -> 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KatalogRoslinScreen(
    navController: NavController,
    plantViewModel: PlantViewModel = viewModel()
) {
    val allPlants by plantViewModel.allPlants.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(KATALOG_CATEGORIES[0]) }

    val catalogPlants = remember(allPlants, searchQuery, selectedCategory) {
        allPlants
            .filter { !it.isUserAdded }
            .filter { plant ->
                selectedCategory.types.isEmpty() || plant.type in selectedCategory.types
            }
            .filter { plant ->
                searchQuery.isBlank() ||
                    plant.name.contains(searchQuery, ignoreCase = true) ||
                    plant.botanicalName.contains(searchQuery, ignoreCase = true)
            }
            .sortedBy { it.name }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Katalog roślin") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Szukaj rośliny...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(50)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(KATALOG_CATEGORIES) { category ->
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = { selectedCategory = category },
                        label = { Text(category.label) }
                    )
                }
            }

            Text(
                text = "${catalogPlants.size} roślin",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(catalogPlants, key = { it.id }) { plant ->
                    KatalogPlantRow(
                        plant = plant,
                        onAdd = { plantViewModel.setOwned(plant.id, true) }
                    )
                }
            }
        }
    }
}

@Composable
private fun KatalogPlantRow(
    plant: Plant,
    onAdd: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val drawable = plant.katalogDrawable()
            if (drawable != 0) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2D5016))
                ) {
                    Image(
                        painter = painterResource(drawable),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plant.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (plant.botanicalName.isNotBlank()) {
                    Text(
                        text = plant.botanicalName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onAdd, enabled = !plant.owned) {
                Icon(
                    imageVector = if (plant.owned) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = if (plant.owned) "Dodano" else "Dodaj do moich roślin",
                    tint = if (plant.owned) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
