package com.qianrenni.reading.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import java.io.ByteArrayOutputStream

/**
 * 创建指定尺寸的纯白色图片，返回 PNG 格式的 ByteArray
 */
fun createWhiteImagePng(width: Int, height: Int): ByteArray {
    // 创建 ARGB_8888 格式的 Bitmap（支持透明度）
    val bitmap = createBitmap(width, height)

    // （也可用 bitmap.eraseColor(Color.WHITE)）
    bitmap.eraseColor(Color.WHITE)

    // 压缩为 PNG（无损）或 JPEG（有损，需指定质量）
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    // bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream) // JPEG 方案

    // 回收内存（重要！）
    bitmap.recycle()

    return outputStream.toByteArray()
}