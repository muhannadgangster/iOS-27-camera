package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatioSetting
import com.example.model.GridMode
import com.example.model.PhotographicStyle
import com.example.processing.FilterColorMatrices
import com.example.ui.theme.CameraDeepBlack
import com.example.ui.theme.CameraGoldAccent
import com.example.ui.theme.CameraTextPrimary
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ViewfinderOverlay(
  gridMode: GridMode,
  isLevelEnabled: Boolean,
  levelRollDegrees: Float,
  isLevelAligned: Boolean,
  isAeAfLocked: Boolean,
  focusPoint: Pair<Float, Float>?, // normalized 0..1
  countdownRemaining: Int,
  aspectRatioSetting: AspectRatioSetting,
  selectedStyle: PhotographicStyle,
  onExposureDrag: (Float) -> Unit,
  onUnlockAeAf: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier.fillMaxSize()) {

    // 1. Live Photographic Style Tint Filter Overlay (only if not original)
    if (selectedStyle != PhotographicStyle.ORIGINAL) {
      val composeMatrix = remember(selectedStyle) {
        FilterColorMatrices.createComposeColorMatrix(
          selectedStyle,
          com.example.model.FilterAdjustments()
        )
      }
      Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
          color = Color.White,
          colorFilter = ColorFilter.colorMatrix(composeMatrix)
        )
      }
    }

    // 2. Aspect Ratio Framing Mask (Black bars when 1:1 or 4:3 vs 16:9)
    AspectFramingMask(aspectRatioSetting)

    // 3. Grid Overlay
    if (gridMode != GridMode.OFF) {
      CameraGridCanvas(gridMode = gridMode)
    }

    // 4. Horizon Leveling Indicator
    if (isLevelEnabled) {
      HorizonLevelIndicator(
        rollDegrees = levelRollDegrees,
        isAligned = isLevelAligned
      )
    }

    // 5. AE/AF Lock Banner
    AnimatedVisibility(
      visible = isAeAfLocked,
      enter = fadeIn(tween(200)) + scaleIn(tween(200)),
      exit = fadeOut(tween(200)) + scaleOut(tween(200)),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 16.dp)
    ) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(CameraGoldAccent)
          .padding(horizontal = 10.dp, vertical = 4.dp)
          .testTag("ae_af_lock_badge")
      ) {
        Text(
          text = "AE/AF LOCK",
          color = Color.Black,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp
        )
      }
    }

    // 6. Interactive Touch-to-Focus Reticle with Draggable Sun Icon
    focusPoint?.let { (normX, normY) ->
      FocusReticle(
        normX = normX,
        normY = normY,
        onExposureDrag = onExposureDrag
      )
    }

    // 7. Giant Countdown Overlay (3, 2, 1)
    if (countdownRemaining > 0) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = countdownRemaining.toString(),
          color = CameraTextPrimary,
          fontSize = 120.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.testTag("timer_countdown_text")
        )
      }
    }
  }
}

@Composable
private fun BoxScope.AspectFramingMask(setting: AspectRatioSetting) {
  when (setting) {
    AspectRatioSetting.RATIO_1_1 -> {
      // Top and bottom square masks
      Box(
        modifier = Modifier
          .fillMaxSize()
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val squareSize = size.width
          val verticalGap = (size.height - squareSize) / 2f
          if (verticalGap > 0f) {
            // Top black bar
            drawRect(
              color = CameraDeepBlack,
              topLeft = Offset.Zero,
              size = Size(size.width, verticalGap)
            )
            // Bottom black bar
            drawRect(
              color = CameraDeepBlack,
              topLeft = Offset(0f, size.height - verticalGap),
              size = Size(size.width, verticalGap)
            )
          }
        }
      }
    }
    AspectRatioSetting.RATIO_16_9 -> {
      // 16:9 full viewport or letterbox
    }
    AspectRatioSetting.RATIO_4_3 -> {
      // Standard 4:3 mask
    }
  }
}

@Composable
private fun CameraGridCanvas(gridMode: GridMode) {
  Canvas(
    modifier = Modifier
      .fillMaxSize()
      .testTag("camera_grid_canvas")
  ) {
    val stroke = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f)))
    val gridColor = Color.White.copy(alpha = 0.28f)

    when (gridMode) {
      GridMode.RULE_OF_THIRDS -> {
        val colW = size.width / 3f
        val rowH = size.height / 3f

        // Vertical lines
        drawLine(gridColor, Offset(colW, 0f), Offset(colW, size.height), strokeWidth = 1f)
        drawLine(gridColor, Offset(colW * 2, 0f), Offset(colW * 2, size.height), strokeWidth = 1f)

        // Horizontal lines
        drawLine(gridColor, Offset(0f, rowH), Offset(size.width, rowH), strokeWidth = 1f)
        drawLine(gridColor, Offset(0f, rowH * 2), Offset(size.width, rowH * 2), strokeWidth = 1f)
      }
      GridMode.SQUARE -> {
        val squareDim = size.width
        val topOffset = (size.height - squareDim) / 2f
        drawRect(
          color = gridColor,
          topLeft = Offset(0f, topOffset),
          size = Size(squareDim, squareDim),
          style = Stroke(width = 1.5f)
        )
      }
      GridMode.GOLDEN_RATIO -> {
        val phi = 0.618f
        val x1 = size.width * (1f - phi)
        val x2 = size.width * phi
        val y1 = size.height * (1f - phi)
        val y2 = size.height * phi

        drawLine(gridColor, Offset(x1, 0f), Offset(x1, size.height), strokeWidth = 1f)
        drawLine(gridColor, Offset(x2, 0f), Offset(x2, size.height), strokeWidth = 1f)
        drawLine(gridColor, Offset(0f, y1), Offset(size.width, y1), strokeWidth = 1f)
        drawLine(gridColor, Offset(0f, y2), Offset(size.width, y2), strokeWidth = 1f)
      }
      GridMode.OFF -> {}
    }
  }
}

