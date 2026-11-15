package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.model.CameraMode
import com.example.model.CapturedMedia
import com.example.ui.theme.CameraGlassBorder

/**
 * iOS 26 Camera Bottom Controls:
 * - Round Liquid Glass photo preview button on the left
 * - Liquid Glass Shutter button with visible outer glass rim and side edges
 * - Round Liquid Glass camera flip button on the right
 */
@Composable
fun CameraBottomControls(
  currentMode: CameraMode,
  isRecording: Boolean,
  recentMedia: CapturedMedia?,
  onShutterClick: () -> Unit,
  onSwitchCamera: () -> Unit,
  onOpenGallery: () -> Unit,
  modifier: Modifier = Modifier
) {
  var flipRotation by remember { mutableFloatStateOf(0f) }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // 1. Round Media Preview Button (Changed from square to round in iOS 26)
    Box(
      modifier = Modifier
        .size(54.dp)
        .shadow(elevation = 6.dp, shape = CircleShape, clip = false)
        .clip(CircleShape)
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0x52232938),
              Color(0x380F131D)
            )
          )
        )
        .border(
          width = 1.2.dp,
          brush = Brush.linearGradient(
            colors = listOf(
              Color(0x80FFFFFF),
              CameraGlassBorder,
              Color(0x26FFFFFF)
            )
          ),
          shape = CircleShape
        )
        .clickable { onOpenGallery() }
        .testTag("gallery_thumbnail_button"),
      contentAlignment = Alignment.Center
    ) {
      if (recentMedia != null) {
        AsyncImage(
          model = recentMedia.uri,
          contentDescription = "Recent photo thumbnail",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
        )
      } else {
        Icon(
          imageVector = Icons.Default.PhotoLibrary,
          contentDescription = "Open Gallery",
          tint = Color.White.copy(alpha = 0.8f),
          modifier = Modifier.size(24.dp)
        )
      }
    }

    // 2. Liquid Glass Shutter Button
    // Outer glass rim, visible side edges, reflections, tactile feedback
    LiquidGlassShutterButton(
      isVideoMode = currentMode.isVideoMode,
      isRecording = isRecording,
      onClick = onShutterClick,
      modifier = Modifier.testTag("camera_shutter_button")
    )

    // 3. Round Camera Lens Switch (Flip) Button
    val animatedRotation by animateFloatAsState(
      targetValue = flipRotation,
      animationSpec = spring(),
      label = "flip_rotation"
    )

    Box(
      modifier = Modifier
        .size(54.dp)
        .shadow(elevation = 6.dp, shape = CircleShape, clip = false)
        .clip(CircleShape)
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0x52232938),
              Color(0x380F131D)
            )
          )
        )
        .border(
          width = 1.2.dp,
          brush = Brush.linearGradient(
            colors = listOf(
              Color(0x80FFFFFF),
              CameraGlassBorder,
              Color(0x26FFFFFF)
            )
          ),
          shape = CircleShape
        )
        .clickable {
          flipRotation += 180f
          onSwitchCamera()
        }
        .testTag("switch_camera_button"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Cameraswitch,
        contentDescription = "Switch camera lens",
        tint = Color.White,
        modifier = Modifier
          .size(24.dp)
          .rotate(animatedRotation)
      )
    }
  }
}
