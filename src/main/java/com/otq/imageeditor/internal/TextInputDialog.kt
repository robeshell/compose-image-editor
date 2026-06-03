package com.otq.imageeditor.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.otq.imageeditor.EditorTheme
import com.otq.imageeditor.R

/**
 * 文字输入/编辑弹窗。确认后回吐文字内容与颜色(ARGB int)。
 *
 * @param initialText  编辑已有文字时预填;新增时为空
 */
@Composable
internal fun TextInputDialog(
    theme: EditorTheme,
    palette: List<Color>,
    initialText: String = "",
    initialColor: Color = Color.White,
    onConfirm: (text: String, colorCode: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initialText) }
    var color by remember { mutableStateOf(initialColor) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF222222), RoundedCornerShape(12.dp))
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                placeholder = { Text(stringResource(R.string.ie_text_hint), color = Color.White.copy(alpha = 0.5f)) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = theme.primary,
                    focusedBorderColor = theme.primary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                palette.forEach { c ->
                    val sel = c == color
                    Box(
                        modifier = Modifier
                            .size(if (sel) 26.dp else 20.dp)
                            .background(c, CircleShape)
                            .border(
                                width = if (sel) 2.dp else 1.dp,
                                color = if (sel) theme.primary else Color.White.copy(alpha = 0.4f),
                                shape = CircleShape,
                            )
                            .clickable { color = c },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.ie_cancel), color = Color.White) }
                TextButton(
                    onClick = { if (text.isNotBlank()) onConfirm(text, color.toArgb()) },
                ) { Text(stringResource(R.string.ie_confirm), color = theme.primary) }
            }
        }
    }
}
