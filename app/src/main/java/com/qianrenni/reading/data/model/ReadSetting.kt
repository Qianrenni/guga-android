package com.qianrenni.reading.data.model


data class ReadSettings(
    val fontSize: Float = 18f,
    val lineHeight: Float = 30f,
    val letterSpacing: Float = 2f,
    val fontFamily: String = "default",
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

enum class FontFamily(val displayName: String, val value: String) {
    DEFAULT("默认", "default"),
    SERIF("宋体", "serif"),
    SANS_SERIF("黑体", "sans-serif"),
    MONOSPACE("等宽", "monospace"),
    KAITI("楷体", "kaiTi"),
    FANGSONG("仿宋", "fangSong"),
    YOUYUAN("幼圆", "youYuan"),
    LIshu("隶书", "liShu")
}