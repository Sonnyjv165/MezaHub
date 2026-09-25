package com.example.mezahub.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mezahub.navigation.MezaDestination
import com.example.mezahub.ui.screens.HistoryScreen
import com.example.mezahub.ui.screens.ListenScreen
import com.example.mezahub.ui.screens.PokedexScreen
import com.example.mezahub.ui.screens.SettingsScreen

@Composable
fun MezaHubApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { MezaBottomBar(navController) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MezaDestination.LISTEN.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(MezaDestination.LISTEN.route) { ListenScreen() }
            composable(MezaDestination.HISTORY.route) { HistoryScreen() }
            composable(MezaDestination.POKEDEX.route) { PokedexScreen() }
            composable(MezaDestination.SETTINGS.route) { SettingsScreen() }
        }
    }
}

@Composable
private fun MezaBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        MezaDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(stringResource(destination.labelRes)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}
