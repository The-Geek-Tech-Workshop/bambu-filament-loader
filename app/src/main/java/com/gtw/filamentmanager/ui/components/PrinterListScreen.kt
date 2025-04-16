@file:OptIn(ExperimentalMaterial3Api::class)

package com.gtw.filamentmanager.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gtw.filamentmanager.model.domain.DiscoveredPrinter

@Composable
fun PrinterListScreen(
    modifier: Modifier = Modifier,
    discoveredPrinters: List<DiscoveredPrinter>,
    onCardClick: (DiscoveredPrinter) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
    ) {
        if (discoveredPrinters.isEmpty() && !isRefreshing) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No printers found", style = MaterialTheme.typography.titleMedium)
                IconButton(
                    onClick = onRefresh
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh printers")
                }
            }
        } else {
            LazyColumn {
                items(discoveredPrinters) { printer ->
                    PrinterCard(
                        printer,
                        modifier = Modifier.padding(bottom = 12.dp, start = 8.dp, end = 8.dp),
                        onClick = { onCardClick(printer) }
                    )
                }
            }
        }
    }
}