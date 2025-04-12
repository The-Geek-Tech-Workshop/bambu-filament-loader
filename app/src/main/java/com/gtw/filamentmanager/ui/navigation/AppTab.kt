package com.gtw.filamentmanager.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun AppTab(
    label: String,
    icon: ImageVector,
    appTab: AppTabIndex,
    currentDestination: Destination,
    onClick: () -> Unit,
) {
    Tab(
        selected = currentDestination.tab() == appTab,
        text = { Text(label) },
        icon = { Icon(icon, contentDescription = label) },
        onClick = onClick
    )
}