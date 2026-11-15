package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CameraGlassBorder
import com.example.ui.theme.CameraGlassHighlight
import com.example.ui.theme.CameraGlassShine
import com.example.ui.theme.CameraOnyxDark
import com.example.ui.theme.CameraSurfaceGlass

@Composable
fun LiquidGlassSurface(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(28.dp),
  elevation: Dp = 8.dp,
  isGlassEnabled: Boolean = true,
  isReflectionEnabled: Boolean = true,
  backgroundColor: Color = CameraSurfaceGlass,
  content: @Composable BoxScope.() -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "liquid_glass_sheen")
  val sheenOffset by infiniteTransition.animateFloat(
    initialValue = -1f,
    targetValue = 2f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "sheen_offset"
  )

  val borderBrush = Brush.linearGradient(
    colors = listOf(
      CameraGlassHighlight,
      CameraGlassBorder,
      CameraGlassHighlight.copy(alpha = 0.15f),
      CameraGlassBorder
    ),
    start = Offset.Zero,
    end = Offset.Infinite
  )

  Box(
    modifier = modifier
      .shadow(elevation = if (isGlassEnabled) elevation else 2.dp, shape = shape, clip = false)
      .clip(shape)
      .background(
        if (isGlassEnabled) {
          Brush.verticalGradient(
            colors = listOf(
              backgroundColor,
              backgroundColor.copy(alpha = 0.88f),
              CameraOnyxDark.copy(alpha = 0.95f)
            )
          )
        } else {
          Brush.verticalGradient(listOf(CameraOnyxDark, Color.Black))
        }
      )
      .then(
        if (isGlassEnabled && isReflectionEnabled) {
          Modifier.drawBehind {
            // Subtle 3D top specular curve reflection
            drawRoundRect(
              brush = Brush.verticalGradient(
                colors = listOf(CameraGlassShine, Color.Transparent),
                startY = 0f,
                endY = size.height * 0.45f
              ),
              size = size
            )
          }
        } else {
          Modifier
        }
      )
      .border(
        width = 1.dp,
        brush = if (isGlassEnabled) borderBrush else Brush.linearGradient(listOf(CameraGlassBorder, Color.Transparent)),
        shape = shape
      ),
    content = content
  )
}

@Composable
fun LiquidGlassPill(
  modifier: Modifier = Modifier,
  isSelected: Boolean = false,
  activeColor: Color = Color(0xFFFFD60A),
  content: @Composable BoxScope.() -> Unit
) {
  val shape = CircleShape
  val bgBrush = if (isSelected) {
    Brush.verticalGradient(
      colors = listOf(
        activeColor.copy(alpha = 0.35f),
        activeColor.copy(alpha = 0.18f)
      )
    )
  } else {
    Brush.verticalGradient(
      colors = listOf(
        Color(0x3D242B3B),
        Color(0x1F0B0E17)
      )
    )
  }

  val borderBrush = if (isSelected) {
    Brush.linearGradient(
      colors = listOf(activeColor, activeColor.copy(alpha = 0.5f))
    )
  } else {
    Brush.linearGradient(
      colors = listOf(CameraGlassHighlight.copy(alpha = 0.4f), CameraGlassBorder)
    )
  }

  Box(
    modifier = modifier
      .clip(shape)
      .background(bgBrush)
      .border(width = 1.dp, brush = borderBrush, shape = shape),
    content = content
  )
}
