package com.otq.imageeditor.internal

import ja.burhanrashid52.photoeditor.TextStyleBuilder

/** 构造文字样式:颜色 + 可选对比阴影。 */
internal fun buildTextStyle(colorCode: Int, shadow: Boolean): TextStyleBuilder =
    TextStyleBuilder().apply {
        withTextColor(colorCode)
        if (shadow) withTextShadow(8f, 0f, 0f, contrastShadowColor(colorCode))
    }

/**
 * 纯函数:按文字亮度返回半透明对比阴影色。
 * 浅色文字 → 深色阴影;深色文字 → 浅色阴影。保证任意字色在任意底图上可读。
 */
internal fun contrastShadowColor(colorCode: Int): Int {
    val r = (colorCode shr 16) and 0xFF
    val g = (colorCode shr 8) and 0xFF
    val b = colorCode and 0xFF
    val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
    return if (luminance > 0.5) 0xCC000000.toInt() else 0xCCFFFFFF.toInt()
}
