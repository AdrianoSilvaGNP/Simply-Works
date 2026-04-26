package com.adrianosilva.simply_works.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adrianosilva.simply_works.ui.theme.CyanAccent
import com.adrianosilva.simply_works.ui.theme.GlassBorder
import com.adrianosilva.simply_works.ui.theme.GlassSurface
import com.adrianosilva.simply_works.ui.theme.SoftWhite

@Composable
fun StatusCircularIndicator(
    progress: Float, // 0.0 to 1.0
    mainText: String,
    subText: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (isLoading) 1f else progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(260.dp)) {
        // Background Circle (faint)
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = GlassBorder,
            strokeWidth = 12.dp,
            trackColor = Color.Transparent,
        )

        // Foreground Progress
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            color = if (isLoading) CyanAccent.copy(alpha = 0.5f) else CyanAccent,
            strokeWidth = 12.dp,
            strokeCap = StrokeCap.Round,
            trackColor = Color.Transparent,
        )

        // Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = CyanAccent,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "UPDATING",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = CyanAccent,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            } else {
                AnimatedContent(
                    targetState = mainText,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "mainText"
                ) { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp,
                            color = SoftWhite,
                            shadow =
                            androidx.compose.ui.graphics.Shadow(
                                color = CyanAccent,
                                blurRadius = 20f
                            )
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                AnimatedContent(
                    targetState = subText,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "subText"
                ) { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = SoftWhite.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Light
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .width(100.dp)
            .height(90.dp),
        shape = RoundedCornerShape(16.dp),
        color = GlassSurface,
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = SoftWhite.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = CyanAccent
            )
        }
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier,
        shape = RoundedCornerShape(50), // Pill shape
        color = GlassSurface.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = CyanAccent
            )
        }
    }
}

@Preview
@Composable
private fun StatusCircularIndicatorPreview() {
    StatusCircularIndicator(
        progress = 0.75f,
        mainText = "45 min",
        subText = "Time Remaining"
    )
}

@Preview
@Composable
private fun StatCardPreview() {
    StatCard(label = "Cycles", value = "12")
}

@Preview
@Composable
private fun GlassButtonPreview() {
    GlassButton(text = "Start Wash", onClick = {})
}
