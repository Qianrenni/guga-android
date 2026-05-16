package com.qianrenni.reading.data.api

import com.qianrenni.reading.data.model.ReadEvent
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

object ReportService {
    suspend fun reportChapterRead(
        event: ReadEvent
    ) {
        NetworkClient.client.post("${NetworkClient.getBaseUrl()}statistic/book-chapter") {
            contentType(ContentType.Application.Json)
            setBody(event)
        }.body<Unit>()
    }
}