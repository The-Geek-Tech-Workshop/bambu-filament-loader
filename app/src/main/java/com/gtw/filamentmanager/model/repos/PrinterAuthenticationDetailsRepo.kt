package com.gtw.filamentmanager.model.repos

import com.gtw.filamentmanager.model.domain.DiscoveredPrinter
import com.gtw.filamentmanager.model.domain.PrinterAuthenticationDetails

interface PrinterAuthenticationDetailsRepo {

    fun getAuthenticationDetails(printer: DiscoveredPrinter): PrinterAuthenticationDetails?

    fun setAuthenticationDetails(
        printer: DiscoveredPrinter,
        authenticationDetails: PrinterAuthenticationDetails
    )

    fun clearAllAuthenticationDetails()

}