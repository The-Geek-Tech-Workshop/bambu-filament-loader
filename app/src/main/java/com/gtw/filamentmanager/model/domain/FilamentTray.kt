package com.gtw.filamentmanager.model.domain

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
sealed interface TrayLocation {
    fun name(): String
}

@Serializable
data object ExternalSpool : TrayLocation {
    override fun name(): String = "External Spool"
}

@Serializable
data class AMS(val unit: Int, val slot: Int) : TrayLocation {
    override fun name(): String = "AMS ${'A' + unit}${slot + 1}"
}

object ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Color", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeLong(value.value.toLong())
    }

    override fun deserialize(decoder: Decoder): Color {
        return Color(decoder.decodeLong().toULong())
    }
}

@Serializable
sealed interface FilamentTray {
    val location: TrayLocation
}

@Serializable
data class SpooledFilamentTray(
    override val location: TrayLocation,
    val tagUID: String,
    @SerialName("material_type")
    val type: String,
    @Serializable(with = ColorSerializer::class)
    val color: Color,
    val weight: Float,
    val diameter: Float,
    val temperature: Float,
    val time: Float,
    val bedTemperature: Float,
    val nozzleTemperatureMaximumInCelsius: Float,
    val nozzleTemperatureMinimumInCelsius: Float,
) : FilamentTray

@Serializable
data class EmptyFilamentTray(
    override val location: TrayLocation,
) : FilamentTray
