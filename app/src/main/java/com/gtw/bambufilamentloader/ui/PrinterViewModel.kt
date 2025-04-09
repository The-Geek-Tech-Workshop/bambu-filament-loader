@file:OptIn(ExperimentalUuidApi::class)

package com.gtw.bambufilamentloader.ui

import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gtw.bambufilamentloader.model.domain.AMS
import com.gtw.bambufilamentloader.model.domain.DiscoveredPrinter
import com.gtw.bambufilamentloader.model.domain.ExternalSpool
import com.gtw.bambufilamentloader.model.domain.FilamentSpool
import com.gtw.bambufilamentloader.model.domain.FilamentTray
import com.gtw.bambufilamentloader.model.domain.PrinterAuthenticationDetails
import com.gtw.bambufilamentloader.model.repos.ConnectedPrinter
import com.gtw.bambufilamentloader.model.repos.FilamentTraysUpdate
import com.gtw.bambufilamentloader.model.repos.FilamentTraysUpdateError
import com.gtw.bambufilamentloader.model.repos.PrinterAuthenticationDetailsRepo
import com.gtw.bambufilamentloader.model.repos.PrinterConnector
import com.gtw.bambufilamentloader.model.repos.PrinterDisconnected
import com.gtw.bambufilamentloader.model.repos.PrinterFound
import com.gtw.bambufilamentloader.model.repos.PrinterRepo
import com.gtw.bambufilamentloader.model.repos.PrinterSearchCompleted
import com.gtw.bambufilamentloader.model.repos.PrinterSearchStarted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

data class PrinterState(
    val discoveredPrinters: List<DiscoveredPrinter> = emptyList(),
    val selectedPrinter: DiscoveredPrinter? = null,
    val scannedFilament: FilamentSpool? = null,
    val accessCodeIsRequired: AccessCodeIsRequired? = null,
    val authenticationDetails: Map<DiscoveredPrinter, PrinterAuthenticationDetails> = emptyMap(),
    val filamentTrays: Map<DiscoveredPrinter, PrinterFilamentTrays> = emptyMap(),
    val printerSearchesInProgress: Set<Uuid> = emptySet(),
    val showPrinterDetails: Boolean = false,
    val filamentTrayToScanTo: Pair<DiscoveredPrinter, FilamentTray>? = null,
    val connectedPrinters: Map<DiscoveredPrinter, ConnectedPrinter> = emptyMap()
) {
    fun isPrinterConnected(printer: DiscoveredPrinter): Boolean =
        connectedPrinters.containsKey(printer)
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

    fun connectToPrinter(printer: DiscoveredPrinter) {
        if (_state.value.isPrinterConnected(printer)) return
        getAuthenticationDetails(printer)?.let { authenticationDetails ->
            viewModelScope.launch {
                printerConnector.connectPrinter(
                    printer = printer,
                    printerAuthenticationDetails = authenticationDetails
                ).also { connectedPrinter ->
                    _state.update { it.copy(connectedPrinters = it.connectedPrinters + (printer to connectedPrinter)) }
                }.apply {
                    launch {
                        events.collect { event ->
                            when (event) {
                                is FilamentTraysUpdate -> updateFilamentTrays(
                                    printer,
                                    PrinterFilamentTrays(
                                        externalSpool = event.trays.firstOrNull { it.location == ExternalSpool },
                                        ams = event.trays.filter { it.location is AMS }
                                    )
                                )

                                is FilamentTraysUpdateError -> displayMessage("Error updating filament tray data")
                                is PrinterDisconnected -> _state.update {
                                    _state.value.copy(
                                        connectedPrinters = _state.value.connectedPrinters - printer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } ?: _state.run {
            update { it.copy(accessCodeIsRequired = AccessCodeIsRequired(printer)) }
        }
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

    fun setFilamentTrayToScanTo(filamentTray: Pair<DiscoveredPrinter, FilamentTray>?) {
        _state.update { it.copy(filamentTrayToScanTo = filamentTray) }
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
        Log.d("PrinterViewModel", "New filament spool scanned: $filamentSpool")
        _state.value.filamentTrayToScanTo?.let { (printer, filamentTrayToScanTo) ->
            getAuthenticationDetails(printer)
                ?.let { authenticationDetails ->
                    _state.value.connectedPrinters[printer]?.let { connectedPrinter ->
                        viewModelScope.launch {
                            Log.d("PrinterViewModel", "Setting filament tray spool")
                            connectedPrinter.setFilamentTraySpool(
                                filamentSpool = filamentSpool,
                                filamentTray = filamentTrayToScanTo
                            )
                        }
                    }
                    _state.update { it.copy(filamentTrayToScanTo = null) }
                }
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