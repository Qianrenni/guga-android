package com.qianrenni.reading.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.qianrenni.reading.data.model.ReadSettings
import com.qianrenni.reading.data.model.FontFamily as AppFontFamily

@Composable
fun ReadingSettingsDialog(
    settings: ReadSettings,
    onSettingsChange: (ReadSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var fontSize by remember { mutableIntStateOf(settings.fontSize) }
    var lineHeight by remember { mutableIntStateOf(settings.lineHeight) }
    var letterSpacing by remember { mutableIntStateOf(settings.letterSpacing) }
    var selectedFontFamily by remember { mutableStateOf(settings.fontFamily) }
    var textColor by remember { mutableStateOf(settings.textColor) }
    var backgroundColor by remember { mutableStateOf(settings.backgroundColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("阅读设置") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 字体大小
                SettingSection(title = "字体大小") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (fontSize > 12) {
                                fontSize--
                                onSettingsChange(settings.copy(fontSize = fontSize))
                            }
                        }) {
                            Text("-", fontSize = 20.sp)
                        }
                        Text("$fontSize", fontSize = 16.sp)
                        IconButton(onClick = {
                            if (fontSize < 32) {
                                fontSize++
                                onSettingsChange(settings.copy(fontSize = fontSize))
                            }
                        }) {
                            Text("+", fontSize = 20.sp)
                        }
                    }
                }

                // 行间距
                SettingSection(title = "行间距") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (lineHeight > 20) {
                                lineHeight -= 2
                                onSettingsChange(settings.copy(lineHeight = lineHeight))
                            }
                        }) {
                            Text("-", fontSize = 20.sp)
                        }
                        Text("$lineHeight", fontSize = 16.sp)
                        IconButton(onClick = {
                            if (lineHeight < 60) {
                                lineHeight += 2
                                onSettingsChange(settings.copy(lineHeight = lineHeight))
                            }
                        }) {
                            Text("+", fontSize = 20.sp)
                        }
                    }
                }

                // 字间距
                SettingSection(title = "字间距") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (letterSpacing > 0) {
                                letterSpacing--
                                onSettingsChange(settings.copy(letterSpacing = letterSpacing))
                            }
                        }) {
                            Text("-", fontSize = 20.sp)
                        }
                        Text("$letterSpacing", fontSize = 16.sp)
                        IconButton(onClick = {
                            if (letterSpacing < 10) {
                                letterSpacing++
                                onSettingsChange(settings.copy(letterSpacing = letterSpacing))
                            }
                        }) {
                            Text("+", fontSize = 20.sp)
                        }
                    }
                }

                // 字体选择
                SettingSection(title = "字体") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(AppFontFamily.entries.size) { index ->
                            val fontFamily = AppFontFamily.entries[index]
                            FilterChip(
                                selected = selectedFontFamily == fontFamily.value,
                                onClick = {
                                    selectedFontFamily = fontFamily.value
                                    onSettingsChange(settings.copy(fontFamily = fontFamily.value))
                                },
                                label = { Text(fontFamily.displayName) }
                            )
                        }
                    }
                }

                // 主题颜色
                SettingSection(title = "主题") {
                    val themes = listOf(
                        Triple(
                            "日间",
                            ReadSettings.DayTheme.textColor,
                            ReadSettings.DayTheme.backgroundColor
                        ),
                        Triple(
                            "夜间",
                            ReadSettings.NightTheme.textColor,
                            ReadSettings.NightTheme.backgroundColor
                        ),
                        Triple(
                            "护眼",
                            ReadSettings.EyeTheme.textColor,
                            ReadSettings.EyeTheme.backgroundColor
                        ),
                        Triple(
                            "纸张",
                            ReadSettings.PaperTheme.textColor,
                            ReadSettings.PaperTheme.backgroundColor
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        themes.forEach { (name, tc, bc) ->
                            ThemeButton(
                                name = name,
                                textColor = tc,
                                backgroundColor = bc,
                                isSelected = textColor == tc && backgroundColor == bc,
                                onClick = {
                                    textColor = tc
                                    backgroundColor = bc
                                    onSettingsChange(
                                        settings.copy(
                                            textColor = tc,
                                            backgroundColor = bc
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun SettingSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun ThemeButton(
    name: String,
    textColor: String,
    backgroundColor: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(backgroundColor.toColorInt()))
                .then(
                    if (isSelected) Modifier
                        .padding(2.dp)
                        .background(Color.Black.copy(alpha = 0.2f)) else Modifier
                )
        ) {
            Text(
                text = "A",
                color = Color(textColor.toColorInt()),
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = name, fontSize = 12.sp)
    }
}
