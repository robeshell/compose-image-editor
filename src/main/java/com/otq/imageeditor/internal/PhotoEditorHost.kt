package com.otq.imageeditor.internal

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView

/**
 * PhotoEditor 实例 + 承载它的 PhotoEditorView。提升到 Compose 状态,供工具栏驱动。
 */
@Stable
internal class PhotoEditorState(
    val view: PhotoEditorView,
    val editor: PhotoEditor,
)

/**
 * 创建并记住 PhotoEditorView/PhotoEditor。base 作为底图喂给 source ImageView。
 * setClipSourceImage(true):标注与导出都裁到图片区域,不溢出黑边。
 */
@Composable
internal fun rememberPhotoEditorState(base: Bitmap): PhotoEditorState {
    val context = LocalContext.current
    return remember(base) {
        val view = PhotoEditorView(context).apply {
            source.setImageBitmap(base)
        }
        val editor = PhotoEditor.Builder(context, view)
            .setPinchTextScalable(true)
            .setClipSourceImage(true)
            .build()
        PhotoEditorState(view, editor)
    }
}

/**
 * 把 PhotoEditorView 渲染进 Compose。
 */
@Composable
internal fun PhotoEditorCanvas(
    state: PhotoEditorState,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { state.view },
        modifier = modifier,
    )
}
