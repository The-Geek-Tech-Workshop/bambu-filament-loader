package com.gtw.bambufilamentloader.ui.navigation

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import com.gtw.bambufilamentloader.model.domain.DiscoveredPrinter
import kotlinx.serialization.json.Json


object DiscoveredPrinterNavType : NavType<DiscoveredPrinter>(
    isNullableAllowed = false
) {

    override fun parseValue(value: String): DiscoveredPrinter =
        Json.decodeFromString(Uri.decode(value))

    override fun serializeAsValue(value: DiscoveredPrinter): String =
        Uri.encode(Json.encodeToString(value))

    override fun get(
        bundle: Bundle,
        key: String
    ): DiscoveredPrinter? =
        bundle.getString(key)?.let { Json.decodeFromString(it) }

    override fun put(
        bundle: Bundle,
        key: String,
        value: DiscoveredPrinter
    ) {
        bundle.putString(key, Json.encodeToString(value))
    }

}