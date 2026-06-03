package com.otq.imageeditor.internal

/**
 * 把触摸点从 view 像素坐标线性映射到 bitmap 像素坐标(纯函数,可单测)。
 * 前提:overlay 与图片显示区完全重合(同宽高比),否则映射不准。
 * 返回 [x, y];view 尺寸非法时返回 [0, 0]。
 */
internal fun mapViewToBitmap(
    px: Float,
    py: Float,
    viewW: Int,
    viewH: Int,
    baseW: Int,
    baseH: Int,
): FloatArray {
    if (viewW <= 0 || viewH <= 0) return floatArrayOf(0f, 0f)
    return floatArrayOf(px / viewW * baseW, py / viewH * baseH)
}
