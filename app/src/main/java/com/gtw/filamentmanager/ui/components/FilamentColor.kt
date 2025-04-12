package com.gtw.filamentmanager.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

@Composable
fun FilamentColor(color: Color, modifier: Modifier = Modifier) {
    val unspecifiedTextMeasurer = rememberTextMeasurer()
    val unspecifiedTextStyle = MaterialTheme.typography.titleMedium
    val unspecifiedText = "n/a"
    val textLayoutResult = remember(
        unspecifiedText
    ) {
        unspecifiedTextMeasurer.measure(unspecifiedText, unspecifiedTextStyle)
    }
    Canvas(
        modifier = modifier
            .size(48.dp)
            .padding(end = 8.dp)
    ) {
        drawCircle(color = Color.White)
        drawCircle(color = color, radius = (size.width - 4) / 2)
        if (color == Color.Unspecified) {
            drawText(
                text = unspecifiedText, textMeasurer = unspecifiedTextMeasurer,
                style = unspecifiedTextStyle,
                topLeft = Offset(
                    x = center.x - textLayoutResult.size.width / 2,
                    y = center.y - textLayoutResult.size.height / 2,
                )
            )
        }
    }
}