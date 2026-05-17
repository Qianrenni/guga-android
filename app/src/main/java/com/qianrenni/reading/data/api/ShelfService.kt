package com.qianrenni.reading.data.api

import com.qianrenni.reading.data.model.AddShelfRequest
import com.qianrenni.reading.data.model.ShelfItem
import io.ktor.client.request.setBody

object ShelfService {
    suspend fun getShelf(): NetworkResult<ShelfItem> {
        return NetworkClient.get("shelf/get")
    }

    suspend fun addToShelf(
        request: AddShelfRequest
    ): NetworkResult<Nothing> {
        return NetworkClient.post("shelf/add") {
            setBody(request)
        }
    }

    suspend fun removeFromShelf(
        bookId: Int
    ): NetworkResult<Nothing> {
        return NetworkClient.delete("shelf/delete/$bookId")
    }
}