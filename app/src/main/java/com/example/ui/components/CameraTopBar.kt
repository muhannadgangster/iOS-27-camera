package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.theme.CameraGlassBorder
import com.example.ui.theme.CameraGoldAccent
import com.example.ui.theme.CameraRedRecording
import com.example.ui.theme.CameraTextPrimary

/**
 * iOS 26 Camera Top HUD:
 * Minimal floating Liquid Glass quick-toggles in the corners,
 * centered expandable pop-out chevron button, and live recording HUD.
 */
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
          .shadow(elevation = 6.dp, shape = RoundedCornerShape(14.dp))
          .clip(RoundedCornerShape(14.dp))
          .background(Color.Black.copy(alpha = 0.6f))
          .border(1.dp, Color(0x33FF3B30), RoundedCornerShape(14.dp))
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
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x38FFFFFF))
            .border(0.8.dp, Color(0x66FFFFFF), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
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
      // Left: Flash toggle with floating Liquid Glass circle
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        val (icon, tint, isFlashActive) = when (flashMode) {
          FlashMode.AUTO -> Triple(Icons.Default.FlashAuto, Color.White.copy(alpha = 0.9f), false)
          FlashMode.ON -> Triple(Icons.Default.FlashOn, CameraGoldAccent, true)
          FlashMode.OFF -> Triple(Icons.Default.FlashOff, Color.White.copy(alpha = 0.55f), false)
        }

        Box(
          modifier = Modifier
            .size(38.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(
              if (isFlashActive) CameraGoldAccent.copy(alpha = 0.25f)
              else Color(0x401D2230)
            )
            .border(
              width = 1.dp,
              brush = Brush.linearGradient(
                listOf(
                  if (isFlashActive) CameraGoldAccent else Color(0x80FFFFFF),
                  Color(0x1AFFFFFF)
                )
              ),
              shape = CircleShape
            )
            .clickable { onFlashToggle() }
            .testTag("top_bar_flash_toggle"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = "Flash ${flashMode.displayName}",
            tint = tint,
            modifier = Modifier.size(20.dp)
          )
        }

        // Night mode indicator if in NIGHT mode
        if (currentMode == CameraMode.NIGHT) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(CameraGoldAccent.copy(alpha = 0.25f))
              .border(0.8.dp, CameraGoldAccent, RoundedCornerShape(12.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Bedtime,
                contentDescription = "Night Mode",
                tint = CameraGoldAccent,
                modifier = Modifier.size(14.dp)
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

      // Center: Expandable Liquid Glass Pop-out Chevron Button
      Box(
        modifier = Modifier
          .size(38.dp)
          .shadow(4.dp, CircleShape)
          .clip(CircleShape)
          .background(
            if (isControlsPanelOpen) CameraGoldAccent.copy(alpha = 0.35f)
            else Color(0x401D2230)
          )
          .border(
            width = 1.dp,
            brush = Brush.linearGradient(
              listOf(
                if (isControlsPanelOpen) CameraGoldAccent else Color(0x80FFFFFF),
                Color(0x26FFFFFF)
              )
            ),
            shape = CircleShape
          )
          .clickable { onToggleControlsPanel() }
          .testTag("controls_panel_chevron_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isControlsPanelOpen) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
          contentDescription = if (isControlsPanelOpen) "Close pop-out controls" else "Open pop-out controls",
          tint = if (isControlsPanelOpen) CameraGoldAccent else Color.White,
          modifier = Modifier.size(22.dp)
        )
      }

      // Right: Photographic Styles & Exposure Quick Toggles
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (currentMode.isVideoMode) {
          // Resolution pill (e.g. 4K / HD)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0x401D2230))
              .border(1.dp, Color(0x4DFFFFFF), RoundedCornerShape(10.dp))
              .clickable { onResolutionToggle() }
              .padding(horizontal = 8.dp, vertical = 5.dp)
              .testTag("video_resolution_quick_toggle"),
            contentAlignment = Alignment.Center
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
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0x401D2230))
              .border(1.dp, Color(0x4DFFFFFF), RoundedCornerShape(10.dp))
              .clickable { onFpsToggle() }
              .padding(horizontal = 8.dp, vertical = 5.dp)
              .testTag("video_fps_quick_toggle"),
            contentAlignment = Alignment.Center
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
          val hasStyle = selectedStyle != PhotographicStyle.ORIGINAL
          Box(
            modifier = Modifier
              .size(38.dp)
              .shadow(4.dp, CircleShape)
              .clip(CircleShape)
              .background(
                if (hasStyle) CameraAmberAccent.copy(alpha = 0.25f)
                else Color(0x401D2230)
              )
              .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                  listOf(
                    if (hasStyle) CameraAmberAccent else Color(0x80FFFFFF),
                    Color(0x1AFFFFFF)
                  )
                ),
                shape = CircleShape
              )
              .clickable { onStylesClick() }
              .testTag("top_bar_style_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ColorLens,
              contentDescription = "Photographic Styles",
              tint = if (hasStyle) CameraAmberAccent else Color.White.copy(alpha = 0.85f),
              modifier = Modifier.size(20.dp)
            )
          }

          // Exposure indicator pill (e.g. +0.0)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0x401D2230))
              .border(1.dp, Color(0x4DFFFFFF), RoundedCornerShape(10.dp))
              .padding(horizontal = 8.dp, vertical = 5.dp)
              .testTag("top_bar_exposure_badge"),
            contentAlignment = Alignment.Center
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
