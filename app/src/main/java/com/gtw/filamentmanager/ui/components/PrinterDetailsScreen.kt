package com.gtw.filamentmanager.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gtw.filamentmanager.model.domain.DiscoveredPrinter
import com.gtw.filamentmanager.model.domain.FilamentTray
import com.gtw.filamentmanager.ui.PrinterFilamentTrays

@ExperimentalMaterial3Api
@Composable
fun PrinterDetailsScreen(
    modifier: Modifier = Modifier,
    printer: DiscoveredPrinter,
    filamentTrays: PrinterFilamentTrays?,
    onFilamentTrayCardClick: (filamentTray: FilamentTray) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {

        filamentTrays?.let { trays ->
            LazyColumn {
                items((trays.externalSpool?.let { listOf(it) }
                    ?: emptyList()) + trays.ams) { filamentTray ->
                    FilamentTrayCard(
                        filamentTray = filamentTray,
                        modifier = Modifier.padding(bottom = 12.dp, start = 8.dp, end = 8.dp),
                        onClick = { onFilamentTrayCardClick(filamentTray) },
                    )
                }
            }
        }
    }
}