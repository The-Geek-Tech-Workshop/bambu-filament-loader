package com.gtw.bambufilamentloader.model.repos

import com.gtw.bambufilamentloader.model.domain.DiscoveredPrinter
import com.gtw.bambufilamentloader.model.domain.PrinterAuthenticationDetails

interface PrinterAuthenticationDetailsRepo {

    fun getAuthenticationDetails(printer: DiscoveredPrinter): PrinterAuthenticationDetails?

    fun setAuthenticationDetails(
        printer: DiscoveredPrinter,
        authenticationDetails: PrinterAuthenticationDetails
    )

    fun clearAllAuthenticationDetails()

}