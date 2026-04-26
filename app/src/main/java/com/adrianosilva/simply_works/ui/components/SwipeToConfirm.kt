package com.adrianosilva.simply_works.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.adrianosilva.simply_works.ui.theme.CyanAccent
import com.adrianosilva.simply_works.ui.theme.GlassSurface
import com.adrianosilva.simply_works.ui.theme.SoftWhite
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeToConfirm(
    text: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val hapticFeedback = LocalHapticFeedback.current
    var width by remember { mutableFloatStateOf(0f) }
    val thumbSize = 56.dp
    val thumbSizePx = with(LocalDensity.current) { thumbSize.toPx() }
    val coroutineScope = rememberCoroutineScope()
    
    val offsetX = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbSize)
            .background(GlassSurface, RoundedCornerShape(percent = 50))
            .onSizeChanged { width = it.width.toFloat() }
    ) {
        // Background Text
        Text(
            text = if (isLoading) "PROCESSING..." else text,
            color = SoftWhite.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Center)
        )

        // Draggable Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(thumbSize)
                .background(
                    color = if (isLoading) Color.Gray else CyanAccent,
                    shape = RoundedCornerShape(percent = 50)
                )
                .pointerInput(width, isLoading) {
                    if (isLoading) return@pointerInput
                    detectDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                val threshold = width - thumbSizePx - 10f
                                if (offsetX.value >= threshold) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onConfirm()
                                    offsetX.animateTo(0f)
                                } else {
                                    offsetX.animateTo(0f)
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                val newOffset = (offsetX.value + dragAmount.x).coerceIn(0f, width - thumbSizePx)
                                offsetX.snapTo(newOffset)
                            }
                        }
                    )
                }
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp).align(Alignment.Center),
                    color = SoftWhite,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
