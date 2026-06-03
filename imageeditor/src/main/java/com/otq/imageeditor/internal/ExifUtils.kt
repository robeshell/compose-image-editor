package com.otq.imageeditor.internal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayInputStream

/**
 * 解码图片并按 EXIF 方向转正。模块自带一份,不依赖宿主的发送链路。
 * 同时做降采样,避免大图 OOM。
 */
internal object ExifUtils {

    fun loadOrientedBitmap(context: Context, uri: Uri, maxSize: Int = 2560): Bitmap {
        val raw = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("无法读取图片: $uri")

        // 先量尺寸算降采样
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(raw, 0, raw.size, bounds)
        var sample = 1
        val longest = maxOf(bounds.outWidth, bounds.outHeight)
        while (longest / sample > maxSize) sample *= 2

        val decoded = BitmapFactory.decodeByteArray(
            raw, 0, raw.size,
            BitmapFactory.Options().apply { inSampleSize = sample }
        ) ?: error("无法解码图片: $uri")

        val orientation = runCatching {
            ExifInterface(ByteArrayInputStream(raw))
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        return applyExif(decoded, orientation)
    }

    private fun applyExif(bmp: Bitmap, orientation: Int): Bitmap {
        val t = exifTransform(orientation)
        if (t.isIdentity) return bmp
        val m = Matrix()
        if (t.rotationDegrees != 0f) m.postRotate(t.rotationDegrees)
        if (t.flipX) m.postScale(-1f, 1f)
        if (t.flipY) m.postScale(1f, -1f)
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
    }
}

/** EXIF 方向对应的几何变换(纯数据,无 Android 依赖,可单测)。 */
internal data class ExifTransform(
    val rotationDegrees: Float,
    val flipX: Boolean,
    val flipY: Boolean,
) {
    val isIdentity: Boolean get() = rotationDegrees == 0f && !flipX && !flipY
}

/**
 * 把 EXIF orientation(1..8)映射为旋转角 + 翻转。
 * 注:ExifInterface.ORIENTATION_* 为编译期常量,会被内联,故本函数在纯 JVM 下亦可运行。
 */
internal fun exifTransform(orientation: Int): ExifTransform = when (orientation) {
    ExifInterface.ORIENTATION_ROTATE_90 -> ExifTransform(90f, false, false)
    ExifInterface.ORIENTATION_ROTATE_180 -> ExifTransform(180f, false, false)
    ExifInterface.ORIENTATION_ROTATE_270 -> ExifTransform(270f, false, false)
    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> ExifTransform(0f, true, false)
    ExifInterface.ORIENTATION_FLIP_VERTICAL -> ExifTransform(0f, false, true)
    ExifInterface.ORIENTATION_TRANSPOSE -> ExifTransform(90f, true, false)
    ExifInterface.ORIENTATION_TRANSVERSE -> ExifTransform(270f, true, false)
    else -> ExifTransform(0f, false, false)
}
