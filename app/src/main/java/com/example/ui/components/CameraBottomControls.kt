package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.model.CameraMode
import com.example.model.CapturedMedia
import com.example.ui.theme.CameraGlassBorder
import com.example.ui.theme.CameraRedRecording

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
      .padding(horizontal = 24.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // 1. Gallery Thumbnail
    Box(
      modifier = Modifier
        .size(52.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(Color.White.copy(alpha = 0.12f))
        .border(1.dp, CameraGlassBorder, RoundedCornerShape(14.dp))
        .clickable { onOpenGallery() }
        .testTag("gallery_thumbnail_button"),
      contentAlignment = Alignment.Center
    ) {
      if (recentMedia != null) {
        AsyncImage(
          model = recentMedia.uri,
          contentDescription = "Recent photo thumbnail",
          contentScale = ContentScale.Crop,
          modifier = Modifier.clip(RoundedCornerShape(14.dp))
        )
      } else {
        Icon(
          imageVector = Icons.Default.PhotoLibrary,
          contentDescription = "Open Gallery",
          tint = Color.White.copy(alpha = 0.7f),
          modifier = Modifier.size(26.dp)
        )
      }
    }

    // 2. Large Circular Shutter Button
    val isVideo = currentMode.isVideoMode
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
      targetValue = if (isPressed) 0.90f else 1.0f,
      animationSpec = spring(),
      label = "shutter_scale"
    )

    val innerCornerRadius by animateDpAsState(
      targetValue = if (isVideo && isRecording) 8.dp else 32.dp,
      animationSpec = tween(250, easing = FastOutSlowInEasing),
      label = "inner_corner"
    )

    val innerSize by animateDpAsState(
      targetValue = if (isVideo && isRecording) 30.dp else 64.dp,
      animationSpec = tween(250, easing = FastOutSlowInEasing),
      label = "inner_size"
    )

    Box(
      modifier = Modifier
        .size(80.dp)
        .scale(scale)
        .border(4.dp, Color.White, CircleShape)
        .padding(4.dp)
        .clickable(
          interactionSource = interactionSource,
          indication = null
        ) { onShutterClick() }
        .testTag("camera_shutter_button"),
      contentAlignment = Alignment.Center
    ) {
      val innerColor = if (isVideo) CameraRedRecording else Color.White

      Box(
        modifier = Modifier
          .size(innerSize)
          .clip(RoundedCornerShape(innerCornerRadius))
          .background(innerColor)
      )
    }

    // 3. Camera Lens Switch (Flip) Button
    val animatedRotation by animateFloatAsState(
      targetValue = flipRotation,
      animationSpec = spring(),
      label = "flip_rotation"
    )

    Box(
      modifier = Modifier
        .size(52.dp)
        .clip(CircleShape)
        .background(Color.White.copy(alpha = 0.12f))
        .border(1.dp, CameraGlassBorder, CircleShape)
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
          .size(26.dp)
          .rotate(animatedRotation)
      )
    }
  }
}
