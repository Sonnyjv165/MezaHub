package com.example.mezahub.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mezahub.R

enum class MezaDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    LISTEN(route = "listen", labelRes = R.string.nav_listen, icon = Icons.Filled.Mic),
    HISTORY(route = "history", labelRes = R.string.nav_history, icon = Icons.Filled.History),
    POKEDEX(route = "pokedex", labelRes = R.string.nav_pokedex, icon = Icons.Filled.CatchingPokemon),
    SETTINGS(route = "settings", labelRes = R.string.nav_settings, icon = Icons.Filled.Settings),
}
