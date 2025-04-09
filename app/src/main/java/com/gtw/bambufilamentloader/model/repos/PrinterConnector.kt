package com.gtw.bambufilamentloader.model.repos

import com.gtw.bambufilamentloader.model.domain.DiscoveredPrinter
import com.gtw.bambufilamentloader.model.domain.FilamentSpool
import com.gtw.bambufilamentloader.model.domain.FilamentTray
import com.gtw.bambufilamentloader.model.domain.PrinterAuthenticationDetails
import kotlinx.coroutines.flow.Flow

sealed interface BambuPrinterEvent
data class FilamentTraysUpdate(
    val trays: List<FilamentTray>,
) :
    BambuPrinterEvent

data object FilamentTraysUpdateError : BambuPrinterEvent

object PrinterDisconnected : BambuPrinterEvent

interface ConnectedPrinter {
    val events: Flow<BambuPrinterEvent>
    suspend fun requestAllPrinterData()
    suspend fun setFilamentTraySpool(
        filamentTray: FilamentTray,
        filamentSpool: FilamentSpool
    )
}

interface PrinterConnector {

    suspend fun connectPrinter(
        printer: DiscoveredPrinter,
        printerAuthenticationDetails: PrinterAuthenticationDetails,
    ): ConnectedPrinter
}