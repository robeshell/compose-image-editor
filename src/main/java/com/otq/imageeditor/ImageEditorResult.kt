package com.otq.imageeditor

import android.net.Uri

/**
 * 编辑结果。模块只产出一个本地图片 [Uri],绝不涉及发送/上传。
 */
sealed interface ImageEditorResult {
    /** 编辑完成,产出图片。 */
    data class Success(val uri: Uri) : ImageEditorResult

    /** 用户放弃编辑。 */
    data object Cancelled : ImageEditorResult

    /** 编辑过程中出错(解码/导出失败等)。 */
    data class Failed(val cause: Throwable) : ImageEditorResult
}
