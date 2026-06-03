package com.otq.imageeditor

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.otq.imageeditor.internal.AnnotateStage
import com.otq.imageeditor.internal.CropStage

/**
 * 编辑器入口(主)。宿主把它放进自己的导航/浮层即可。
 *
 * 契约:接收 [ImageEditorRequest],编辑结束通过 [onResult] 回吐
 * [ImageEditorResult]。模块对发送、聊天、宿主业务一无所知。
 *
 * 两阶段状态机:
 *  1. 裁剪前置阶段(CropStage,可在 cropper 内取消=跳过)
 *  2. 标注阶段(AnnotateStage:涂鸦/马赛克/文字/贴纸 + 导出)
 *
 * 便捷的 ActivityResultContract 包装将在 Composable 成形后再补(可选)。
 */
@Composable
fun ImageEditorScreen(
    request: ImageEditorRequest,
    onResult: (ImageEditorResult) -> Unit,
) {
    val needCrop = request.config.cropFirst && EditTool.CROP in request.config.tools

    // baseUri 为 null 表示尚未确定标注底图(还在裁剪阶段)
    var baseUri by remember {
        mutableStateOf<Uri?>(if (needCrop) null else request.sourceUri)
    }

    val base = baseUri
    if (base == null) {
        CropStage(source = request.sourceUri, onResolved = { baseUri = it })
    } else {
        AnnotateStage(baseUri = base, request = request, onResult = onResult)
    }
}
