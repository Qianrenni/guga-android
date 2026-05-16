package com.qianrenni.reading.data.api

import com.qianrenni.reading.data.model.ApiResponse
import com.qianrenni.reading.data.model.BookReadingProgress
import com.qianrenni.reading.data.model.UpdateProgressRequest
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

object ReadingProgressService {
    suspend fun getReadingProgress(): ApiResponse<BookReadingProgress> {
        return NetworkClient.client.get("${NetworkClient.getBaseUrl()}user_reading_progress/get") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    suspend fun updateReadingProgress(
        request: UpdateProgressRequest
    ): ApiResponse<Nothing> {
        return NetworkClient.client.patch("${NetworkClient.getBaseUrl()}user_reading_progress/add") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteReadingProgress(
        bookId: Int
    ): ApiResponse<Nothing> {
        return NetworkClient.client.delete("${NetworkClient.getBaseUrl()}user_reading_progress/delete/$bookId") {
            contentType(ContentType.Application.Json)
        }.body()
    }
}