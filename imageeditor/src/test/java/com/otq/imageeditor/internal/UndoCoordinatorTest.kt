package com.otq.imageeditor.internal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UndoCoordinatorTest {

    @Test fun empty_hasNothing() {
        val c = UndoCoordinator()
        assertFalse(c.canUndo)
        assertFalse(c.canRedo)
        assertNull(c.undo())
        assertNull(c.redo())
    }

    @Test fun record_then_undo_then_redo() {
        val c = UndoCoordinator()
        c.record(OpSource.EDITOR)
        assertTrue(c.canUndo)
        assertFalse(c.canRedo)

        assertEquals(OpSource.EDITOR, c.undo())
        assertFalse(c.canUndo)
        assertTrue(c.canRedo)

        assertEquals(OpSource.EDITOR, c.redo())
        assertTrue(c.canUndo)
        assertFalse(c.canRedo)
    }

    @Test fun interleaved_order_is_lifo() {
        val c = UndoCoordinator()
        c.record(OpSource.EDITOR)   // E1
        c.record(OpSource.MOSAIC)   // M1
        c.record(OpSource.EDITOR)   // E2

        // 撤销按逆序:E2, M1, E1
        assertEquals(OpSource.EDITOR, c.undo())
        assertEquals(OpSource.MOSAIC, c.undo())
        assertEquals(OpSource.EDITOR, c.undo())
        assertNull(c.undo())

        // 重做按相反顺序回放:E1, M1, E2
        assertEquals(OpSource.EDITOR, c.redo())
        assertEquals(OpSource.MOSAIC, c.redo())
        assertEquals(OpSource.EDITOR, c.redo())
        assertNull(c.redo())
    }

    @Test fun new_record_invalidates_redo() {
        val c = UndoCoordinator()
        c.record(OpSource.EDITOR)
        c.undo()
        assertTrue(c.canRedo)

        c.record(OpSource.MOSAIC) // 新操作
        assertFalse(c.canRedo)
        assertEquals(1, c.undoDepth)
        assertEquals(0, c.redoDepth)
    }
}
