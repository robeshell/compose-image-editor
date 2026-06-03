package com.otq.imageeditor

import android.graphics.Bitmap
import java.io.File

/**
 * 编辑产物的输出规格。
 */
data class OutputSpec(
    val format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    /** 0..100,仅 JPEG/WEBP 有意义 */
    val quality: Int = 95,
    /** 输出目录;null = 模块内部 cacheDir/imageeditor */
    val targetDir: File? = null,
)
