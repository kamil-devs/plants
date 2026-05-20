package com.example.pruningapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pruningapp.navigation.Screen
import com.example.pruningapp.ui.components.FloatingPillNav
import com.example.pruningapp.ui.components.PillNavItem
import com.example.pruningapp.ui.screens.AddEditCollectionScreen
import com.example.pruningapp.ui.screens.AddPlantScreen
import com.example.pruningapp.ui.screens.CalendarScreen
import com.example.pruningapp.ui.screens.CollectionDetailScreen
import com.example.pruningapp.ui.screens.CollectionsScreen
import com.example.pruningapp.ui.screens.DashboardScreen
import com.example.pruningapp.ui.screens.KatalogRoslinScreen
import com.example.pruningapp.ui.screens.EditPlantScreen
import com.example.pruningapp.ui.screens.PerenualPlantDetailScreen
import com.example.pruningapp.ui.screens.PerenualPlantsScreen
import com.example.pruningapp.ui.screens.PlantDetailScreen
import com.example.pruningapp.ui.screens.PlantListScreen
import com.example.pruningapp.ui.screens.SettingsScreen
import com.example.pruningapp.ui.screens.StatsScreen
import com.example.pruningapp.ui.theme.PlantPruningTheme

class MainActivity : ComponentActivity() {

    private var pendingPlantIdState = mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readPlantIdFromIntent(intent)
        requestNotificationPermission()
        setContent {
            PlantPruningTheme {
                MainApp(
                    pendingPlantId = pendingPlantIdState.value,
                    onPendingPlantHandled = { pendingPlantIdState.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readPlantIdFromIntent(intent)
    }

    private fun readPlantIdFromIntent(intent: Intent?) {
        val id = intent?.getLongExtra(EXTRA_PLANT_ID, -1L) ?: -1L
        if (id > 0L) pendingPlantIdState.value = id
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    companion object {
        const val EXTRA_PLANT_ID = "plant_id"
    }
}

private val pillNavItems = listOf(
    PillNavItem(Screen.Dashboard.route, Icons.Default.Home, "Pulpit"),
    PillNavItem(Screen.Plants.route, Icons.Default.Eco, "Rośliny"),
    PillNavItem(Screen.Calendar.route, Icons.Default.CalendarMonth, "Kalendarz"),
    PillNavItem(Screen.Encyclopedia.route, Icons.AutoMirrored.Filled.MenuBook, "Poradnik")
)

private val pillNavRoutes = pillNavItems.map { it.route }.toSet()

@Composable
fun MainApp(
    pendingPlantId: Long? = null,
    onPendingPlantHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.hierarchy
        ?.firstOrNull { dest -> pillNavRoutes.any { it == dest.route } }
        ?.route

    val showPillNav = navBackStackEntry?.destination?.route in pillNavRoutes

    LaunchedEffect(pendingPlantId) {
        val id = pendingPlantId ?: return@LaunchedEffect
        navController.navigate(Screen.PlantDetail.route(id)) {
            launchSingleTop = true
        }
        onPendingPlantHandled()
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(bottom = if (showPillNav) 80.dp else 0.dp)
            ) {
                composable(Screen.Dashboard.route) { DashboardScreen(navController) }
                composable(Screen.Plants.route) { PlantListScreen(navController) }
                composable(Screen.Calendar.route) { CalendarScreen(navController) }
                composable(Screen.Collections.route) { CollectionsScreen(navController) }
                composable(Screen.Settings.route) { SettingsScreen(navController) }

                composable(Screen.PlantDetail.route) { backStackEntry ->
                    val plantId = backStackEntry.arguments?.getString("plantId")?.toLongOrNull() ?: 0L
                    PlantDetailScreen(navController, plantId)
                }
                composable(Screen.AddPlant.route) { AddPlantScreen(navController) }
                composable(Screen.EditPlant.route) { backStackEntry ->
                    val plantId = backStackEntry.arguments?.getString("plantId")?.toLongOrNull() ?: 0L
                    EditPlantScreen(navController, plantId)
                }

                composable(Screen.Stats.route) { StatsScreen(navController) }

                composable(Screen.Encyclopedia.route) { PerenualPlantsScreen(navController) }
                composable(Screen.EncyclopediaDetail.route) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: return@composable
                    PerenualPlantDetailScreen(navController, id)
                }

                composable(Screen.AddCollection.route) {
                    AddEditCollectionScreen(navController, collectionId = null)
                }
                composable(Screen.EditCollection.route) { backStackEntry ->
                    val collectionId = backStackEntry.arguments?.getString("collectionId")?.toLongOrNull() ?: 0L
                    AddEditCollectionScreen(navController, collectionId = collectionId)
                }
                composable(Screen.CollectionDetail.route) { backStackEntry ->
                    val collectionId = backStackEntry.arguments?.getString("collectionId")?.toLongOrNull() ?: 0L
                    CollectionDetailScreen(navController, collectionId)
                }
                composable(Screen.Katalog.route) {
                    KatalogRoslinScreen(navController)
                }
            }

            AnimatedVisibility(
                visible = showPillNav,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                FloatingPillNav(
                    items = pillNavItems,
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
