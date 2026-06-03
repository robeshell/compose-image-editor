package com.otq.imageeditor

/**
 * 编辑器支持的工具。橡皮不是顶层工具,而是涂鸦的子选项,故不在此枚举。
 */
enum class EditTool {
    /** 裁剪 / 旋转 / 翻转(前置阶段) */
    CROP,

    /** 涂鸦画笔(含橡皮子选项) */
    DRAW,

    /** 马赛克(自研像素块笔刷) */
    MOSAIC,

    /** 文字标注(可拖拽/缩放/旋转图层) */
    TEXT,

    /** 贴纸 / emoji(素材由宿主 StickerProvider 提供) */
    STICKER,
}
