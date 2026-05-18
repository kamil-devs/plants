package com.example.pruningapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pruningapp.ui.screens.AddEditCollectionScreen
import com.example.pruningapp.ui.screens.AddPlantScreen
import com.example.pruningapp.ui.screens.CalendarScreen
import com.example.pruningapp.ui.screens.CollectionDetailScreen
import com.example.pruningapp.ui.screens.CollectionsScreen
import com.example.pruningapp.ui.screens.DashboardScreen
import com.example.pruningapp.ui.screens.EditPlantScreen
import com.example.pruningapp.ui.screens.PlantDetailScreen
import com.example.pruningapp.ui.screens.PlantListScreen
import com.example.pruningapp.ui.screens.PerenualPlantDetailScreen
import com.example.pruningapp.ui.screens.PerenualPlantsScreen
import com.example.pruningapp.ui.screens.SettingsScreen
import com.example.pruningapp.ui.screens.StatsScreen
import com.example.pruningapp.ui.theme.PlantPruningTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        setContent {
            PlantPruningTheme {
                MainApp()
            }
        }
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
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun MainApp() {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem("dashboard", Icons.Default.Home, "Pulpit"),
        BottomNavItem("plants", Icons.Default.Eco, "Rośliny"),
        BottomNavItem("calendar", Icons.Default.CalendarMonth, "Kalendarz"),
        BottomNavItem("encyclopedia", Icons.Default.MenuBook, "Poradnik")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                modifier = Modifier.border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") { DashboardScreen(navController) }
            composable("plants") { PlantListScreen(navController) }
            composable("calendar") { CalendarScreen(navController) }
            composable("collections") { CollectionsScreen(navController) }
            composable("settings") { SettingsScreen() }

            composable("plant_detail/{plantId}") { backStackEntry ->
                val plantId = backStackEntry.arguments?.getString("plantId")?.toLongOrNull() ?: 0L
                PlantDetailScreen(navController, plantId)
            }
            composable("add_plant") { AddPlantScreen(navController) }
            composable("edit_plant/{plantId}") { backStackEntry ->
                val plantId = backStackEntry.arguments?.getString("plantId")?.toLongOrNull() ?: 0L
                EditPlantScreen(navController, plantId)
            }

            composable("stats") { StatsScreen(navController) }

            composable("encyclopedia") { PerenualPlantsScreen(navController) }
            composable("encyclopedia/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: return@composable
                PerenualPlantDetailScreen(navController, id)
            }

            composable("add_collection") {
                AddEditCollectionScreen(navController, collectionId = null)
            }
            composable("edit_collection/{collectionId}") { backStackEntry ->
                val collectionId = backStackEntry.arguments?.getString("collectionId")?.toLongOrNull() ?: 0L
                AddEditCollectionScreen(navController, collectionId = collectionId)
            }
            composable("collection_detail/{collectionId}") { backStackEntry ->
                val collectionId = backStackEntry.arguments?.getString("collectionId")?.toLongOrNull() ?: 0L
                CollectionDetailScreen(navController, collectionId)
            }
        }
    }
}
