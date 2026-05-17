package com.qianrenni.reading.data.store

import com.qianrenni.reading.data.api.BookService
import com.qianrenni.reading.data.model.Book

object BookStore {
    private val bookService = BookService
    private val booksCache = mutableMapOf<Int, Book>()
    private val categoryCursors = mutableMapOf<String, Int>()


    fun clear() {
        booksCache.clear()
        categoryCursors.clear()
    }
}