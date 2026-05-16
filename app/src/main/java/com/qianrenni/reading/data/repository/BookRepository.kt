package com.qianrenni.reading.data.repository

import com.qianrenni.reading.data.api.BookService
import com.qianrenni.reading.data.model.Book

object BookRepository {
    private val bookService = BookService
    private val booksCache = mutableMapOf<Int, Book>()
    private val categoryCursors = mutableMapOf<String, Int>()


    fun clear() {
        booksCache.clear()
        categoryCursors.clear()
    }
}