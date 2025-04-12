@file:OptIn(ExperimentalUuidApi::class)

package com.gtw.filamentmanager.model.repos

import com.gtw.filamentmanager.model.domain.DiscoveredPrinter
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface PrinterSearchEvent
data class PrinterSearchStarted(val searchId: Uuid) :
    PrinterSearchEvent

data class PrinterSearchCompleted(val searchId: Uuid) :
    PrinterSearchEvent

data class PrinterFound(val printer: DiscoveredPrinter) : PrinterSearchEvent
interface PrinterRepo {

    val events: Flow<PrinterSearchEvent>

    suspend fun findPrinters()
}