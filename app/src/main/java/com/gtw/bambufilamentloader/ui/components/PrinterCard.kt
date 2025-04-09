package com.gtw.bambufilamentloader.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gtw.bambufilamentloader.model.domain.DiscoveredPrinter

@Composable
fun PrinterCard(
    printer: DiscoveredPrinter,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier.width(300.dp),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Text(
                """
            ${printer.name}
            ${printer.model.modelName}
        """.trimIndent(),
                modifier = Modifier
                    .weight(5f),
            )
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Select Printer ${printer.name}",
                modifier = Modifier
                    .weight(1f)
            )
        }
    }
}
