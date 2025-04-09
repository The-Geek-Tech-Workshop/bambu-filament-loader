package com.gtw.bambufilamentloader.model.domain

import androidx.compose.ui.graphics.Color

sealed interface TrayLocation
data object ExternalSpool : TrayLocation
data class AMS(val unit: Int, val slot: Int) : TrayLocation

sealed interface FilamentTray {
    val location: TrayLocation
}

data class SpooledFilamentTray(
    override val location: TrayLocation,
    val tagUID: String,
    val type: String,
    val color: Color,
    val weight: Float,
    val diameter: Float,
    val temperature: Float,
    val time: Float,
    val bedTemperature: Float,
    val nozzleTemperatureMaximumInCelsius: Float,
    val nozzleTemperatureMinimumInCelsius: Float,
) : FilamentTray

data class EmptyFilamentTray(
    override val location: TrayLocation,
) : FilamentTray
