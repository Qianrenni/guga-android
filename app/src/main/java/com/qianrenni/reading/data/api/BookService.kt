package com.qianrenni.reading.data.api

import com.qianrenni.reading.data.model.ApiResponse
import com.qianrenni.reading.data.model.Book
import com.qianrenni.reading.data.model.BookCategoryResponse
import com.qianrenni.reading.data.model.Catalog
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

object BookService {
    suspend fun getCategories(): ApiResponse<BookCategoryResponse> {
        return NetworkClient.client.get("${NetworkClient.getBaseUrl()}book/category") {
        }.body()
    }

    suspend fun getBooksByCategory(
        category: String,
        offset: Int = 0,
        limit: Int = 25,
        sort: String = "id:-1"
    ): ApiResponse<Array<Book>> {
        return NetworkClient.client.get("${NetworkClient.getBaseUrl()}book/select") {
            parameter("category", category)
            parameter("offset", offset)
            parameter("limit", limit)
            parameter("sort", sort)
        }.body()
    }

    suspend fun searchBooks(
        query: String
    ): ApiResponse<Array<Book>> {
        return NetworkClient.client.get("${NetworkClient.getBaseUrl()}book/search") {
            parameter("q", query)
        }.body()
    }

    suspend fun getBookById(
        bookId: Int
    ): ApiResponse<Book> {
        return NetworkClient.client.get("${NetworkClient.getBaseUrl()}book/$bookId") {
        }.body()
    }

    suspend fun getCatalog(
        bookId: Int
    ): ApiResponse<Catalog> {
        return NetworkClient.client.get("${NetworkClient.getBaseUrl()}book/toc/$bookId") {
        }.body()
    }

    suspend fun getChapter(
        chapterId: Int,
        bookId: Int
    ): ApiResponse<String> {
        return NetworkClient.client.get("${NetworkClient.getBaseUrl()}book/chapter/$chapterId") {
            parameter("book_id", bookId)
        }.body()
    }

    suspend fun getRecommendations(
        query: String = "tags"
    ): ApiResponse<Array<Book>> {
        return NetworkClient.client.get("${NetworkClient.getBaseUrl()}book/recommend") {
            parameter("query", query)
        }.body()
    }

    suspend fun getBooksByIds(
        bookIds: String
    ): ApiResponse<Array<Book>> {
        return NetworkClient.client.get("${NetworkClient.getBaseUrl()}book/list") {
            parameter("book_ids", bookIds)
        }.body()
    }
}
