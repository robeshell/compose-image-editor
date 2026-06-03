package com.otq.imageeditor.internal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExifTransformTest {

    @Test fun normal_isIdentity() {
        assertTrue(exifTransform(1).isIdentity) // ORIENTATION_NORMAL
        assertTrue(exifTransform(0).isIdentity) // 未定义 → 视为正常
    }

    @Test fun rotations() {
        assertEquals(ExifTransform(90f, false, false), exifTransform(6))  // ROTATE_90
        assertEquals(ExifTransform(180f, false, false), exifTransform(3)) // ROTATE_180
        assertEquals(ExifTransform(270f, false, false), exifTransform(8)) // ROTATE_270
    }

    @Test fun flips() {
        assertEquals(ExifTransform(0f, true, false), exifTransform(2))  // FLIP_HORIZONTAL
        assertEquals(ExifTransform(0f, false, true), exifTransform(4))  // FLIP_VERTICAL
    }

    @Test fun transpose_transverse() {
        assertEquals(ExifTransform(90f, true, false), exifTransform(5))  // TRANSPOSE
        assertEquals(ExifTransform(270f, true, false), exifTransform(7)) // TRANSVERSE
    }
}
