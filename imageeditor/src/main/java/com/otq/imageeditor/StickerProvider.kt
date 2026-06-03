package com.otq.imageeditor

import android.net.Uri

/**
 * 贴纸素材由宿主注入,模块本身不内置任何素材(避免素材包体积与版权问题)。
 * 返回的每个 [Uri] 既用于面板缩略图,也用于贴到画布(模块内部用 Coil 解码)。
 */
interface StickerProvider {
    fun stickers(): List<Uri>
}
