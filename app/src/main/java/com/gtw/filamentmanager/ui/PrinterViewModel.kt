@file:OptIn(ExperimentalUuidApi::class)

package com.gtw.filamentmanager.ui

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gtw.filamentmanager.model.domain.AMS
import com.gtw.filamentmanager.model.domain.DiscoveredPrinter
import com.gtw.filamentmanager.model.domain.ExternalSpool
import com.gtw.filamentmanager.model.domain.FilamentSpool
import com.gtw.filamentmanager.model.domain.FilamentTray
import com.gtw.filamentmanager.model.domain.PrinterAuthenticationDetails
import com.gtw.filamentmanager.model.domain.TrayLocation
import com.gtw.filamentmanager.model.repos.ConnectedPrinter
import com.gtw.filamentmanager.model.repos.FilamentTraysUpdate
import com.gtw.filamentmanager.model.repos.FilamentTraysUpdateError
import com.gtw.filamentmanager.model.repos.NotAuthorizedException
import com.gtw.filamentmanager.model.repos.PrinterAuthenticationDetailsRepo
import com.gtw.filamentmanager.model.repos.PrinterConnector
import com.gtw.filamentmanager.model.repos.PrinterDisconnected
import com.gtw.filamentmanager.model.repos.PrinterFound
import com.gtw.filamentmanager.model.repos.PrinterRepo
import com.gtw.filamentmanager.model.repos.PrinterSearchCompleted
import com.gtw.filamentmanager.model.repos.PrinterSearchStarted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class AccessCodeIsRequired(val forPrinter: DiscoveredPrinter)

data class PrinterFilamentTrays(
    val externalSpool: FilamentTray?,
    val ams: List<FilamentTray>
)

sealed interface ScanDestination
data class ScanToFilamentTray(val printer: DiscoveredPrinter, val tray: FilamentTray) :
    ScanDestination

data object ScanToApp : ScanDestination

data class PrinterState(
    val discoveredPrinters: List<DiscoveredPrinter> = emptyList(),
    val selectedPrinter: DiscoveredPrinter? = null,
    val scannedFilament: FilamentSpool? = null,
    val accessCodeIsRequired: AccessCodeIsRequired? = null,
    val authenticationDetails: Map<DiscoveredPrinter, PrinterAuthenticationDetails> = emptyMap(),
    val filamentTrays: Map<DiscoveredPrinter, PrinterFilamentTrays> = emptyMap(),
    val printerSearchesInProgress: Set<Uuid> = emptySet(),
    val showPrinterDetails: Boolean = false,
    val scanDestination: ScanDestination? = null,
    val connectedPrinters: Map<DiscoveredPrinter, ConnectedPrinter> = emptyMap(),
    val connectedPrintersAwaitingRefresh: Set<DiscoveredPrinter> = emptySet()
) {
    fun isPrinterConnected(printer: DiscoveredPrinter): Boolean =
        connectedPrinters.containsKey(printer)

    fun isPrinterAwaitingRefresh(printer: DiscoveredPrinter): Boolean =
        connectedPrintersAwaitingRefresh.contains(printer)

    fun discoveredPrinter(serialNumber: String): DiscoveredPrinter? =
        discoveredPrinters.firstOrNull { it.serialNumber == serialNumber }

    fun filamentTray(printer: DiscoveredPrinter, trayLocation: TrayLocation): FilamentTray? =
        filamentTrays[printer]?.let { trayData ->
            when (trayLocation) {
                is ExternalSpool -> trayData.externalSpool
                is AMS -> trayData.ams.firstOrNull { it.location == trayLocation }
            }
        }
}

