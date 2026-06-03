package com.otq.imageeditor.internal

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions

/**
 * 裁剪前置阶段:进入即拉起 cropper 自带全屏 UI(裁剪/旋转/翻转/比例)。
 *
 * - 裁剪成功 → [onResolved] 回裁剪后的 Uri
 * - 用户在 cropper 里取消 → 视为"跳过裁剪",[onResolved] 回原图 [source]
 *
 * 取消裁剪等于跳过(用原图进标注),整体退出走标注阶段的返回键。
 */
// cropper 4.7.0 把内置 CropImageContract 标了 deprecated,但并未提供替代类,
// 它仍是该版本唯一可用入口且功能正常,故在此抑制告警。
@Suppress("DEPRECATION")
@Composable
internal fun CropStage(
    source: Uri,
    onResolved: (Uri) -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        onResolved(if (result.isSuccessful) (result.uriContent ?: source) else source)
    }
    LaunchedEffect(Unit) {
        launcher.launch(CropImageContractOptions(source, CropImageOptions()))
    }
}
