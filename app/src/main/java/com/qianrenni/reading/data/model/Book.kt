package com.qianrenni.reading.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Book(
    val id: Int,
    val name: String,
    val author: String,
    val cover: String,
    val description: String = "",
    val category: String = "",
    val tags: String = "",
    val total_chapter: Int = 0,
    val created_at: String = "",
    val is_ended: Boolean = false,
    val words_cnt: Int = 0
)

@Serializable
data class Catalog(
    val id: Int,
    val title: String,
    val word_count: Int = 0,
    val order: Double = 0.0
)

@Serializable
data class Chapter(
    val id: Int,
    val title: String,
    val content: String,
    val book_id: Int,
    val word_count: Int = 0,
    val order: Double = 0.0
)

// 后端可能返回的是字符串数组 ["category1", "category2"] 而不是对象数组
// 使用自定义类型适配器或者直接使用 String
@Serializable
data class BookCategoryResponse(
    val categories: List<String> = emptyList()
)