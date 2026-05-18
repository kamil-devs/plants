package com.example.pruningapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pruningapp.data.PlantDatabase
import com.example.pruningapp.ui.components.MagazineCard
import com.example.pruningapp.viewmodel.PlantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerenualPlantsScreen(
    navController: NavController,
    plantViewModel: PlantViewModel = viewModel()
) {
    val plants by plantViewModel.allPlants.collectAsState()
    val cache by plantViewModel.pruningGuideCache.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Poradnik pielęgnacji") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Szczegółowe porady przycinania z bazy Perenual. Kliknij roślinę, aby zobaczyć ekspertyzę.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            items(PlantDatabase.plants) { dbPlant ->
                val localPlant = plants.find { it.name.equals(dbPlant.polishName, ignoreCase = true) }
                val cachedEntry = if (dbPlant.perenualId > 0) {
                    cache.find { it.perenualId == dbPlant.perenualId }
                } else null

                MagazineCard(
                    title = dbPlant.polishName,
                    subtitle = dbPlant.latinName,
                    category = dbPlant.category,
                    imageUrl = localPlant?.wikiImageUrl ?: localPlant?.apiImageUrl ?: cachedEntry?.imageUrl,
                    onClick = { navController.navigate("encyclopedia/${dbPlant.perenualId}") }
                )
            }
        }
    }
}
