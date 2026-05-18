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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pruningapp.R
import com.example.pruningapp.data.EncyclopediaSpecies
import com.example.pruningapp.ui.components.MagazineCard
import com.example.pruningapp.ui.components.PlantCardItem
import com.example.pruningapp.viewmodel.PlantViewModel

// Ekran pobiera katalog z encyclopediaSpecies StateFlow — Room jest SSOT,
// nie ma już żadnej referencji do statycznego PlantDatabase.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerenualPlantsScreen(
    navController: NavController,
    plantViewModel: PlantViewModel = viewModel()
) {
    val plants by plantViewModel.allPlants.collectAsState()
    val cache by plantViewModel.pruningGuideCache.collectAsState()
    val encyclopediaSpecies by plantViewModel.encyclopediaSpecies.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_encyclopedia)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
                            contentDescription = stringResource(R.string.cd_info),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = stringResource(R.string.encyclopedia_info_tip),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            items(encyclopediaSpecies) { species ->
                val localPlant = plants.find {
                    it.name.equals(species.polishName, ignoreCase = true)
                }
                val cachedEntry = if (species.perenualId > 0) {
                    cache.find { it.perenualId == species.perenualId }
                } else null

                val imageUrl = localPlant?.wikiImageUrl
                    ?: localPlant?.apiImageUrl
                    ?: cachedEntry?.imageUrl

                MagazineCard(
                    item = species.toCardItem(imageUrl),
                    onClick = { navController.navigate("encyclopedia/${species.perenualId}") }
                )
            }
        }
    }
}

// Extension function: tworzy CardDisplayable z encji Room bez importowania komponentu
// MagazineCard do pakietu data — izolacja warstw zachowana.
private fun EncyclopediaSpecies.toCardItem(imageUrl: String?): PlantCardItem =
    PlantCardItem(
        title = polishName,
        subtitle = latinName,
        imageUrl = imageUrl,
        category = category
    )
