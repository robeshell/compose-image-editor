package com.otq.imageeditor.internal

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize

/**
 * 马赛克触摸层。只在马赛克工具激活时挂载,捕获拖拽并把 view 坐标线性映射到 bitmap 坐标。
 * 依赖外层把本 overlay 约束到与图片显示区域完全重合(同宽高比),映射才精确。
 */
@Composable
internal fun MosaicOverlay(
    controller: MosaicController,
    modifier: Modifier = Modifier,
) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    fun map(o: Offset): Offset {
        if (size.width == 0 || size.height == 0) return Offset.Zero
        return Offset(
            x = (o.x / size.width) * controller.baseWidth,
            y = (o.y / size.height) * controller.baseHeight,
        )
    }

    Box(
        modifier = modifier
            .onSizeChanged { size = it }
            .pointerInput(controller) {
                detectDragGestures(
                    onDragStart = { o -> map(o).let { controller.begin(it.x, it.y) } },
                    onDrag = { change, _ ->
                        change.consume()
                        map(change.position).let { controller.extend(it.x, it.y) }
                    },
                    onDragEnd = { controller.commit() },
                    onDragCancel = { controller.commit() },
                )
            }
    )
}
