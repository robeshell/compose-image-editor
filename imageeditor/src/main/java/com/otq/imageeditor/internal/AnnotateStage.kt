package com.otq.imageeditor.internal

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.otq.imageeditor.ImageEditorRequest
import com.otq.imageeditor.ImageEditorResult
import com.otq.imageeditor.R
import ja.burhanrashid52.photoeditor.PhotoEditor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 标注阶段(主舞台):加载底图 → PhotoEditor 桥接 → 工具栏(涂鸦/文字/贴纸) + (任务6)马赛克 + (任务7)导出。
 */
@Composable
internal fun AnnotateStage(
    baseUri: Uri,
    request: ImageEditorRequest,
    onResult: (ImageEditorResult) -> Unit,
) {
    val context = LocalContext.current
    var failure by remember { mutableStateOf<Throwable?>(null) }

    val baseBitmap by produceState<Bitmap?>(initialValue = null, baseUri) {
        value = withContext(Dispatchers.IO) {
            runCatching { ExifUtils.loadOrientedBitmap(context, baseUri) }
                .onFailure { failure = it }
                .getOrNull()
        }
    }

    LaunchedEffect(failure) { failure?.let { onResult(ImageEditorResult.Failed(it)) } }

    val bmp = baseBitmap
    if (bmp == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator(color = Color.White) }
        return
    }

    val editorState = rememberPhotoEditorState(bmp)
    val controller = remember(editorState) { AnnotateController(editorState, request.config) }
    val mosaic = remember(editorState) {
        MosaicController(
            base = bmp,
            blockSize = request.config.mosaicBlockSize,
            onBaked = { editorState.view.source.setImageBitmap(it) },
            onStrokeCommitted = { controller.notifyMosaicStroke() },
        ).also { controller.mosaic = it }
    }

    var showTextDialog by remember { mutableStateOf(false) }
    var showStickerPanel by remember { mutableStateOf(false) }
    var showDiscardConfirm by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }

    val stickers = request.config.stickerProvider?.stickers().orEmpty()

    fun export() {
        if (exporting) return
        exporting = true
        val file = buildOutputFile(context, request.config.output)
        editorState.editor.saveAsFile(
            file.absolutePath,
            buildSaveSettings(request.config.output),
            object : PhotoEditor.OnSaveListener {
                override fun onSuccess(imagePath: String) {
                    onResult(ImageEditorResult.Success(Uri.fromFile(File(imagePath))))
                }
                override fun onFailure(exception: Exception) {
                    exporting = false
                    onResult(ImageEditorResult.Failed(exception))
                }
            },
        )
    }

    fun handleBack() {
        if (controller.hasEdits) showDiscardConfirm = true else onResult(ImageEditorResult.Cancelled)
    }

    EditorScaffold(
        controller = controller,
        theme = request.config.theme,
        showSticker = stickers.isNotEmpty(),
        onBack = ::handleBack,
        onDone = ::export,
        onTextClick = { showTextDialog = true },
        onStickerClick = { showStickerPanel = true },
        canvas = {
            // 画布盒约束到底图宽高比,图片精确铺满(无 letterbox),
            // 这样马赛克 overlay 与图片显示区一一对应,坐标映射才准。
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // 只用 aspectRatio(不加 fillMaxWidth):在宽/高双约束里取能放下的最大尺寸,
                // 竖图也能完整居中显示,不会纵向溢出被顶栏/工具栏裁掉。
                Box(
                    modifier = Modifier
                        .aspectRatio(mosaic.baseWidth.toFloat() / mosaic.baseHeight.toFloat()),
                ) {
                    PhotoEditorCanvas(state = editorState, modifier = Modifier.fillMaxSize())
                    if (controller.activeTool == ActiveTool.Mosaic) {
                        MosaicOverlay(controller = mosaic, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        },
    )

    // 新增文字
    if (showTextDialog) {
        TextInputDialog(
            theme = request.config.theme,
            palette = request.config.brushColors,
            onConfirm = { text, colorCode ->
                editorState.editor.addText(text, buildTextStyle(colorCode, request.config.textShadow))
                showTextDialog = false
            },
            onDismiss = { showTextDialog = false },
        )
    }

    // 双击已有文字 → 编辑
    controller.pendingEditText?.let { pending ->
        TextInputDialog(
            theme = request.config.theme,
            palette = request.config.brushColors,
            initialText = pending.text,
            initialColor = Color(pending.colorCode),
            onConfirm = { text, colorCode ->
                editorState.editor.editText(pending.rootView, text, buildTextStyle(colorCode, request.config.textShadow))
                controller.pendingEditText = null
            },
            onDismiss = { controller.pendingEditText = null },
        )
    }

    // 贴纸面板
    if (showStickerPanel && stickers.isNotEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            StickerPanel(
                stickers = stickers,
                onPick = { editorState.editor.addImage(it) },
                onDismiss = { showStickerPanel = false },
            )
        }
    }

    // 返回二次确认
    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = { Text(stringResource(R.string.ie_discard_title)) },
            text = { Text(stringResource(R.string.ie_discard_message)) },
            confirmButton = {
                TextButton(onClick = { showDiscardConfirm = false; onResult(ImageEditorResult.Cancelled) }) {
                    Text(stringResource(R.string.ie_discard_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirm = false }) { Text(stringResource(R.string.ie_discard_keep)) }
            },
        )
    }

    // 导出进度遮罩(同时拦截操作,避免重复点击)
    if (exporting) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}
