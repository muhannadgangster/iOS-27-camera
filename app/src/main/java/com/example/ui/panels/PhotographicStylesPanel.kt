package com.example.ui.panels

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
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FilterAdjustments
import com.example.model.PhotographicStyle
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.theme.CameraAmberAccent
import com.example.ui.theme.CameraGlassBorder
import com.example.ui.theme.CameraGoldAccent
import com.example.ui.theme.CameraTextPrimary
import com.example.ui.theme.CameraTextSecondary

@Composable
fun PhotographicStylesPanel(
  selectedStyle: PhotographicStyle,
  adjustments: FilterAdjustments,
  onStyleSelected: (PhotographicStyle) -> Unit,
  onAdjustmentsChange: (FilterAdjustments) -> Unit,
  onResetAdjustments: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = selectedStyle.displayName.uppercase(),
          color = CameraGoldAccent,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp
        )
        Text(
          text = selectedStyle.description,
          color = CameraTextSecondary,
          fontSize = 12.sp
        )
      }

      IconButton(
        onClick = onResetAdjustments,
        modifier = Modifier.testTag("reset_style_adjustments_button")
      ) {
        Icon(
          imageVector = Icons.Default.RestartAlt,
          contentDescription = "Reset filter adjustments",
          tint = Color.White.copy(alpha = 0.7f),
          modifier = Modifier.size(20.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Horizontal Style Cards
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(PhotographicStyle.entries, key = { it.name }) { style ->
        val isSelected = style == selectedStyle

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .clickable { onStyleSelected(style) }
            .testTag("style_item_${style.name.lowercase()}")
        ) {
          Box(
            modifier = Modifier
              .size(width = 68.dp, height = 72.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(
                Brush.linearGradient(
                  colors = when (style) {
                    PhotographicStyle.ORIGINAL -> listOf(Color(0xFF3A3D46), Color(0xFF1E2026))
                    PhotographicStyle.VIVID -> listOf(Color(0xFFFF5E3A), Color(0xFFFF2A68))
                    PhotographicStyle.DRAMATIC -> listOf(Color(0xFF1F1C2C), Color(0xFF928DAB))
                    PhotographicStyle.WARM -> listOf(Color(0xFFFFB300), Color(0xFFF77737))
                    PhotographicStyle.COOL -> listOf(Color(0xFF00C0FF), Color(0xFF4286F4))
                    PhotographicStyle.MONO -> listOf(Color(0xFF888888), Color(0xFF333333))
                    PhotographicStyle.SILVERTONE -> listOf(Color(0xFFE0E0E0), Color(0xFF555555))
                    PhotographicStyle.NOIR -> listOf(Color(0xFF222222), Color(0xFF000000))
                    PhotographicStyle.RICH_CONTRAST -> listOf(Color(0xFFE94057), Color(0xFF8A2387))
                    PhotographicStyle.WARM_CONTRAST -> listOf(Color(0xFFF27121), Color(0xFFE94057))
                    PhotographicStyle.COOL_CONTRAST -> listOf(Color(0xFF00B4DB), Color(0xFF0083B0))
                  }
                )
              )
              .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) CameraGoldAccent else CameraGlassBorder,
                shape = RoundedCornerShape(16.dp)
              ),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = style.displayName.take(3).uppercase(),
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = style.displayName,
            color = if (isSelected) CameraGoldAccent else CameraTextSecondary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Fine-Tuning Sliders
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(Color.Black.copy(alpha = 0.35f))
        .padding(12.dp)
    ) {
      // 1. Intensity
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Intensity",
          color = CameraTextPrimary,
          fontSize = 12.sp,
          modifier = Modifier.width(76.dp)
        )
        Slider(
          value = adjustments.intensity,
          onValueChange = { onAdjustmentsChange(adjustments.copy(intensity = it)) },
          valueRange = 0f..1f,
          colors = SliderDefaults.colors(
            thumbColor = CameraGoldAccent,
            activeTrackColor = CameraGoldAccent,
            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
          ),
          modifier = Modifier.weight(1f)
        )
        Text(
          text = "${(adjustments.intensity * 100).toInt()}%",
          color = CameraTextSecondary,
          fontSize = 11.sp,
          modifier = Modifier.width(36.dp)
        )
      }

      // 2. Warmth / Tone
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Tone",
          color = CameraTextPrimary,
          fontSize = 12.sp,
          modifier = Modifier.width(76.dp)
        )
        Slider(
          value = adjustments.warmth,
          onValueChange = { onAdjustmentsChange(adjustments.copy(warmth = it)) },
          valueRange = -0.5f..0.5f,
          colors = SliderDefaults.colors(
            thumbColor = CameraAmberAccent,
            activeTrackColor = CameraAmberAccent,
            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
          ),
          modifier = Modifier.weight(1f)
        )
        Text(
          text = "%+d".format((adjustments.warmth * 100).toInt()),
          color = CameraTextSecondary,
          fontSize = 11.sp,
          modifier = Modifier.width(36.dp)
        )
      }

      // 3. Contrast
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Contrast",
          color = CameraTextPrimary,
          fontSize = 12.sp,
          modifier = Modifier.width(76.dp)
        )
        Slider(
          value = adjustments.contrast,
          onValueChange = { onAdjustmentsChange(adjustments.copy(contrast = it)) },
          valueRange = 0.5f..1.8f,
          colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Color.White,
            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
          ),
          modifier = Modifier.weight(1f)
        )
        Text(
          text = "%.1fx".format(adjustments.contrast),
          color = CameraTextSecondary,
          fontSize = 11.sp,
          modifier = Modifier.width(36.dp)
        )
      }
    }
  }
}
