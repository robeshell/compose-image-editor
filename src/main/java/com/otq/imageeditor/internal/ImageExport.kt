package com.otq.imageeditor.internal

import android.content.Context
import android.graphics.Bitmap
import com.otq.imageeditor.OutputSpec
import ja.burhanrashid52.photoeditor.SaveSettings
import java.io.File

/** 按 [OutputSpec] 决定输出文件(默认落到 cacheDir/imageeditor)。 */
internal fun buildOutputFile(context: Context, output: OutputSpec): File {
    val dir = output.targetDir ?: File(context.cacheDir, "imageeditor").apply { mkdirs() }
    if (!dir.exists()) dir.mkdirs()
    val ext = when (output.format) {
        Bitmap.CompressFormat.PNG -> "png"
        Bitmap.CompressFormat.JPEG -> "jpg"
        else -> "webp"
    }
    return File(dir, "edited_${System.currentTimeMillis()}.$ext")
}

/** 由 [OutputSpec] 构造 PhotoEditor 的保存设置。保留所有图层,PNG 才开透明。 */
internal fun buildSaveSettings(output: OutputSpec): SaveSettings =
    SaveSettings.Builder()
        .setClearViewsEnabled(false)
        .setTransparencyEnabled(output.format == Bitmap.CompressFormat.PNG)
        .setCompressFormat(output.format)
        .setCompressQuality(output.quality)
        .build()
