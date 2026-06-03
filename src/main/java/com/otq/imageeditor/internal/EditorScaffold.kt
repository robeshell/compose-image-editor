package com.otq.imageeditor.internal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otq.imageeditor.EditTool
import com.otq.imageeditor.EditorTheme
import com.otq.imageeditor.R

/**
 * 编辑器脚手架:画布铺底,顶栏(返回/撤销)与底部工具栏(+上下文条)悬浮其上。
 */
@Composable
internal fun EditorScaffold(
    controller: AnnotateController,
    theme: EditorTheme,
    showSticker: Boolean,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onTextClick: () -> Unit,
    onStickerClick: () -> Unit,
    canvas: @Composable () -> Unit,
) {
    val tools = controller.config.tools
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        canvas()

        // 顶栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.toolbarBackground.copy(alpha = 0.55f))
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.ie_back), tint = theme.toolbarContent)
            }
            Spacer(Modifier.width(0.dp).weight(1f))
            IconButton(onClick = controller::undo, enabled = controller.canUndo) {
                Icon(
                    Icons.AutoMirrored.Filled.Undo, stringResource(R.string.ie_undo),
                    tint = if (controller.canUndo) theme.toolbarContent else theme.toolbarContent.copy(alpha = 0.3f),
                )
            }
        }

        // 底部:上下文条 + 主工具栏
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            // 涂鸦上下文条:色板 + 粗细 + 橡皮
            AnimatedVisibility(visible = controller.activeTool == ActiveTool.Draw) {
                DrawContextBar(controller = controller, theme = theme)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.toolbarBackground.copy(alpha = 0.85f))
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (EditTool.DRAW in tools) {
                    ToolButton(Icons.Filled.Brush, stringResource(R.string.ie_tool_draw), controller.activeTool == ActiveTool.Draw, theme) {
                        if (controller.activeTool == ActiveTool.Draw) controller.clearActive() else controller.selectDraw()
                    }
                }
                if (EditTool.MOSAIC in tools) {
                    ToolButton(Icons.Filled.GridOn, stringResource(R.string.ie_tool_mosaic), controller.activeTool == ActiveTool.Mosaic, theme) {
                        if (controller.activeTool == ActiveTool.Mosaic) controller.clearActive() else controller.selectMosaic()
                    }
                }
                if (EditTool.TEXT in tools) {
                    ToolButton(Icons.Filled.TextFields, stringResource(R.string.ie_tool_text), false, theme) {
                        controller.clearActive(); onTextClick()
                    }
                }
                if (EditTool.STICKER in tools && showSticker) {
                    ToolButton(Icons.Filled.EmojiEmotions, stringResource(R.string.ie_tool_sticker), false, theme) {
                        controller.clearActive(); onStickerClick()
                    }
                }

                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(theme.primary)
                        .clickable(onClick = onDone)
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                ) {
                    Text(stringResource(R.string.ie_done), color = Color.White, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    theme: EditorTheme,
    onClick: () -> Unit,
) {
    val tint = if (selected) theme.primary else theme.toolbarContent
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Icon(icon, label, tint = tint, modifier = Modifier.size(24.dp))
        Text(label, color = tint, fontSize = 10.sp)
    }
}

@Composable
private fun DrawContextBar(controller: AnnotateController, theme: EditorTheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.toolbarBackground.copy(alpha = 0.7f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // 色板
        controller.config.brushColors.forEach { color ->
            val isSel = !controller.eraser && color == controller.brushColor
            Box(
                modifier = Modifier
                    .size(if (isSel) 28.dp else 22.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSel) 2.dp else 1.dp,
                        color = if (isSel) theme.primary else Color.White.copy(alpha = 0.5f),
                        shape = CircleShape,
                    )
                    .clickable { controller.changeBrushColor(color) },
            )
        }
        Spacer(Modifier.width(8.dp))
        // 粗细档位
        controller.config.brushWidths.forEach { w ->
            val isSel = !controller.eraser && w == controller.brushWidth
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (isSel) theme.primary.copy(alpha = 0.25f) else Color.Transparent)
                    .clickable { controller.changeBrushWidth(w) },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size((w / 2f).coerceIn(4f, 14f).dp)
                        .clip(CircleShape)
                        .background(if (isSel) theme.primary else theme.toolbarContent),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        // 橡皮
        Text(
            text = stringResource(R.string.ie_eraser),
            color = if (controller.eraser) theme.primary else theme.toolbarContent,
            fontSize = 13.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable { controller.toggleEraser() }
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}
