package com.gtw.filamentmanager.ui.navigation

import com.gtw.filamentmanager.model.domain.TrayLocation
import com.gtw.filamentmanager.ui.PrinterState
import kotlinx.serialization.Serializable

enum class AppTabIndex(val tabIndex: Int) {
    PRINTERS(0),
    SCAN(1);
}

sealed interface Destination {
    companion object {
        @Serializable
        object PrinterList : Destination {
            override fun title(printerState: PrinterState): String = "Filament Manager"
            override fun tab(): AppTabIndex = AppTabIndex.PRINTERS
        }

        @Serializable
        data class PrinterDetails(val printerSerialNumber: String) : Destination {
            override fun title(printerState: PrinterState): String = printerState.discoveredPrinter(
                printerSerialNumber
            )?.name ?: printerSerialNumber

            override fun tab(): AppTabIndex = AppTabIndex.PRINTERS
        }

        @Serializable
        data class FilamentDetails(
            val printerSerialNumber: String,
            val filamentTrayLocation: TrayLocation
        ) :
            Destination {
            override fun title(printerState: PrinterState): String =
                printerState.discoveredPrinter(
                    printerSerialNumber
                )?.let { printer ->
                    printerState.filamentTray(
                        printer,
                        filamentTrayLocation
                    )?.let { tray ->
                        "${printer.name} - ${tray.location.name()}"
                    }
                }
                    ?: (printerSerialNumber + " - " + filamentTrayLocation.name())

            override fun tab(): AppTabIndex = AppTabIndex.PRINTERS
        }

        @Serializable
        object FilamentScanner : Destination {
            override fun title(printerState: PrinterState): String = "Filament Manager"
            override fun tab(): AppTabIndex = AppTabIndex.SCAN
        }

        @Serializable
        object AppSettings : Destination {
            override fun title(printerState: PrinterState): String = "Settings"
            override fun tab(): AppTabIndex = AppTabIndex.PRINTERS
        }
    }

    fun title(printerState: PrinterState): String
    fun tab(): AppTabIndex
}
