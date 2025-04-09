@file:OptIn(ExperimentalMaterial3Api::class)

package com.gtw.bambufilamentloader.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gtw.bambufilamentloader.model.domain.DiscoveredPrinter

@Composable
fun PrinterListScreen(
    discoveredPrinters: List<DiscoveredPrinter>,
    onCardClick: (DiscoveredPrinter) -> Unit,
    snackbarHostState: SnackbarHostState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            PullToRefreshBox(
//                isRefreshing = isRefreshing,
//                onRefresh = onRefresh,
//            ) {
            discoveredPrinters.forEach { printer ->
                PrinterCard(
                    printer,
                    modifier = Modifier.padding(bottom = 12.dp),
                    onClick = { onCardClick(printer) }
                )
            }
//            }
        }
    }
}