package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraMode
import com.example.model.FlashMode
import com.example.model.PhotographicStyle
import com.example.model.VideoFps
import com.example.model.VideoResolution
import com.example.ui.theme.CameraAmberAccent
import com.example.ui.theme.CameraGoldAccent
import com.example.ui.theme.CameraRedRecording
import com.example.ui.theme.CameraTextPrimary

@Composable
fun CameraTopBar(
  currentMode: CameraMode,
  flashMode: FlashMode,
  selectedStyle: PhotographicStyle,
  exposureEvText: String,
  isRecording: Boolean,
  recordingTimeText: String,
  isControlsPanelOpen: Boolean,
  videoResolution: VideoResolution,
  videoFps: VideoFps,
  onFlashToggle: () -> Unit,
  onStylesClick: () -> Unit,
  onToggleControlsPanel: () -> Unit,
  onResolutionToggle: () -> Unit,
  onFpsToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(56.dp)
      .padding(horizontal = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    if (isRecording) {
      // Recording HUD: Pulsing red dot + Timer
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .background(Color.Black.copy(alpha = 0.5f))
          .padding(horizontal = 12.dp, vertical = 6.dp)
          .testTag("recording_indicator")
      ) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(CameraRedRecording)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = recordingTimeText,
          color = CameraTextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
      }

      // Resolution & FPS badge
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "${videoResolution.label} · ${videoFps.fps}",
            color = CameraTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

    } else {
      // Normal Mode HUD:
      // Left: Flash toggle
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onFlashToggle,
          modifier = Modifier
            .size(42.dp)
            .testTag("top_bar_flash_toggle")
        ) {
          val (icon, tint) = when (flashMode) {
            FlashMode.AUTO -> Icons.Default.FlashAuto to Color.White.copy(alpha = 0.85f)
            FlashMode.ON -> Icons.Default.FlashOn to CameraGoldAccent
            FlashMode.OFF -> Icons.Default.FlashOff to Color.White.copy(alpha = 0.5f)
          }
          Icon(
            imageVector = icon,
            contentDescription = "Flash ${flashMode.displayName}",
            tint = tint,
            modifier = Modifier.size(22.dp)
          )
        }

        // Night mode indicator if in NIGHT mode
        if (currentMode == CameraMode.NIGHT) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(CameraGoldAccent.copy(alpha = 0.25f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Bedtime,
                contentDescription = "Night Mode",
                tint = CameraGoldAccent,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "AUTO",
                color = CameraGoldAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // Center: Expandable Liquid Glass Chevron Button
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(if (isControlsPanelOpen) CameraGoldAccent.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.35f))
          .clickable { onToggleControlsPanel() }
          .testTag("controls_panel_chevron_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isControlsPanelOpen) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
          contentDescription = if (isControlsPanelOpen) "Close controls panel" else "Open expanded controls panel",
          tint = if (isControlsPanelOpen) CameraGoldAccent else Color.White,
          modifier = Modifier.size(24.dp)
        )
      }

      // Right: Video resolution & FPS quick toggles (in video modes) OR Style / Exposure (in photo modes)
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (currentMode.isVideoMode) {
          // Resolution pill (e.g. 4K / HD)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color.White.copy(alpha = 0.18f))
              .clickable { onResolutionToggle() }
              .padding(horizontal = 7.dp, vertical = 4.dp)
              .testTag("video_resolution_quick_toggle")
          ) {
            Text(
              text = videoResolution.label,
              color = CameraTextPrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }

          // FPS pill (e.g. 30 / 60)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color.White.copy(alpha = 0.18f))
              .clickable { onFpsToggle() }
              .padding(horizontal = 7.dp, vertical = 4.dp)
              .testTag("video_fps_quick_toggle")
          ) {
            Text(
              text = videoFps.fps.toString(),
              color = CameraTextPrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        } else {
          // Photographic Style Badge
          IconButton(
            onClick = onStylesClick,
            modifier = Modifier
              .size(40.dp)
              .testTag("top_bar_style_button")
          ) {
            Icon(
              imageVector = Icons.Default.ColorLens,
              contentDescription = "Photographic Styles",
              tint = if (selectedStyle != PhotographicStyle.ORIGINAL) CameraAmberAccent else Color.White.copy(alpha = 0.85f),
              modifier = Modifier.size(22.dp)
            )
          }

          // Exposure indicator (e.g. +0.0)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color.White.copy(alpha = 0.15f))
              .padding(horizontal = 7.dp, vertical = 4.dp)
              .testTag("top_bar_exposure_badge")
          ) {
            Text(
              text = exposureEvText,
              color = CameraTextPrimary,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }
  }
}
