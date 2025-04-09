package com.gtw.bambufilamentloader.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gtw.bambufilamentloader.model.domain.DiscoveredPrinter
import com.gtw.bambufilamentloader.model.domain.FilamentTray
import com.gtw.bambufilamentloader.ui.PrinterFilamentTrays

@ExperimentalMaterial3Api
@Composable
fun PrinterDetailsScreen(
    printer: DiscoveredPrinter,
    filamentTrays: PrinterFilamentTrays?,
    onFilamentTrayCardClick: (filamentTray: FilamentTray) -> Unit,
    filamentTraySelected: FilamentTray?,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("${printer.name}: ${filamentTraySelected?.location ?: "n/a"}")
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            filamentTrays?.let { trays ->
                ((trays.externalSpool?.let { listOf(it) }
                    ?: emptyList()) + trays.ams).forEach { filamentTray ->
                    FilamentTrayCard(
                        filamentTray = filamentTray,
                        modifier = Modifier.padding(bottom = 12.dp),
                        onClick = { onFilamentTrayCardClick(filamentTray) },
                        isSelected = filamentTray == filamentTraySelected
                    )
                }
            }
        }
    }
}