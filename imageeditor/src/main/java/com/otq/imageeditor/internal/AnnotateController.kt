package com.otq.imageeditor.internal

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.otq.imageeditor.ImageEditorConfig
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener
import ja.burhanrashid52.photoeditor.ViewType
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder
import ja.burhanrashid52.photoeditor.shape.ShapeType

/** 当前激活的"持续型"工具。文字/贴纸是一次性动作,不在此列。 */
internal enum class ActiveTool { None, Draw, Mosaic }

/**
 * 标注阶段的交互中枢:集中持有工具状态并驱动 PhotoEditor。
 * 单工具激活:切换工具会互斥地关掉其它模式。
 */
@Stable
internal class AnnotateController(
    val state: PhotoEditorState,
    val config: ImageEditorConfig,
) {
    var activeTool by mutableStateOf(ActiveTool.None)
        private set

    var brushColor by mutableStateOf(config.brushColors.firstOrNull() ?: Color.White)
        private set
    var brushWidth by mutableStateOf(config.brushWidths.getOrElse(1) { 16f })
        private set
    var eraser by mutableStateOf(false)
        private set

    /**
     * 全局撤销/重做协调器:只记操作来源时间序,具体撤销/重做分流到子系统。
     * - PhotoEditor 侧每笔涂鸦/文字/贴纸 → onAddViewListener → 记 EDITOR
     * - 马赛克每笔 → notifyMosaicStroke → 记 MOSAIC
     */
    private val coordinator = UndoCoordinator()

    /** 撤销/重做会触发 PhotoEditor 重新增删视图并回调监听,此标志避免把它误记成新操作。 */
    private var suppressRecord = false

    /** 历史版本号:任何撤销栈变化 +1,驱动 Compose 重算 canUndo/canRedo。 */
    private var historyVersion by mutableStateOf(0)

    /** 马赛克控制器(由 AnnotateStage 在底图就绪后注入)。 */
    var mosaic: MosaicController? = null

    /** 待编辑文字(双击已有文字触发);非空时宿主弹出编辑框。 */
    var pendingEditText by mutableStateOf<PendingEditText?>(null)

    init {
        state.editor.setOnPhotoEditorListener(object : OnPhotoEditorListener {
            override fun onEditTextChangeListener(rootView: android.view.View, text: String, colorCode: Int) {
                pendingEditText = PendingEditText(rootView, text, colorCode)
            }
            override fun onAddViewListener(viewType: ViewType, numberOfAddedViews: Int) {
                if (suppressRecord) return
                coordinator.record(OpSource.EDITOR)
                mosaic?.clearRedo()
                historyVersion++
            }
            override fun onRemoveViewListener(viewType: ViewType, numberOfAddedViews: Int) {}
            override fun onStartViewChangeListener(viewType: ViewType) {}
            override fun onStopViewChangeListener(viewType: ViewType) {}
            override fun onTouchSourceImage(event: android.view.MotionEvent) {}
        })
    }

    /** 马赛克提交一笔时调用,记入全局时间序。 */
    fun notifyMosaicStroke() {
        coordinator.record(OpSource.MOSAIC)
        historyVersion++
    }

    fun selectDraw() {
        activeTool = ActiveTool.Draw
        eraser = false
        applyBrush()
    }

    fun selectMosaic() {
        activeTool = ActiveTool.Mosaic
        state.editor.setBrushDrawingMode(false)
    }

    /** 退出持续型工具(用于触发文字/贴纸等一次性动作前)。 */
    fun clearActive() {
        activeTool = ActiveTool.None
        state.editor.setBrushDrawingMode(false)
    }

    fun changeBrushColor(color: Color) {
        brushColor = color
        eraser = false
        if (activeTool == ActiveTool.Draw) applyBrush()
    }

    fun changeBrushWidth(width: Float) {
        brushWidth = width
        if (activeTool == ActiveTool.Draw) applyBrush()
    }

    fun toggleEraser() {
        eraser = !eraser
        applyBrush()
    }

    private fun applyBrush() {
        val e = state.editor
        e.setBrushDrawingMode(true)
        if (eraser) {
            e.brushEraser()
        } else {
            e.setShape(
                ShapeBuilder()
                    .withShapeType(ShapeType.Brush)
                    .withShapeColor(brushColor.toArgb())
                    .withShapeSize(brushWidth)
            )
        }
    }

    /** 单撤销键:弹出全局栈顶,分流到对应子系统撤销。 */
    fun undo() {
        val source = coordinator.undo() ?: return
        suppressRecord = true
        when (source) {
            OpSource.EDITOR -> state.editor.undo()
            OpSource.MOSAIC -> mosaic?.undo()
        }
        suppressRecord = false
        historyVersion++
    }

    /** 单重做键:弹出重做栈顶,分流到对应子系统重做。 */
    fun redo() {
        val source = coordinator.redo() ?: return
        suppressRecord = true
        when (source) {
            OpSource.EDITOR -> state.editor.redo()
            OpSource.MOSAIC -> mosaic?.redo()
        }
        suppressRecord = false
        historyVersion++
    }

    /** 是否有任何编辑(用于返回时判断是否需要二次确认)。 */
    val hasEdits: Boolean get() { historyVersion; return coordinator.canUndo }

    val canUndo: Boolean get() { historyVersion; return coordinator.canUndo }

    val canRedo: Boolean get() { historyVersion; return coordinator.canRedo }
}

internal data class PendingEditText(
    val rootView: android.view.View,
    val text: String,
    val colorCode: Int,
)
