package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CameraDeepBlack
import com.example.ui.theme.CameraGoldAccent
import com.example.ui.theme.CameraGlassBorder
import com.example.ui.theme.CameraTextPrimary
import kotlin.math.abs

@Composable
fun CameraZoomControl(
  zoomRatio: Float,
  minZoom: Float,
  maxZoom: Float,
  onZoomChange: (Float) -> Unit,
  modifier: Modifier = Modifier
) {
  var showZoomSlider by remember { mutableStateOf(false) }

  // Available fixed zoom stops based on device capabilities
  val zoomStops = remember(minZoom, maxZoom) {
    val list = mutableListOf<Float>()
    if (minZoom < 1.0f) list.add(0.5f)
    list.add(1.0f)
    if (maxZoom >= 2.0f) list.add(2.0f)
    if (maxZoom >= 5.0f) list.add(5.0f)
    list
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Zoom Slider when expanded
    AnimatedVisibility(
      visible = showZoomSlider,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(0.85f)
          .clip(RoundedCornerShape(20.dp))
          .background(CameraDeepBlack.copy(alpha = 0.65f))
          .border(1.dp, CameraGlassBorder, RoundedCornerShape(20.dp))
          .padding(horizontal = 16.dp, vertical = 6.dp)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "%.1f×".format(zoomRatio),
            color = CameraGoldAccent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
          Slider(
            value = zoomRatio,
            onValueChange = onZoomChange,
            valueRange = minZoom..maxZoom.coerceAtLeast(minZoom + 0.5f),
            colors = SliderDefaults.colors(
              thumbColor = CameraGoldAccent,
              activeTrackColor = CameraGoldAccent,
              inactiveTrackColor = Color.White.copy(alpha = 0.25f)
            ),
            modifier = Modifier.testTag("zoom_fine_slider")
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // iOS 27 Zoom Pills
    Box(
      modifier = Modifier
        .clip(CircleShape)
        .background(Color.Black.copy(alpha = 0.45f))
        .border(0.8.dp, CameraGlassBorder, CircleShape)
        .padding(horizontal = 4.dp, vertical = 4.dp)
        .pointerInput(Unit) {
          detectDragGestures(
            onDragStart = { showZoomSlider = true },
            onDragEnd = { },
            onDrag = { change, dragAmount ->
              change.consume()
              val delta = dragAmount.x * 0.02f
              val newZoom = (zoomRatio + delta).coerceIn(minZoom, maxZoom)
              onZoomChange(newZoom)
            }
          )
        }
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        zoomStops.forEach { stop ->
          val isSelected = abs(zoomRatio - stop) < 0.15f
          val label = if (stop == 0.5f) ".5" else stop.toInt().toString()

          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(if (isSelected) CameraGoldAccent else Color.Transparent)
              .clickable {
                onZoomChange(stop)
                showZoomSlider = false
              }
              .testTag("zoom_pill_$label"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (isSelected && abs(zoomRatio - stop) > 0.1f) "%.1f".format(zoomRatio) else label,
              color = if (isSelected) Color.Black else CameraTextPrimary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
