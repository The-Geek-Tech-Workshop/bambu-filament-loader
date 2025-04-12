@file:OptIn(ExperimentalEncodingApi::class)

package com.gtw.filamentmanager.data

import android.content.Context
import com.gtw.filamentmanager.model.domain.DiscoveredPrinter
import com.gtw.filamentmanager.model.domain.PrinterAuthenticationDetails
import com.gtw.filamentmanager.model.repos.PrinterAuthenticationDetailsRepo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class PrinterAuthenticationDetailsRepoSharedPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) : PrinterAuthenticationDetailsRepo {

    private val base64 = Base64.Default

    override fun getAuthenticationDetails(printer: DiscoveredPrinter): PrinterAuthenticationDetails? =
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            .getString(printer.serialNumber, null)?.let {
                it.split(DETAILS_SEPARATOR).let { vals ->
                    PrinterAuthenticationDetails(
                        username = base64.decode(vals[0]).decodeToString(),
                        accessCode = base64.decode(vals[1]).decodeToString()
                    )
                }
            }

    override fun setAuthenticationDetails(
        printer: DiscoveredPrinter,
        authenticationDetails: PrinterAuthenticationDetails
    ) {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            .edit().run {
                putString(
                    printer.serialNumber, listOf(
                        base64.encode(authenticationDetails.username.encodeToByteArray()),
                        base64.encode(authenticationDetails.accessCode.encodeToByteArray())
                    ).joinToString(DETAILS_SEPARATOR)
                )
                apply()
            }
    }

    override fun clearAllAuthenticationDetails() {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            .edit().run {
                clear()
                apply()
            }
    }


    companion object {
        const val PREFERENCE_NAME = "printer_auth_details"
        const val DETAILS_SEPARATOR = ","
    }
}