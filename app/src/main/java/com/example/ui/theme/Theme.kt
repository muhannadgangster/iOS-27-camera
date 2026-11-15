package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CameraColorScheme = darkColorScheme(
  primary = CameraGoldAccent,
  onPrimary = Color.Black,
  primaryContainer = CameraPillActiveBg,
  onPrimaryContainer = CameraGoldAccent,
  secondary = CameraAmberAccent,
  onSecondary = Color.Black,
  tertiary = CameraBlueFilter,
  background = CameraDeepBlack,
  onBackground = CameraTextPrimary,
  surface = CameraOnyxDark,
  onSurface = CameraTextPrimary,
  surfaceVariant = CameraSurfaceGlass,
  onSurfaceVariant = CameraTextSecondary,
  error = CameraRedRecording,
  onError = Color.White
)

@Composable
fun CameraTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = CameraColorScheme,
    typography = CameraTypography,
    content = content
  )
}
