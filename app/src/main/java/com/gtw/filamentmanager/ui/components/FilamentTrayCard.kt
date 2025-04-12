package com.gtw.filamentmanager.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gtw.filamentmanager.model.domain.EmptyFilamentTray
import com.gtw.filamentmanager.model.domain.FilamentTray
import com.gtw.filamentmanager.model.domain.SpooledFilamentTray

@Composable
fun FilamentTrayCard(
    filamentTray: FilamentTray,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
            when (filamentTray) {
                is EmptyFilamentTray -> Color.Unspecified
                is SpooledFilamentTray -> filamentTray.color
            }.let { FilamentColor(it) }
            val descriptionText = when (filamentTray) {
                is EmptyFilamentTray -> "Empty"
                is SpooledFilamentTray -> filamentTray.type
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier.weight(5f)
                ) {
                    Row {
                        Text(
                            text = descriptionText,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Row {
                        Text(
                            text = filamentTray.location.name(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Select filament tray in location ${filamentTray.location.name()}",
                    )
                }
            }
        }
    }
}
