package com.qianrenni.reading.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object NetworkClient {
    private const val BASE_URL = "http://49.235.107.221:8000/" // 请替换为实际的 API 基础 URL

    val client: HttpClient by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                })
            }

            install(Logging) {
                level = LogLevel.BODY
            }

            engine {
                connectTimeout = 30_000
                socketTimeout = 30_000
            }
        }
    }

    fun getBaseUrl(): String = BASE_URL

    /**
     * GET请求 - 模仿Web项目的get方法
     */
    suspend fun <T> get(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): NetworkResult<T> {
        val response: HttpResponse = client.get(BASE_URL + urlString) {
            contentType(ContentType.Application.Json)
            block()
        }
        return ResponseHandler.handleResponse(response)
    }

    /**
     * POST请求 - 模仿Web项目的post方法
     */
    suspend fun <T> post(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): NetworkResult<T> {
        val response: HttpResponse = client.post(BASE_URL + urlString) {
            contentType(ContentType.Application.Json)
            block()
        }
        return ResponseHandler.handleResponse(response)
    }

    /**
     * PUT请求 - 模仿Web项目的put方法
     */
    suspend fun <T> put(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): NetworkResult<T> {
        val response: HttpResponse = client.put(BASE_URL + urlString) {
            contentType(ContentType.Application.Json)
            block()
        }
        return ResponseHandler.handleResponse(response)
    }

    /**
     * DELETE请求 - 模仿Web项目的del方法
     */
    suspend fun <T> delete(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): NetworkResult<T> {
        val response: HttpResponse = client.delete(BASE_URL + urlString) {
            contentType(ContentType.Application.Json)
            block()
        }
        return ResponseHandler.handleResponse(response)
    }

    /**
     * PATCH请求 - 模仿Web项目的patch方法
     */
    suspend fun <T> patch(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): NetworkResult<T> {
        val response: HttpResponse = client.patch(BASE_URL + urlString) {
            contentType(ContentType.Application.Json)
            block()
        }
        return ResponseHandler.handleResponse(response)
    }
}
