package com.qianrenni.reading.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BookReadingProgress(
    val book_id: Int,
    val last_chapter_id: Int,
    val last_position: Int = 0,
    val last_read_at: String = ""
)

@Serializable
data class UpdateProgressRequest(
    val book_id: Int,
    val last_chapter_id: Int,
    val last_position: Int = 0
)

@Serializable
data class ShelfItem(
    val book_id: Int,
    val created_at: String = "",
    // 合并阅读进度信息
    val last_chapter_id: Int? = null,
    val last_position: Int? = null,
    val last_read_at: String? = null
)

@Serializable
data class AddShelfRequest(
    val book_id: Int
)

@Serializable
data class ReadEvent(
    val book_id: Int,
    val chapter_id: Int,
    val event_type: String // "enter", "exit", "heartbeat"
)