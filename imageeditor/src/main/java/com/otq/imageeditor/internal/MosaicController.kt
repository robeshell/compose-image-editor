package com.otq.imageeditor.internal

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import androidx.compose.runtime.Stable
import kotlin.math.max

/**
 * 自研马赛克:把整图降采样再放大成"全马赛克版"作为 BitmapShader,
 * 沿手指路径描边即得像素块效果,结果烘进底图(经 [onBaked] 推给 PhotoEditor 的 source)。
 *
 * 拥有独立的撤销栈(按 base 重放),不依赖 PhotoEditor 的 undo。
 * 所有坐标均为 **bitmap 像素坐标**,由 overlay 负责把触摸点从 view 坐标映射过来。
 */
@Stable
internal class MosaicController(
    private val base: Bitmap,
    blockSize: Int,
    private val onBaked: (Bitmap) -> Unit,
    private val onStrokeCommitted: () -> Unit,
) {
    val baseWidth: Int = base.width
    val baseHeight: Int = base.height

    /** 描边粗细(bitmap 像素),与图像尺寸成比例,保证不同分辨率手感一致。 */
    val strokeWidth: Float = max(base.width * 0.08f, blockSize * 2f)

    private val mosaicSource: Bitmap = buildMosaic(base, blockSize)
    private val working: Bitmap = base.copy(Bitmap.Config.ARGB_8888, true)
    private val canvas = Canvas(working)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = BitmapShader(mosaicSource, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = this@MosaicController.strokeWidth
    }

    private val strokes = ArrayList<Path>()
    private var current: Path? = null
    private val seg = Path()
    private var lastX = 0f
    private var lastY = 0f

    fun begin(x: Float, y: Float) {
        current = Path().apply { moveTo(x, y) }
        lastX = x; lastY = y
        canvas.drawPoint(x, y, paint) // 起点画个圆点,点按也能出马赛克
        onBaked(working)
    }

    fun extend(x: Float, y: Float) {
        val c = current ?: return
        c.lineTo(x, y)
        seg.reset(); seg.moveTo(lastX, lastY); seg.lineTo(x, y)
        canvas.drawPath(seg, paint) // 增量画新段,避免每次全量重绘
        lastX = x; lastY = y
        onBaked(working)
    }

    fun commit() {
        val c = current ?: return
        strokes.add(c)
        current = null
        onStrokeCommitted()
    }

    /** 撤销最近一笔马赛克:清空后按 base 重放剩余笔画。 */
    fun undo() {
        strokes.removeLastOrNull()
        canvas.drawBitmap(base, 0f, 0f, null)
        for (p in strokes) canvas.drawPath(p, paint)
        onBaked(working)
    }

    val hasStrokes: Boolean get() = strokes.isNotEmpty()

    private fun buildMosaic(src: Bitmap, blockSize: Int): Bitmap {
        val sw = max(1, src.width / blockSize)
        val sh = max(1, src.height / blockSize)
        val small = Bitmap.createScaledBitmap(src, sw, sh, false) // 降采样
        return Bitmap.createScaledBitmap(small, src.width, src.height, false) // 放大成块状
    }
}
