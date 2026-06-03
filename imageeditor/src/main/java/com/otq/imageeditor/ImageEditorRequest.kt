package com.otq.imageeditor

import android.net.Uri

/**
 * 编辑请求:一张源图 + 配置。模块只认 [Uri],不关心它从哪来。
 */
data class ImageEditorRequest(
    val sourceUri: Uri,
    val config: ImageEditorConfig = ImageEditorConfig(),
)