@HiltViewModel
class PrinterViewModel @Inject constructor(
    private val printerAuthenticationDetailsRepository: PrinterAuthenticationDetailsRepo,
    private val printerRepo: PrinterRepo,
    private val printerConnector: PrinterConnector,
) : ViewModel() {
    private val _state = MutableStateFlow(PrinterState())
    val printerState: StateFlow<PrinterState> = _state.asStateFlow()

    val snackbarHostState = SnackbarHostState()

    init {

        viewModelScope.launch {
            printerRepo.events.collect { event ->
                when (event) {
                    is PrinterSearchStarted -> _state.update {
                        it.copy(printerSearchesInProgress = it.printerSearchesInProgress + event.searchId)
                    }

                    is PrinterSearchCompleted -> _state.update {
                        it.copy(printerSearchesInProgress = it.printerSearchesInProgress - event.searchId)
                    }

                    is PrinterFound -> addPrinter(event.printer)
                }
            }
        }

        findPrinters()
    }

    private fun withConnectedPrinter(
        printer: DiscoveredPrinter,
        block: suspend ConnectedPrinter.() -> Unit
    ) {
        _state.value.connectedPrinters[printer]?.let { connectedPrinter ->
            viewModelScope.launch {
                connectedPrinter.block()
            }
        } ?: connectToPrinter(printer, block)
    }

    fun displayMessage(message: String) {
        viewModelScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    private fun addPrinter(printer: DiscoveredPrinter) {
        _state.update { currentState ->
            if (currentState.discoveredPrinters.none { it.serialNumber == printer.serialNumber }) {
                currentState.copy(discoveredPrinters = currentState.discoveredPrinters + printer)
            } else currentState
        }
    }

    private fun updateFilamentTrays(printer: DiscoveredPrinter, trayData: PrinterFilamentTrays) {
        _state.update { currentState ->
            currentState.copy(filamentTrays = currentState.filamentTrays + (printer to trayData))
        }
    }

    fun setSelectedPrinter(printer: DiscoveredPrinter?) {
        _state.update {
            it.copy(selectedPrinter = printer)
        }
    }

    private fun connectToPrinter(
        printer: DiscoveredPrinter,
        onConnected: suspend ConnectedPrinter.() -> Unit = {}
    ) {
        if (_state.value.isPrinterConnected(printer)) return
        _state.update { it.copy(connectedPrintersAwaitingRefresh = it.connectedPrintersAwaitingRefresh + printer) }
        getAuthenticationDetails(printer)?.let { authenticationDetails ->
            viewModelScope.launch {
                try {
                    printerConnector.connectPrinter(
                        printer = printer,
                        printerAuthenticationDetails = authenticationDetails
                    ).also { connectedPrinter ->
                        _state.update { it.copy(connectedPrinters = it.connectedPrinters + (printer to connectedPrinter)) }
                    }.apply {
                        launch {
                            events.transformWhile({ event ->
                                emit(event)
                                event !is PrinterDisconnected
                            }).collect { event ->
                                when (event) {
                                    is FilamentTraysUpdate -> {
                                        updateFilamentTrays(
                                            printer,
                                            PrinterFilamentTrays(
                                                externalSpool = event.trays.firstOrNull { it.location == ExternalSpool },
                                                ams = event.trays.filter { it.location is AMS }
                                            )
                                        )
                                        _state.update { it.copy(connectedPrintersAwaitingRefresh = it.connectedPrintersAwaitingRefresh - printer) }
                                    }

                                    is FilamentTraysUpdateError -> displayMessage("Error updating filament tray data")

                                    is PrinterDisconnected -> {
                                        _state.update {
                                            it.copy(
                                                connectedPrinters = it.connectedPrinters - printer,
                                                connectedPrintersAwaitingRefresh = it.connectedPrintersAwaitingRefresh - printer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        requestAllPrinterData()
                        onConnected()
                    }
                } catch (_: NotAuthorizedException) {
                    displayMessage("Access code is incorrect")
                    _state.update {
                        _state.value.copy(
                            accessCodeIsRequired = AccessCodeIsRequired(printer)
                        )
                    }
                }
            }
        } ?: _state.run {
            update { it.copy(accessCodeIsRequired = AccessCodeIsRequired(printer)) }
        }
    }

    fun refreshPrinterData(printer: DiscoveredPrinter) {
        _state.update { it.copy(connectedPrintersAwaitingRefresh = it.connectedPrintersAwaitingRefresh + printer) }
        withConnectedPrinter(printer) { requestAllPrinterData() }
    }

    fun updateAccessCodeForPrinter(printer: DiscoveredPrinter, accessCode: String) {
        val authenticationDetails =
            PrinterAuthenticationDetails(DEFAULT_BAMBU_LOCAL_USERNAME, accessCode)
        printerAuthenticationDetailsRepository.setAuthenticationDetails(
            printer,
            authenticationDetails
        )
        _state.update {
            it.copy(
                authenticationDetails = it.authenticationDetails + (printer to authenticationDetails)
            )
        }
        if (printer == printerState.value.accessCodeIsRequired?.forPrinter) {
            _state.update { it.copy(accessCodeIsRequired = null) }
            connectToPrinter(printer)
        }
    }

    fun accessCodeRequestDismissed() {
        _state.update { it.copy(accessCodeIsRequired = null) }
    }

    fun findPrinters() {
        if (_state.value.printerSearchesInProgress.isNotEmpty()) return
        viewModelScope.launch {
            printerRepo.findPrinters()
        }
    }

    fun setScanDestination(scanDestination: ScanDestination?) {
        _state.update { it.copy(scanDestination = scanDestination) }
    }

    fun clearPrinterAuthenticationDetails() {
        printerAuthenticationDetailsRepository.clearAllAuthenticationDetails()
        _state.update {
            it.copy(
                authenticationDetails = emptyMap(),
                filamentTrays = emptyMap()
            )
        }
    }

    fun newFilamentSpoolScanned(filamentSpool: FilamentSpool) {
        when (val scanDestination = _state.value.scanDestination) {
            is ScanToFilamentTray -> withConnectedPrinter(scanDestination.printer) {
                setFilamentTraySpool(
                    filamentSpool = filamentSpool,
                    filamentTray = scanDestination.tray
                )
            }

            is ScanToApp -> _state.update { it.copy(scannedFilament = filamentSpool) }
            else -> Unit
        }
    }

    private fun getAuthenticationDetails(printer: DiscoveredPrinter): PrinterAuthenticationDetails? =
        _state.value.authenticationDetails.getOrElse(printer) {
            printerAuthenticationDetailsRepository.getAuthenticationDetails(printer)
                ?.also { authenticationDetails ->
                    _state.update { it.copy(authenticationDetails = it.authenticationDetails + (printer to authenticationDetails)) }
                }
        }

    companion object {
        const val DEFAULT_BAMBU_LOCAL_USERNAME = "bblp"
    }

}