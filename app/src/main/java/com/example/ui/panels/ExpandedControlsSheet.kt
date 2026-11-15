package com.example.ui.panels

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Exposure
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatioSetting
import com.example.model.CameraControlId
import com.example.model.ControlItem
import com.example.model.FilterAdjustments
import com.example.model.FlashMode
import com.example.model.GridMode
import com.example.model.PhotographicStyle
import com.example.model.TimerSetting
import com.example.model.VideoFps
import com.example.model.VideoResolution
import com.example.ui.components.LiquidGlassPill
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.theme.CameraDeepBlack
import com.example.ui.theme.CameraGlassBorder
import com.example.ui.theme.CameraGoldAccent
import com.example.ui.theme.CameraTextPrimary
import com.example.ui.theme.CameraTextSecondary

@Composable
fun ExpandedControlsSheet(
  customControls: List<ControlItem>,
  flashMode: FlashMode,
  timerSetting: TimerSetting,
  aspectRatio: AspectRatioSetting,
  gridMode: GridMode,
  isLevelEnabled: Boolean,
  exposureIndex: Int,
  minExposure: Int,
  maxExposure: Int,
  exposureStep: Float,
  selectedStyle: PhotographicStyle,
  filterAdjustments: FilterAdjustments,
  videoResolution: VideoResolution,
  videoFps: VideoFps,
  supportedResolutions: List<VideoResolution>,
  supportedFps: List<VideoFps>,
  isStabilizationEnabled: Boolean,
  isLiquidGlassEnabled: Boolean,
  isGlassReflectionsEnabled: Boolean,
  onFlashChange: (FlashMode) -> Unit,
  onTimerChange: (TimerSetting) -> Unit,
  onAspectRatioChange: (AspectRatioSetting) -> Unit,
  onGridModeChange: (GridMode) -> Unit,
  onLevelToggle: (Boolean) -> Unit,
  onExposureChange: (Int) -> Unit,
  onStyleSelected: (PhotographicStyle) -> Unit,
  onFilterAdjustmentsChange: (FilterAdjustments) -> Unit,
  onResetFilterAdjustments: () -> Unit,
  onVideoResolutionChange: (VideoResolution) -> Unit,
  onVideoFpsChange: (VideoFps) -> Unit,
  onStabilizationToggle: (Boolean) -> Unit,
  onOpenCustomizeControls: () -> Unit,
  onOpenSettings: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedControl by remember { mutableStateOf<CameraControlId?>(CameraControlId.FLASH) }

  LiquidGlassSurface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 8.dp)
      .testTag("expanded_controls_panel"),
    shape = RoundedCornerShape(26.dp),
    isGlassEnabled = isLiquidGlassEnabled,
    isReflectionEnabled = isGlassReflectionsEnabled
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 14.dp, horizontal = 12.dp)
    ) {
      // 1. Control Icons Horizontal Carousel
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        val enabledControls = customControls.filter { it.isEnabled }
        items(enabledControls, key = { it.id.name }) { item ->
          val isCurrent = selectedControl == item.id
          val icon = getControlIcon(item.id)

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .clickable {
                selectedControl = if (isCurrent) null else item.id
              }
              .testTag("control_icon_${item.id.name.lowercase()}")
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                  if (isCurrent) {
                    Brush.verticalGradient(
                      listOf(CameraGoldAccent, CameraGoldAccent.copy(alpha = 0.85f))
                    )
                  } else {
                    Brush.verticalGradient(
                      listOf(Color(0x52232938), Color(0x380F131D))
                    )
                  }
                )
                .border(
                  width = 1.2.dp,
                  brush = Brush.linearGradient(
                    if (isCurrent) {
                      listOf(Color.White, CameraGoldAccent)
                    } else {
                      listOf(Color(0x80FFFFFF), Color(0x26FFFFFF))
                    }
                  ),
                  shape = CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = icon,
                contentDescription = item.title,
                tint = if (isCurrent) Color.Black else Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = item.title,
              color = if (isCurrent) CameraGoldAccent else CameraTextSecondary,
              fontSize = 10.sp,
              fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Dedicated Interactive Options Sub-Panel
      AnimatedVisibility(
        visible = selectedControl != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, CameraGlassBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
        ) {
          when (selectedControl) {
            CameraControlId.FLASH -> {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
              ) {
                FlashMode.entries.forEach { mode ->
                  val isSelected = flashMode == mode
                  LiquidGlassPill(
                    isSelected = isSelected,
                    modifier = Modifier
                      .weight(1f)
                      .padding(horizontal = 4.dp)
                      .clickable { onFlashChange(mode) }
                      .testTag("flash_option_${mode.name.lowercase()}")
                  ) {
                    Text(
                      text = mode.displayName,
                      color = if (isSelected) CameraGoldAccent else CameraTextPrimary,
                      fontSize = 13.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      modifier = Modifier
                        .align(Alignment.Center)
                        .padding(vertical = 8.dp)
                    )
                  }
                }
              }
            }

            CameraControlId.TIMER -> {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
              ) {
                TimerSetting.entries.forEach { setting ->
                  val isSelected = timerSetting == setting
                  LiquidGlassPill(
                    isSelected = isSelected,
                    modifier = Modifier
                      .weight(1f)
                      .padding(horizontal = 4.dp)
                      .clickable { onTimerChange(setting) }
                      .testTag("timer_option_${setting.name.lowercase()}")
                  ) {
                    Text(
                      text = setting.label,
                      color = if (isSelected) CameraGoldAccent else CameraTextPrimary,
                      fontSize = 13.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      modifier = Modifier
                        .align(Alignment.Center)
                        .padding(vertical = 8.dp)
                    )
                  }
                }
              }
            }

            CameraControlId.EXPOSURE -> {
              Column(modifier = Modifier.fillMaxWidth()) {
                val currentEv = exposureIndex * exposureStep
                val evText = if (currentEv >= 0f) "+%.1f".format(currentEv) else "%.1f".format(currentEv)

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Exposure Value: $evText EV",
                    color = CameraGoldAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                  )
                  IconButton(
                    onClick = { onExposureChange(0) },
                    modifier = Modifier.size(32.dp).testTag("reset_exposure_button")
                  ) {
                    Icon(
                      imageVector = Icons.Default.RestartAlt,
                      contentDescription = "Reset Exposure",
                      tint = Color.White.copy(alpha = 0.7f),
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }

                Slider(
                  value = exposureIndex.toFloat(),
                  onValueChange = { onExposureChange(it.toInt()) },
                  valueRange = minExposure.toFloat()..maxExposure.toFloat(),
                  steps = (maxExposure - minExposure) - 1,
                  colors = SliderDefaults.colors(
                    thumbColor = CameraGoldAccent,
                    activeTrackColor = CameraGoldAccent,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                  ),
                  modifier = Modifier.fillMaxWidth().testTag("exposure_slider")
                )
              }
            }

            CameraControlId.STYLES -> {
              PhotographicStylesPanel(
                selectedStyle = selectedStyle,
                adjustments = filterAdjustments,
                onStyleSelected = onStyleSelected,
                onAdjustmentsChange = onFilterAdjustmentsChange,
                onResetAdjustments = onResetFilterAdjustments
              )
            }

            CameraControlId.ASPECT_RATIO -> {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
              ) {
                AspectRatioSetting.entries.forEach { ratio ->
                  val isSelected = aspectRatio == ratio
                  LiquidGlassPill(
                    isSelected = isSelected,
                    modifier = Modifier
                      .weight(1f)
                      .padding(horizontal = 4.dp)
                      .clickable { onAspectRatioChange(ratio) }
                      .testTag("aspect_ratio_option_${ratio.name.lowercase()}")
                  ) {
                    Text(
                      text = ratio.label,
                      color = if (isSelected) CameraGoldAccent else CameraTextPrimary,
                      fontSize = 13.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      modifier = Modifier
                        .align(Alignment.Center)
                        .padding(vertical = 8.dp)
                    )
                  }
                }
              }
            }

            CameraControlId.GRID -> {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
              ) {
                GridMode.entries.forEach { mode ->
                  val isSelected = gridMode == mode
                  LiquidGlassPill(
                    isSelected = isSelected,
                    modifier = Modifier
                      .weight(1f)
                      .padding(horizontal = 4.dp)
                      .clickable { onGridModeChange(mode) }
                      .testTag("grid_option_${mode.name.lowercase()}")
                  ) {
                    Text(
                      text = mode.label,
                      color = if (isSelected) CameraGoldAccent else CameraTextPrimary,
                      fontSize = 12.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      modifier = Modifier
                        .align(Alignment.Center)
                        .padding(vertical = 8.dp)
                    )
                  }
                }
              }
            }

            CameraControlId.LEVEL -> {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Sensor Horizon Level Guide",
                  color = CameraTextPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium
                )
                Switch(
                  checked = isLevelEnabled,
                  onCheckedChange = onLevelToggle,
                  colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = CameraGoldAccent,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                  ),
                  modifier = Modifier.testTag("level_guide_switch")
                )
              }
            }

            CameraControlId.RESOLUTION -> {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
              ) {
                supportedResolutions.forEach { res ->
                  val isSelected = videoResolution == res
                  LiquidGlassPill(
                    isSelected = isSelected,
                    modifier = Modifier
                      .weight(1f)
                      .padding(horizontal = 4.dp)
                      .clickable { onVideoResolutionChange(res) }
                      .testTag("res_option_${res.name.lowercase()}")
                  ) {
                    Text(
                      text = "${res.label} (${res.width}p)",
                      color = if (isSelected) CameraGoldAccent else CameraTextPrimary,
                      fontSize = 12.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      modifier = Modifier
                        .align(Alignment.Center)
                        .padding(vertical = 8.dp)
                    )
                  }
                }
              }
            }

            CameraControlId.FPS -> {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
              ) {
                supportedFps.forEach { fps ->
                  val isSelected = videoFps == fps
                  LiquidGlassPill(
                    isSelected = isSelected,
                    modifier = Modifier
                      .weight(1f)
                      .padding(horizontal = 4.dp)
                      .clickable { onVideoFpsChange(fps) }
                      .testTag("fps_option_${fps.name.lowercase()}")
                  ) {
                    Text(
                      text = fps.label,
                      color = if (isSelected) CameraGoldAccent else CameraTextPrimary,
                      fontSize = 12.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      modifier = Modifier
                        .align(Alignment.Center)
                        .padding(vertical = 8.dp)
                    )
                  }
                }
              }
            }

            CameraControlId.STABILIZATION -> {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Video EIS / Stabilization",
                  color = CameraTextPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium
                )
                Switch(
                  checked = isStabilizationEnabled,
                  onCheckedChange = onStabilizationToggle,
                  colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = CameraGoldAccent
                  ),
                  modifier = Modifier.testTag("stabilization_switch")
                )
              }
            }

            CameraControlId.NIGHT -> {
              Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                  text = "Night Mode Shutter Exposure",
                  color = CameraGoldAccent,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Intelligent low-light long exposure computational enhancement",
                  color = CameraTextSecondary,
                  fontSize = 11.sp
                )
              }
            }

            CameraControlId.MACRO -> {
              Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                  text = "Macro Close-Up Focus",
                  color = CameraGoldAccent,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Optimizes minimum lens focal distance for close-up subjects",
                  color = CameraTextSecondary,
                  fontSize = 11.sp
                )
              }
            }

            null -> {}
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 3. Panel Action Footer
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          // Customize Controls button
          Button(
            onClick = onOpenCustomizeControls,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("open_customize_controls_button")
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Customize Controls",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Customize", color = Color.White, fontSize = 12.sp)
          }

          // Camera Settings button
          Button(
            onClick = onOpenSettings,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("open_camera_settings_button")
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Camera Settings",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Settings", color = Color.White, fontSize = 12.sp)
          }
        }

        // Done button
        Button(
          onClick = onClose,
          colors = ButtonDefaults.buttonColors(containerColor = CameraGoldAccent),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("close_controls_panel_button")
        ) {
          Icon(
            imageVector = Icons.Default.Done,
            contentDescription = "Done",
            tint = Color.Black,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "Done", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

private fun getControlIcon(id: CameraControlId): ImageVector {
  return when (id) {
    CameraControlId.FLASH -> Icons.Default.FlashOn
    CameraControlId.NIGHT -> Icons.Default.Bedtime
    CameraControlId.TIMER -> Icons.Default.Timer
    CameraControlId.EXPOSURE -> Icons.Default.Exposure
    CameraControlId.STYLES -> Icons.Default.ColorLens
    CameraControlId.ASPECT_RATIO -> Icons.Default.AspectRatio
    CameraControlId.GRID -> Icons.Default.GridOn
    CameraControlId.LEVEL -> Icons.Default.Straighten
    CameraControlId.RESOLUTION -> Icons.Default.HighQuality
    CameraControlId.FPS -> Icons.Default.Speed
    CameraControlId.STABILIZATION -> Icons.Default.Vibration
    CameraControlId.MACRO -> Icons.Default.ZoomIn
  }
}
