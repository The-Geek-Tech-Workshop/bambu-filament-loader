@file:OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)

package com.gtw.bambufilamentloader.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.gtw.bambufilamentloader.data.bambu.parseFilamentSpool
import com.gtw.bambufilamentloader.model.domain.DiscoveredPrinter
import com.gtw.bambufilamentloader.model.domain.FilamentSpool
import com.gtw.bambufilamentloader.ui.components.AccessCodeInputDialogue
import com.gtw.bambufilamentloader.ui.components.PrinterDetailsScreen
import com.gtw.bambufilamentloader.ui.components.PrinterListScreen
import com.gtw.bambufilamentloader.ui.navigation.DiscoveredPrinterNavType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject
import kotlin.reflect.typeOf
import kotlin.uuid.ExperimentalUuidApi

fun isNfcSupported(context: Context): Boolean {
    val packageManager = context.packageManager
    return packageManager.hasSystemFeature(PackageManager.FEATURE_NFC) &&
            packageManager.hasSystemFeature("com.nxp.mifare")
}

fun isNfcEnabled(context: Context): Boolean {
    return NfcAdapter.getDefaultAdapter(context)?.isEnabled == true
}

object Destinations {
    @Serializable
    object PrinterList

    @Serializable
    data class PrinterDetails(val printer: DiscoveredPrinter)

    @Serializable
    object AppSettings
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: PrinterViewModel by viewModels()

    @Inject
    lateinit var nfcAdapter: NfcAdapter

    private lateinit var pendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // NFC Setup
        pendingIntent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).let {
            PendingIntent.getActivity(
                this,
                0,
                it,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        enableEdgeToEdge()
        setContent {
            val printerState = viewModel.printerState.collectAsStateWithLifecycle().value
            val navController = rememberNavController()
            MaterialTheme {

                NavHost(
                    navController = navController,
                    startDestination = Destinations.PrinterList
                ) {
                    composable<Destinations.PrinterList> {
                        PrinterListScreen(
                            discoveredPrinters = printerState.discoveredPrinters,
                            onCardClick = { printer ->
                                navController.navigate(Destinations.PrinterDetails(printer))
                                viewModel.connectToPrinter(printer)
                            },
                            isRefreshing = printerState.printerSearchesInProgress.isNotEmpty(),
                            onRefresh = viewModel::findPrinters,
                            snackbarHostState = viewModel.snackbarHostState
                        )
                    }

                    composable<Destinations.PrinterDetails>(
                        typeMap = mapOf(
                            typeOf<DiscoveredPrinter>() to DiscoveredPrinterNavType
                        )
                    ) { backStackEntry ->
                        val printerDetails: Destinations.PrinterDetails = backStackEntry.toRoute()
                        PrinterDetailsScreen(
                            printer = printerDetails.printer,
                            filamentTrays = printerState.filamentTrays[printerDetails.printer],
                            onFilamentTrayCardClick = {
                                viewModel.setFilamentTrayToScanTo(printerDetails.printer to it)
                            },
                            filamentTraySelected = printerState.filamentTrayToScanTo?.second,
                            snackbarHostState = viewModel.snackbarHostState
                        )
                    }
                }
                printerState.accessCodeIsRequired?.let {
                    val printer = it.forPrinter
                    AccessCodeInputDialogue(
                        printer = printer,
                        onDismiss = {
                            viewModel.accessCodeRequestDismissed()
                            viewModel.setSelectedPrinter(null)
                        },
                        onConfirm = { accessCode ->
                            viewModel.updateAccessCodeForPrinter(
                                printer,
                                accessCode
                            )
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        nfcAdapter.disableForegroundDispatch(this)
        super.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        lifecycleScope.launch(Dispatchers.IO) {
            when (intent.action) {
//                NfcAdapter.ACTION_TECH_DISCOVERED -> {
                NfcAdapter.ACTION_TAG_DISCOVERED -> {
                    getTagFromIntent(intent)?.let { tag ->
                        if (tag.techList.contains("android.nfc.tech.MifareClassic")) {
                            try {
                                parseFilamentSpool(tag).getOrThrow().let { filamentSpool ->
                                    viewModel.newFilamentSpoolScanned(filamentSpool)
                                    viewModel.displayMessage("Scanned spool with id: ${filamentSpool.tagUID}")
                                }
                            } catch (e: Exception) {
                                viewModel.displayMessage("Problem parsing filament spool")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getTagFromIntent(intent: Intent): Tag? {
        return intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
    }

}


@Composable
fun NfcScreen(
    filament: FilamentSpool?
) {
    val context = LocalContext.current
    if (!isNfcSupported(context)) {
        Log.e("NFC", "NFC is not supported on this device")
    }
    if (!isNfcEnabled(context)) {
        Log.e("NFC", "NFC is not enabled on this device")
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (filament == null) {
            Text(
                text = "Ready to scan",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "UUID: ${filament.tagUID}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Material variant ID: ${filament.trayInfoIndex.materialVariantId}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Unique material ID: ${filament.trayInfoIndex.uniqueMaterialId}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Filament type: ${filament.filamentType} (${filament.detailedFilamentType.bambuName})",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Colour: ${filament.filamentColour.red}, ${filament.filamentColour.green}, ${filament.filamentColour.blue}, ${filament.filamentColour.alpha}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Weight: ${filament.weightInGrams}g",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Diameter: ${filament.filamentDiameterInMillimeters}mm",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Drying temperature: ${filament.dryingTemperatureInCelsius}°C",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Drying time: ${filament.dryingTime}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Bed temperature: ${filament.bedTemperatureInCelsius}°C",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Max temperature for hotend: ${filament.maxTemperatureForHotendInCelsius}°C",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Min temperature for hotend: ${filament.minTemperatureForHotendInCelsius}°C",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Spool width: ${filament.spoolWidthInMicroMeters}μm",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

        }
    }
}
