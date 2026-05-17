package com.qianrenni.reading.data.api

import com.qianrenni.reading.data.model.Book
import com.qianrenni.reading.data.model.BookCategoryResponse
import com.qianrenni.reading.data.model.Catalog
import io.ktor.client.request.parameter

object BookService {
    suspend fun getCategories(): NetworkResult<BookCategoryResponse> {
        return NetworkClient.get("book/category")
    }

    suspend fun getBooksByCategory(
        category: String,
        offset: Int = 0,
        limit: Int = 25,
        sort: String = "id:-1"
    ): NetworkResult<Array<Book>> {
        return NetworkClient.get("book/select") {
            parameter("category", category)
            parameter("offset", offset)
            parameter("limit", limit)
            parameter("sort", sort)
        }
    }

    suspend fun searchBooks(
        query: String
    ): NetworkResult<Array<Book>> {
        return NetworkClient.get("book/search") {
            parameter("q", query)
        }
    }

    suspend fun getBookById(
        bookId: Int
    ): NetworkResult<Book> {
        return NetworkClient.get("book/$bookId")
    }

    suspend fun getCatalog(
        bookId: Int
    ): NetworkResult<Catalog> {
        return NetworkClient.get("book/toc/$bookId")
    }

    suspend fun getChapter(
        chapterId: Int,
        bookId: Int
    ): NetworkResult<String> {
        return NetworkClient.get("book/chapter/$chapterId") {
            parameter("book_id", bookId)
        }
    }

    suspend fun getRecommendations(
        query: String = "tags"
    ): NetworkResult<Array<Book>> {
        return NetworkClient.get("book/recommend") {
            parameter("query", query)
        }
    }

    suspend fun getBooksByIds(
        bookIds: String
    ): NetworkResult<Array<Book>> {
        return NetworkClient.get("book/list") {
            parameter("book_ids", bookIds)
        }
    }
}
