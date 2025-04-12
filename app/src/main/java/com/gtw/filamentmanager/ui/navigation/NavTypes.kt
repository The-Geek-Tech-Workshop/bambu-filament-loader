package com.gtw.filamentmanager.ui.navigation

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import com.gtw.filamentmanager.model.domain.TrayLocation
import kotlinx.serialization.json.Json

inline fun <reified T> navType() = object : NavType<T>(
    isNullableAllowed = false
) {

    override fun parseValue(value: String): T =
        Json.decodeFromString(Uri.decode(value))

    override fun serializeAsValue(value: T): String =
        Uri.encode(Json.encodeToString(value))

    override fun get(
        bundle: Bundle,
        key: String
    ): T? =
        bundle.getString(key)?.let { Json.decodeFromString(it) }

    override fun put(
        bundle: Bundle,
        key: String,
        value: T
    ) {
        bundle.putString(key, Json.encodeToString(value))
    }

}

object NavTypes {
    val trayLocation = navType<TrayLocation>()
}