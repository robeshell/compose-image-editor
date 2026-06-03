package com.otq.imageeditor.internal

import org.junit.Assert.assertEquals
import org.junit.Test

class TextStylingTest {

    private val darkShadow = 0xCC000000.toInt()
    private val lightShadow = 0xCCFFFFFF.toInt()

    @Test fun light_text_gets_dark_shadow() {
        assertEquals(darkShadow, contrastShadowColor(0xFFFFFFFF.toInt())) // 白
        assertEquals(darkShadow, contrastShadowColor(0xFFFFFF00.toInt())) // 黄(高亮度)
    }

    @Test fun dark_text_gets_light_shadow() {
        assertEquals(lightShadow, contrastShadowColor(0xFF000000.toInt())) // 黑
        assertEquals(lightShadow, contrastShadowColor(0xFF000080.toInt())) // 深蓝(低亮度)
    }
}
