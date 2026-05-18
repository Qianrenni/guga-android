package com.qianrenni.reading.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.qianrenni.reading.data.model.ReadSettings

@Composable
fun ReadingSettings(
    settings: ReadSettings,
    onSettingsChange: (ReadSettings) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("字体大小")
            Slider(
                value = settings.fontSize,
                onValueChange = {
                    onSettingsChange(
                        settings.copy(
                            fontSize = it,
                            lineHeight = settings.lineHeight + it - settings.fontSize
                        )
                    )
                },
                valueRange = 12f..28f
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("行高")
            Slider(
                value = settings.lineHeight,
                onValueChange = {
                    onSettingsChange(settings.copy(lineHeight = it))
                },
                valueRange = settings.fontSize..settings.fontSize * 2
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("字间距")
            Slider(
                value = settings.letterSpacing,
                onValueChange = {
                    onSettingsChange(settings.copy(letterSpacing = it))
                },
                valueRange = 0.5f..4f
            )

        }
    }
}