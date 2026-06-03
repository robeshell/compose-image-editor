package com.otq.imageeditor.internal

/** 操作来源:PhotoEditor 标注 vs 自研马赛克。 */
internal enum class OpSource { EDITOR, MOSAIC }

/**
 * 纯逻辑的撤销/重做协调器(无 Android 依赖,可单测)。
 *
 * 全局只维护「操作来源的时间序」,不持有具体内容——具体撤销/重做交给对应子系统。
 * 两个子系统(PhotoEditor / 马赛克)各自也是 LIFO 栈,与本协调器锁步,因此顺序一致。
 */
internal class UndoCoordinator {
    private val undoStack = ArrayDeque<OpSource>()
    private val redoStack = ArrayDeque<OpSource>()

    /** 记录一次新操作;新操作使重做栈失效。 */
    fun record(source: OpSource) {
        undoStack.addLast(source)
        redoStack.clear()
    }

    /** 弹出应撤销的来源(并转入重做栈);无可撤销返回 null。 */
    fun undo(): OpSource? {
        val s = undoStack.removeLastOrNull() ?: return null
        redoStack.addLast(s)
        return s
    }

    /** 弹出应重做的来源(并转回撤销栈);无可重做返回 null。 */
    fun redo(): OpSource? {
        val s = redoStack.removeLastOrNull() ?: return null
        undoStack.addLast(s)
        return s
    }

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()
    val undoDepth: Int get() = undoStack.size
    val redoDepth: Int get() = redoStack.size
}
