package com.otq.imageeditor.internal

import org.junit.Assert.assertEquals
import org.junit.Test

class MosaicMappingTest {

    @Test fun center_maps_proportionally() {
        val r = mapViewToBitmap(50f, 50f, viewW = 100, viewH = 100, baseW = 200, baseH = 200)
        assertEquals(100f, r[0], 0.001f)
        assertEquals(100f, r[1], 0.001f)
    }

    @Test fun origin_maps_to_origin() {
        val r = mapViewToBitmap(0f, 0f, 100, 100, 200, 200)
        assertEquals(0f, r[0], 0.001f)
        assertEquals(0f, r[1], 0.001f)
    }

    @Test fun non_square_scales_each_axis() {
        val r = mapViewToBitmap(30f, 40f, viewW = 100, viewH = 200, baseW = 1000, baseH = 2000)
        assertEquals(300f, r[0], 0.001f)
        assertEquals(400f, r[1], 0.001f)
    }

    @Test fun zero_view_size_is_safe() {
        val r = mapViewToBitmap(10f, 10f, viewW = 0, viewH = 0, baseW = 200, baseH = 200)
        assertEquals(0f, r[0], 0.001f)
        assertEquals(0f, r[1], 0.001f)
    }
}
