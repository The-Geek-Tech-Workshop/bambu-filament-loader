package com.gtw.bambufilamentloader.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gtw.bambufilamentloader.model.domain.AMS
import com.gtw.bambufilamentloader.model.domain.EmptyFilamentTray
import com.gtw.bambufilamentloader.model.domain.ExternalSpool
import com.gtw.bambufilamentloader.model.domain.FilamentTray
import com.gtw.bambufilamentloader.model.domain.SpooledFilamentTray

@Composable
fun FilamentTrayCard(
    filamentTray: FilamentTray,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isSelected: Boolean
) {
    Card(
        modifier = modifier
            .width(300.dp),
        onClick = onClick,
        colors =
            if (isSelected) CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) else CardDefaults.cardColors()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            val location = filamentTray.location
            val locationText = when (location) {
                is ExternalSpool -> "External Spool"
                is AMS -> "AMS Unit ${location.unit + 1} Slot ${location.slot + 1}"
            }
            val descriptionText = when (filamentTray) {
                is EmptyFilamentTray -> "Empty"
                is SpooledFilamentTray -> filamentTray.type
            } + " ${if (isSelected) "Y" else "N"}"
            val colour = when (filamentTray) {
                is EmptyFilamentTray -> null
                is SpooledFilamentTray -> filamentTray.color
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier.weight(5f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(descriptionText)
                        colour?.let {
                            Spacer(modifier = Modifier.width(8.dp))
                            Canvas(modifier = Modifier.size(16.dp)) {
                                drawCircle(color = Color.White)
                                drawCircle(color = it, radius = (size.width - 4) / 2)
                            }
                        }
                    }
                    Row {
                        Text(locationText)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Select filament tray in location $locationText",
                    )
                }
            }
        }
    }
}
