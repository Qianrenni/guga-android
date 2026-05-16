package com.qianrenni.reading.data.api

import com.qianrenni.reading.data.model.AddShelfRequest
import com.qianrenni.reading.data.model.ApiResponse
import com.qianrenni.reading.data.model.ShelfItem
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

object ShelfService {
    suspend fun getShelf(): ApiResponse<ShelfItem> {
        return NetworkClient.client.get("${NetworkClient.getBaseUrl()}shelf/get") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    suspend fun addToShelf(
        request: AddShelfRequest
    ): ApiResponse<Nothing> {
        return NetworkClient.client.post("${NetworkClient.getBaseUrl()}shelf/add") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun removeFromShelf(
        bookId: Int
    ): ApiResponse<Nothing> {
        return NetworkClient.client.delete("${NetworkClient.getBaseUrl()}shelf/delete/$bookId") {
            contentType(ContentType.Application.Json)
        }.body()
    }
}