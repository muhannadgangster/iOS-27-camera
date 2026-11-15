package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
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
import com.example.ui.theme.CameraRedRecording
import com.example.ui.theme.CameraSurfaceGlass

/**
 * Premium iOS 26 Liquid Glass surface with 3D curved borders, visible side edges,
 * dimensional depth, realistic highlights, and subtle background refraction.
 */
@Composable
fun LiquidGlassSurface(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(28.dp),
  elevation: Dp = 10.dp,
  isGlassEnabled: Boolean = true,
  isReflectionEnabled: Boolean = true,
  backgroundColor: Color = CameraSurfaceGlass,
  content: @Composable BoxScope.() -> Unit
) {
  val borderBrush = Brush.linearGradient(
    colors = listOf(
      Color(0x99FFFFFF), // Top-left sharp specular edge
      Color(0x2BFFFFFF),
      Color(0x0FFFFFFF),
      Color(0x40FFFFFF)  // Bottom-right rim reflection
    ),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
  )

  Box(
    modifier = modifier
      .shadow(elevation = if (isGlassEnabled) elevation else 2.dp, shape = shape, clip = false)
      .clip(shape)
      .background(
        if (isGlassEnabled) {
          Brush.verticalGradient(
            colors = listOf(
              Color(0xD9181D29), // Translucent high-index glass tint
              Color(0xBF10141F),
              Color(0xE60A0D15)
            )
          )
        } else {
          Brush.verticalGradient(listOf(CameraOnyxDark, Color.Black))
        }
      )
      .then(
        if (isGlassEnabled && isReflectionEnabled) {
          Modifier.drawBehind {
            // Upper curved specular refraction highlight
            drawRoundRect(
              brush = Brush.verticalGradient(
                colors = listOf(Color(0x38FFFFFF), Color.Transparent),
                startY = 0f,
                endY = size.height * 0.42f
              ),
              size = size
            )
            // Delicate inner border line simulating glass thickness
            drawRoundRect(
              brush = Brush.verticalGradient(
                colors = listOf(Color(0x1AFFFFFF), Color(0x05FFFFFF)),
                startY = 0f,
                endY = size.height
              ),
              size = size
            )
          }
        } else {
          Modifier
        }
      )
      .border(
        width = 1.2.dp,
        brush = if (isGlassEnabled) borderBrush else Brush.linearGradient(listOf(CameraGlassBorder, Color.Transparent)),
        shape = shape
      ),
    content = content
  )
}

/**
 * iOS 26 Circular or pill Liquid Glass floating control button with visible dimensional side rim.
 */
@Composable
fun LiquidGlassButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  buttonSize: Dp = 50.dp,
  isSelected: Boolean = false,
  activeColor: Color = Color(0xFFFFD60A),
  content: @Composable BoxScope.() -> Unit
) {
  val shape = CircleShape
  val borderBrush = Brush.linearGradient(
    colors = if (isSelected) {
      listOf(activeColor, activeColor.copy(alpha = 0.5f))
    } else {
      listOf(
        Color(0x99FFFFFF),
        Color(0x2EFFFFFF),
        Color(0x14FFFFFF),
        Color(0x4DFFFFFF)
      )
    },
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
  )

  Box(
    modifier = modifier
      .size(buttonSize)
      .shadow(elevation = 6.dp, shape = shape, clip = false)
      .clip(shape)
      .background(
        Brush.verticalGradient(
          colors = if (isSelected) {
            listOf(activeColor.copy(alpha = 0.35f), activeColor.copy(alpha = 0.18f))
          } else {
            listOf(Color(0x52232938), Color(0x380F131D))
          }
        )
      )
      .drawBehind {
        // Light catching top glass rim
        drawCircle(
          brush = Brush.verticalGradient(
            colors = listOf(Color(0x40FFFFFF), Color.Transparent),
            startY = 0f,
            endY = size.height * 0.5f
          )
        )
      }
      .border(width = 1.2.dp, brush = borderBrush, shape = shape)
      .clickable { onClick() },
    contentAlignment = Alignment.Center,
    content = content
  )
}

