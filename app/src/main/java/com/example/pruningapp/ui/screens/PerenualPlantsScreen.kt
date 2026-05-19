package com.example.pruningapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
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
import com.example.pruningapp.navigation.Screen
import com.example.pruningapp.ui.components.MagazineCard
import com.example.pruningapp.ui.components.PlantCardItem
import com.example.pruningapp.viewmodel.PlantViewModel

private fun aspectRatioFor(index: Int): Float = when (index % 3) {
    0 -> 0.68f
    1 -> 0.80f
    else -> 0.73f
}

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
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 96.dp),
            verticalItemSpacing = 10.dp,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
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

            itemsIndexed(encyclopediaSpecies) { index, species ->
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
                    aspectRatio = aspectRatioFor(index),
                    onClick = { navController.navigate(Screen.EncyclopediaDetail.route(species.perenualId)) }
                )
            }
        }
    }
}

private fun EncyclopediaSpecies.toCardItem(imageUrl: String?): PlantCardItem =
    PlantCardItem(
        title = polishName,
        subtitle = latinName,
        imageUrl = imageUrl,
        category = category
    )
