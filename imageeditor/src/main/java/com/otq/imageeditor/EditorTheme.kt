package com.otq.imageeditor

import androidx.compose.ui.graphics.Color

/**
 * 编辑器外观,去硬编码。宿主可注入项目主色,使工具栏与项目风格统一。
 */
data class EditorTheme(
    /** 主色:选中态、确认按钮等 */
    val primary: Color = Color(0xFF1E6FFF),
    /** 工具栏 / 顶栏背景 */
    val toolbarBackground: Color = Color(0xFF1A1A1A),
    /** 工具栏图标 / 文字色 */
    val toolbarContent: Color = Color.White,
)

/** 默认画笔色板(涂鸦/文字共用) */
val DefaultBrushPalette: List<Color> = listOf(
    Color(0xFFFFFFFF), // 白
    Color(0xFF000000), // 黑
    Color(0xFFFF3B30), // 红
    Color(0xFFFF9500), // 橙
    Color(0xFFFFCC00), // 黄
    Color(0xFF34C759), // 绿
    Color(0xFF007AFF), // 蓝
    Color(0xFFAF52DE), // 紫
)
