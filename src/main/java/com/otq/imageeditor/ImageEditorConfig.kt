package com.otq.imageeditor

import androidx.compose.ui.graphics.Color

/**
 * 编辑器配置。全部可由宿主定制,模块无任何业务/项目耦合。
 */
data class ImageEditorConfig(
    /** 启用哪些工具及其在工具栏的顺序。CROP 若包含则作为前置阶段。 */
    val tools: List<EditTool> = EditTool.entries.toList(),

    /** 是否进入编辑器时先走裁剪前置阶段(可在该阶段跳过)。 */
    val cropFirst: Boolean = true,

    /** 涂鸦/文字色板。 */
    val brushColors: List<Color> = DefaultBrushPalette,

    /** 涂鸦笔刷粗细档位(px)。 */
    val brushWidths: List<Float> = listOf(8f, 16f, 28f),

    /** 马赛克像素块边长(px),越大越糊。 */
    val mosaicBlockSize: Int = 24,

    /** 贴纸素材来源;null = 不启用贴纸面板。 */
    val stickerProvider: StickerProvider? = null,

    /** 输出规格。 */
    val output: OutputSpec = OutputSpec(),

    /** 外观主题。 */
    val theme: EditorTheme = EditorTheme(),
)