@Composable
private fun BoxScope.HorizonLevelIndicator(
  rollDegrees: Float,
  isAligned: Boolean
) {
  // Only display if phone is reasonably upright (tilt within +/- 45 deg)
  if (abs(rollDegrees) > 40f) return

  val levelColor = if (isAligned) CameraGoldAccent else Color.White.copy(alpha = 0.55f)

  Box(
    modifier = Modifier
      .align(Alignment.Center)
      .size(width = 160.dp, height = 30.dp)
      .rotate(-rollDegrees)
      .testTag("horizon_level_indicator"),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val midY = size.height / 2f
      val barWidth = 52.dp.toPx()
      val strokeW = if (isAligned) 2.5f else 1.5f

      // Left bar
      drawLine(
        color = levelColor,
        start = Offset(10.dp.toPx(), midY),
        end = Offset(10.dp.toPx() + barWidth, midY),
        strokeWidth = strokeW
      )

      // Center crosshair / notch
      drawCircle(
        color = levelColor,
        radius = 2.dp.toPx(),
        center = Offset(size.width / 2f, midY)
      )

      // Right bar
      drawLine(
        color = levelColor,
        start = Offset(size.width - 10.dp.toPx() - barWidth, midY),
        end = Offset(size.width - 10.dp.toPx(), midY),
        strokeWidth = strokeW
      )
    }

    if (isAligned) {
      Text(
        text = "0°",
        color = CameraGoldAccent,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
          .align(Alignment.TopCenter)
          .offset(y = (-14).dp)
      )
    }
  }
}

@Composable
private fun BoxScope.FocusReticle(
  normX: Float,
  normY: Float,
  onExposureDrag: (Float) -> Unit
) {
  val scaleAnim = remember { Animatable(1.35f) }
  var sunOffsetY by remember { mutableFloatStateOf(0f) }

  LaunchedEffect(normX, normY) {
    scaleAnim.snapTo(1.35f)
    scaleAnim.animateTo(
      targetValue = 1.0f,
      animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
    )
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
  ) {
    Box(
      modifier = Modifier
        .align(Alignment.TopStart)
        .offset {
          // Map normalized coords into pixels
          // Note: using 70dp box offset centered around target
          IntOffset(
            x = (normX * 1080 - 100).roundToInt().coerceAtLeast(10),
            y = (normY * 1920 - 100).roundToInt().coerceAtLeast(10)
          )
        }
    ) {
      // Yellow Reticle Box
      Canvas(
        modifier = Modifier.size(68.dp)
      ) {
        val strokeW = 1.8.dp.toPx()
        val bracketLen = 14.dp.toPx()
        val c = CameraGoldAccent
        val w = size.width
        val h = size.height

        // Top Left corner
        drawLine(c, Offset(0f, 0f), Offset(bracketLen, 0f), strokeW)
        drawLine(c, Offset(0f, 0f), Offset(0f, bracketLen), strokeW)

        // Top Right corner
        drawLine(c, Offset(w, 0f), Offset(w - bracketLen, 0f), strokeW)
        drawLine(c, Offset(w, 0f), Offset(w, bracketLen), strokeW)

        // Bottom Left corner
        drawLine(c, Offset(0f, h), Offset(bracketLen, h), strokeW)
        drawLine(c, Offset(0f, h), Offset(0f, h - bracketLen), strokeW)

        // Bottom Right corner
        drawLine(c, Offset(w, h), Offset(w - bracketLen, h), strokeW)
        drawLine(c, Offset(w, h), Offset(w, h - bracketLen), strokeW)
      }

      // Draggable Sun Icon next to reticle
      Box(
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .offset(x = 36.dp, y = sunOffsetY.dp)
          .size(34.dp)
          .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
              change.consume()
              sunOffsetY = (sunOffsetY + dragAmount.y * 0.25f).coerceIn(-40f, 40f)
              // Up drag = higher exposure, down drag = lower exposure
              onExposureDrag(-dragAmount.y * 0.05f)
            }
          },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.WbSunny,
          contentDescription = "Adjust exposure",
          tint = CameraGoldAccent,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
