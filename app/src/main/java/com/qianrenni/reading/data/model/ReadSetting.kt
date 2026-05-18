package com.qianrenni.reading.data.model

import androidx.compose.ui.text.font.FontFamily


data class ReadSettings(
    val fontSize: Float = 18f,
    val lineHeight: Float = 30f,
    val letterSpacing: Float = 2f,
    val fontFamily: FontFamily = FontFamily.Default,
    val textColor: String = "#333333",
    val backgroundColor: String = "#ffffff"
) {
    companion object {
        val DayTheme = ReadSettings(
            textColor = "#333333",
            backgroundColor = "#ffffff"
        )
        val NightTheme = ReadSettings(
            textColor = "#b0b0b0",
            backgroundColor = "#1a1a1a"
        )
        val EyeTheme = ReadSettings(
            textColor = "#2d4a2d",
            backgroundColor = "#c7edcc"
        )
        val PaperTheme = ReadSettings(
            textColor = "#5b4636",
            backgroundColor = "#f5f0e1"
        )
    }
}

enum class ReadFontFamily(val displayName: String, val value: FontFamily) {
    Default("默认", FontFamily.Default),
    Serif("宋体", FontFamily.Serif),
    SansSerif("黑体", FontFamily.SansSerif),
    Monospace("等宽", FontFamily.Monospace),
    Cursive("手写", FontFamily.Cursive)
}