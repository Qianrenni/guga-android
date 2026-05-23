package com.qianrenni.reading.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.qianrenni.reading.data.model.ReadFontFamily
import com.qianrenni.reading.data.model.ReadSettings

@Composable
fun ReadingSettings(
    settings: ReadSettings,
    onSettingsChange: (ReadSettings) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
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
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(ReadFontFamily.entries) { fontFamily ->
                Row(
                    Modifier
                        .selectable(
                            selected = (fontFamily.value == settings.fontFamily),
                            onClick = { onSettingsChange(settings.copy(fontFamily = fontFamily.value)) },
                            role = Role.RadioButton
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = fontFamily.value == settings.fontFamily,
                        onClick = null
                    )
                    Text(
                        text = fontFamily.displayName,
                    )
                }
            }
        }
    }
}