package com.qianrenni.reading.data.store

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.qianrenni.reading.data.model.Book

object BookStore {
    // 书籍缓存: Map<bookId, Book>
    val booksCache = mutableStateMapOf<Int, Book>()

    // 分类游标: Map<category, offset>
    val categoryCursors = mutableStateMapOf<String, Int>()

    // 分类是否已加载完毕: Map<category, isOver>
    val categoryOvers = mutableStateMapOf<String, Boolean>()

    // 按分类存储的书籍ID列表: Map<category, List<bookId>>
    val booksSplitCategoryMap = mutableStateMapOf<String, MutableList<Int>>()

    // 当前选中的分类
    var currentCategory = mutableStateOf("")

    // 所有分类列表
    var categories = mutableStateOf<List<String>>(emptyList())

    // 加载状态
    var isLoading = mutableStateOf(false)

    // 滚动位置
    var scrollToPosition = mutableStateOf(0f)

    // 每页加载数量
    const val LIMIT = 25

    fun clear() {
        booksCache.clear()
        categoryCursors.clear()
        categoryOvers.clear()
        booksSplitCategoryMap.clear()
        currentCategory.value = ""
        categories.value = emptyList()
        isLoading.value = false
        scrollToPosition.value = 0f
    }
}