/**
 * iOS 26 Liquid Glass Shutter Button:
 * Replaces the old solid white ring with a dimensional Liquid Glass outer rim
 * with visible side edges, reflections, and tactile response in both Photo and Video modes.
 */
@Composable
fun LiquidGlassShutterButton(
  isVideoMode: Boolean,
  isRecording: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.92f else 1.0f,
    animationSpec = spring(),
    label = "shutter_press_scale"
  )

  // Inner button shape & dimensions
  val innerCornerRadius by animateDpAsState(
    targetValue = if (isVideoMode && isRecording) 10.dp else 36.dp,
    animationSpec = tween(280, easing = FastOutSlowInEasing),
    label = "inner_corner_radius"
  )

  val innerSize by animateDpAsState(
    targetValue = if (isVideoMode && isRecording) 32.dp else 60.dp,
    animationSpec = tween(280, easing = FastOutSlowInEasing),
    label = "inner_button_size"
  )

  // Infinite pulsing sheen when recording video
  val infiniteTransition = rememberInfiniteTransition(label = "recording_pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 0.85f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_alpha"
  )

  // Outer Liquid Glass Ring (76dp)
  Box(
    modifier = modifier
      .size(76.dp)
      .scale(scale)
      .shadow(elevation = 8.dp, shape = CircleShape, clip = false)
      .clip(CircleShape)
      .background(
        Brush.radialGradient(
          colors = if (isVideoMode) {
            listOf(Color(0x38FF3B30), Color(0x1AFF3B30), Color(0x1F000000))
          } else {
            listOf(Color(0x3DFFFFFF), Color(0x1AFFFFFF), Color(0x14000000))
          }
        )
      )
      .drawBehind {
        // 3D Glass curved highlight rim
        val rimBorder = Brush.linearGradient(
          colors = if (isVideoMode) {
            listOf(
              Color(0xCCFFFFFF),
              CameraRedRecording.copy(alpha = if (isRecording) pulseAlpha else 0.7f),
              Color(0x33FFFFFF),
              Color(0x80FFFFFF)
            )
          } else {
            listOf(
              Color(0xE6FFFFFF),
              Color(0x59FFFFFF),
              Color(0x1AFFFFFF),
              Color(0x80FFFFFF)
            )
          },
          start = Offset(0f, 0f),
          end = Offset(size.width, size.height)
        )
        // Outer beveled glass edge
        drawCircle(
          brush = rimBorder,
          radius = size.minDimension / 2f - 1.dp.toPx(),
          style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
        )
        // Upper specular refraction
        drawCircle(
          brush = Brush.verticalGradient(
            colors = listOf(Color(0x40FFFFFF), Color.Transparent),
            startY = 0f,
            endY = size.height * 0.45f
          )
        )
      }
      .clickable(
        interactionSource = interactionSource,
        indication = null
      ) { onClick() },
    contentAlignment = Alignment.Center
  ) {
    // Inner Core Button
    val coreShape = RoundedCornerShape(innerCornerRadius)
    Box(
      modifier = Modifier
        .size(innerSize)
        .shadow(elevation = 4.dp, shape = coreShape)
        .clip(coreShape)
        .background(
          if (isVideoMode) {
            Brush.verticalGradient(
              colors = listOf(
                Color(0xFFFF453A),
                Color(0xFFFF2D55),
                Color(0xFFD70015)
              )
            )
          } else {
            Brush.verticalGradient(
              colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFEDEDED),
                Color(0xFFD6D6D6)
              )
            )
          }
        )
        .border(
          width = 1.dp,
          brush = Brush.verticalGradient(
            colors = if (isVideoMode) {
              listOf(Color(0x80FFFFFF), Color(0x33FF453A))
            } else {
              listOf(Color(0xFFFFFFFF), Color(0x80FFFFFF))
            }
          ),
          shape = coreShape
        )
    )
  }
}

/**
 * Liquid Glass Pill for selectable items in panels and options.
 */
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
      colors = listOf(Color(0x80FFFFFF), CameraGlassBorder)
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
