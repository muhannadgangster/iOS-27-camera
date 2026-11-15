package com.example.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VideoFps
import com.example.model.VideoResolution
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.theme.CameraDeepBlack
import com.example.ui.theme.CameraGlassBorder
import com.example.ui.theme.CameraGoldAccent
import com.example.ui.theme.CameraTextPrimary
import com.example.ui.theme.CameraTextSecondary

@Composable
fun CameraSettingsSheet(
  videoResolution: VideoResolution,
  videoFps: VideoFps,
  isStabilizationEnabled: Boolean,
  isLevelEnabled: Boolean,
  isLiquidGlassEnabled: Boolean,
  isGlassReflectionsEnabled: Boolean,
  isBgRefractionEnabled: Boolean,
  isMotionEffectsEnabled: Boolean,
  isPerformanceModeEnabled: Boolean,
  animationQuality: String,
  onVideoResolutionChange: (VideoResolution) -> Unit,
  onVideoFpsChange: (VideoFps) -> Unit,
  onStabilizationToggle: (Boolean) -> Unit,
  onLevelToggle: (Boolean) -> Unit,
  onLiquidGlassToggle: (Boolean) -> Unit,
  onGlassReflectionsToggle: (Boolean) -> Unit,
  onBgRefractionToggle: (Boolean) -> Unit,
  onMotionEffectsToggle: (Boolean) -> Unit,
  onPerformanceModeToggle: (Boolean) -> Unit,
  onAnimationQualityChange: (String) -> Unit,
  onResetAllSettings: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  var saveFormat by remember { mutableStateOf("JPEG") }
  var videoCodec by remember { mutableStateOf("H.264") }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CameraDeepBlack.copy(alpha = 0.88f))
      .padding(16.dp),
    contentAlignment = Alignment.Center
  ) {
    LiquidGlassSurface(
      modifier = Modifier
        .fillMaxWidth()
        .height(650.dp)
        .testTag("camera_settings_dialog"),
      shape = RoundedCornerShape(28.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Camera Settings",
              color = CameraTextPrimary,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Hardware capture, media & display options",
              color = CameraTextSecondary,
              fontSize = 12.sp
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("dismiss_settings_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close settings",
              tint = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // SECTION 1: FORMATS & CAPTURE
          item {
            SettingsSectionHeader("FORMATS & CAPTURE")
          }

          item {
            SettingsCard {
              SettingsRowWithPicker(
                label = "Photo Format",
                description = "Maximum compatibility vs high efficiency",
                options = listOf("JPEG", "HEIF"),
                selected = saveFormat,
                onSelected = { saveFormat = it }
              )
              HorizontalDivider(color = CameraGlassBorder.copy(alpha = 0.3f), thickness = 0.8.dp)
              SettingsRowWithPicker(
                label = "Video Codec",
                description = "Standard AVC vs HEVC High Efficiency",
                options = listOf("H.264", "H.265"),
                selected = videoCodec,
                onSelected = { videoCodec = it }
              )
            }
          }

          // SECTION 2: VIDEO RECORDING
          item {
            SettingsSectionHeader("VIDEO RECORDING DEFAULTS")
          }

          item {
            SettingsCard {
              SettingsRowWithPicker(
                label = "Default Video Resolution",
                description = "Real camera output size",
                options = listOf("720p", "1080p", "4K"),
                selected = videoResolution.label,
                onSelected = { label ->
                  val res = when (label) {
                    "4K" -> VideoResolution.RES_4K
                    "1080p" -> VideoResolution.RES_1080P
                    else -> VideoResolution.RES_720P
                  }
                  onVideoResolutionChange(res)
                }
              )
              HorizontalDivider(color = CameraGlassBorder.copy(alpha = 0.3f), thickness = 0.8.dp)
              SettingsRowWithPicker(
                label = "Video Frame Rate",
                description = "Cinema 24fps vs Standard 30fps vs Smooth 60fps",
                options = listOf("24", "30", "60"),
                selected = videoFps.fps.toString(),
                onSelected = { fpsStr ->
                  val fps = when (fpsStr) {
                    "60" -> VideoFps.FPS_60
                    "24" -> VideoFps.FPS_24
                    else -> VideoFps.FPS_30
                  }
                  onVideoFpsChange(fps)
                }
              )
              HorizontalDivider(color = CameraGlassBorder.copy(alpha = 0.3f), thickness = 0.8.dp)
              SettingsToggleRow(
                label = "Enhanced Video Stabilization",
                description = "Electronic and optical sensor stabilization",
                checked = isStabilizationEnabled,
                onCheckedChange = onStabilizationToggle
              )
            }
          }

          // SECTION 3: COMPOSITION & SENSORS
          item {
            SettingsSectionHeader("COMPOSITION & SENSORS")
          }

          item {
            SettingsCard {
              SettingsToggleRow(
                label = "Sensor Horizon Level",
                description = "Real-time tilt angle with haptic feedback tick",
                checked = isLevelEnabled,
                onCheckedChange = onLevelToggle
              )
            }
          }

          // SECTION 4: ANIMATION & LIQUID GLASS
          item {
            SettingsSectionHeader("ANIMATION & LIQUID GLASS")
          }

          item {
            SettingsCard {
              SettingsRowWithPicker(
                label = "Animation Quality",
                description = "Rendering fidelity for transitions and menus",
                options = listOf("Auto", "High", "Balanced"),
                selected = animationQuality,
                onSelected = onAnimationQualityChange
              )
              HorizontalDivider(color = CameraGlassBorder.copy(alpha = 0.3f), thickness = 0.8.dp)
              SettingsToggleRow(
                label = "Liquid Glass Surfaces",
                description = "Translucent frosted glass depth",
                checked = isLiquidGlassEnabled,
                onCheckedChange = onLiquidGlassToggle
              )
              HorizontalDivider(color = CameraGlassBorder.copy(alpha = 0.3f), thickness = 0.8.dp)
              SettingsToggleRow(
                label = "Glass Specular Reflections",
                description = "Dynamic ambient highlights along 3D curves",
                checked = isGlassReflectionsEnabled,
                onCheckedChange = onGlassReflectionsToggle
              )
              HorizontalDivider(color = CameraGlassBorder.copy(alpha = 0.3f), thickness = 0.8.dp)
              SettingsToggleRow(
                label = "Background Refraction",
                description = "Subtle depth distortion behind panels",
                checked = isBgRefractionEnabled,
                onCheckedChange = onBgRefractionToggle
              )
              HorizontalDivider(color = CameraGlassBorder.copy(alpha = 0.3f), thickness = 0.8.dp)
              SettingsToggleRow(
                label = "Motion Effects",
                description = "Fluid springs on button presses and dials",
                checked = isMotionEffectsEnabled,
                onCheckedChange = onMotionEffectsToggle
              )
              HorizontalDivider(color = CameraGlassBorder.copy(alpha = 0.3f), thickness = 0.8.dp)
              SettingsToggleRow(
                label = "Performance Mode",
                description = "Prioritize thermal stability and battery life",
                checked = isPerformanceModeEnabled,
                onCheckedChange = onPerformanceModeToggle
              )
            }
          }

          // SECTION 5: STORAGE & PRIVACY
          item {
            SettingsSectionHeader("STORAGE & 100% OFFLINE PRIVACY")
          }

          item {
            SettingsCard {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Info,
                  contentDescription = "Offline Info",
                  tint = CameraGoldAccent,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "Strictly 100% Offline",
                    color = CameraTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "All captures are stored locally in Pictures/Camera and Movies/Camera via Android MediaStore. Zero cloud or network transmission.",
                    color = CameraTextSecondary,
                    fontSize = 11.sp
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Reset Settings button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Button(
            onClick = {
              onResetAllSettings()
              onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("reset_all_settings_button")
          ) {
            Icon(
              imageVector = Icons.Default.RestartAlt,
              contentDescription = "Reset Settings",
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Reset All Settings", color = Color.White, fontSize = 13.sp)
          }

          Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = CameraGoldAccent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("done_settings_button")
          ) {
            Text(text = "Done", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
private fun SettingsSectionHeader(title: String) {
  Text(
    text = title,
    color = CameraGoldAccent,
    fontSize = 11.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.8.sp,
    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
  )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(Color.White.copy(alpha = 0.08f))
      .border(1.dp, CameraGlassBorder, RoundedCornerShape(16.dp))
      .padding(14.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      content()
    }
  }
}

@Composable
private fun SettingsToggleRow(
  label: String,
  description: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
      Text(text = label, color = CameraTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
      Text(text = description, color = CameraTextSecondary, fontSize = 11.sp)
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color.Black,
        checkedTrackColor = CameraGoldAccent
      )
    )
  }
}

@Composable
private fun SettingsRowWithPicker(
  label: String,
  description: String,
  options: List<String>,
  selected: String,
  onSelected: (String) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
      Text(text = label, color = CameraTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
      Text(text = description, color = CameraTextSecondary, fontSize = 11.sp)
    }

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
      options.forEach { option ->
        val isOptSelected = option == selected
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isOptSelected) CameraGoldAccent else Color.White.copy(alpha = 0.12f))
            .border(0.8.dp, if (isOptSelected) CameraGoldAccent else CameraGlassBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = option,
            color = if (isOptSelected) Color.Black else CameraTextPrimary,
            fontSize = 11.sp,
            fontWeight = if (isOptSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.testTag("setting_${label.replace(" ", "_").lowercase()}_$option")
          )
        }
      }
    }
  }
}
