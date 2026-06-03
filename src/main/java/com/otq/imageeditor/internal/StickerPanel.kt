package com.otq.imageeditor.internal

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 贴纸面板:横向缩略图列表。点击 → 解码为软件位图 → onPick。
 */
@Composable
internal fun StickerPanel(
    stickers: List<Uri>,
    onPick: (Bitmap) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A))
            .padding(vertical = 10.dp),
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().height(72.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
        ) {
            items(stickers) { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2A2A2A))
                        .clickable {
                            scope.launch {
                                val bmp = withContext(Dispatchers.IO) { loadBitmap(context, uri) }
                                if (bmp != null) {
                                    onPick(bmp)
                                    onDismiss()
                                }
                            }
                        },
                )
            }
        }
    }
}

private suspend fun loadBitmap(context: Context, uri: Uri): Bitmap? {
    val request = ImageRequest.Builder(context)
        .data(uri)
        .allowHardware(false) // PhotoEditor 需要可读像素的软件位图
        .build()
    val result = context.imageLoader.execute(request)
    return (result as? SuccessResult)?.drawable?.toBitmap()
}
