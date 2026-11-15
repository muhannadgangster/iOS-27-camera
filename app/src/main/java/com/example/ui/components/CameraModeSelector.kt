package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.CameraGlassBorder
import com.example.ui.theme.CameraGoldAccent
import com.example.ui.theme.CameraTextPrimary
import com.example.ui.theme.CameraTextSecondary

/**
 * iOS 26 Mode Selector:
 * Positioned below the shutter button.
 * Defaults to the streamlined Two-Tab Primary layout: PHOTO and VIDEO,
 * while allowing horizontal swiping/expanding to access specialized modes
 * (Portrait, Night, Pano, Macro, Slo-Mo, Time-Lapse).
 */
@Composable
fun CameraModeSelector(
  currentMode: CameraMode,
  onModeSelected: (CameraMode) -> Unit,
  modifier: Modifier = Modifier
) {
  var showAllModes by remember { mutableStateOf(false) }

  val isVideoTab = currentMode.isVideoMode

  // Specialized sub-modes for active tab
  val photoFamily = listOf(
    CameraMode.PHOTO,
    CameraMode.PORTRAIT,
    CameraMode.NIGHT,
    CameraMode.PANO,
    CameraMode.MACRO
  )
  val videoFamily = listOf(
    CameraMode.VIDEO,
    CameraMode.SLO_MO,
    CameraMode.TIME_LAPSE
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(bottom = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // 1. Primary Two-Tab Liquid Glass Pill (PHOTO / VIDEO)
    Box(
      modifier = Modifier
        .shadow(elevation = 6.dp, shape = CircleShape, clip = false)
        .clip(CircleShape)
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0x4D2A3042),
              Color(0x33121622)
            )
          )
        )
        .border(
          width = 1.dp,
          brush = Brush.linearGradient(
            colors = listOf(
              Color(0x80FFFFFF),
              CameraGlassBorder,
              Color(0x26FFFFFF)
            )
          ),
          shape = CircleShape
        )
        .padding(horizontal = 4.dp, vertical = 3.dp)
        .testTag("primary_two_tab_mode_bar")
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // Photo Tab
        val isPhotoActive = !isVideoTab
        Box(
          modifier = Modifier
            .clip(CircleShape)
            .then(
              if (isPhotoActive) {
                Modifier
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(
                        Color(0x4DFFFFFF),
                        Color(0x1AFFFFFF)
                      )
                    )
                  )
                  .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(listOf(Color(0x99FFFFFF), Color(0x33FFFFFF))),
                    shape = CircleShape
                  )
              } else {
                Modifier
              }
            )
            .clickable {
              if (isVideoTab) {
                onModeSelected(CameraMode.PHOTO)
              } else {
                showAllModes = !showAllModes
              }
            }
            .padding(horizontal = 18.dp, vertical = 6.dp)
            .testTag("mode_tab_photo"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (!isVideoTab && currentMode != CameraMode.PHOTO) currentMode.title else "PHOTO",
            color = if (isPhotoActive) CameraGoldAccent else CameraTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
          )
        }

        // Video Tab
        Box(
          modifier = Modifier
            .clip(CircleShape)
            .then(
              if (isVideoTab) {
                Modifier
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(
                        Color(0x4DFFFFFF),
                        Color(0x1AFFFFFF)
                      )
                    )
                  )
                  .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(listOf(Color(0x99FFFFFF), Color(0x33FFFFFF))),
                    shape = CircleShape
                  )
              } else {
                Modifier
              }
            )
            .clickable {
              if (!isVideoTab) {
                onModeSelected(CameraMode.VIDEO)
              } else {
                showAllModes = !showAllModes
              }
            }
            .padding(horizontal = 18.dp, vertical = 6.dp)
            .testTag("mode_tab_video"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (isVideoTab && currentMode != CameraMode.VIDEO) currentMode.title else "VIDEO",
            color = if (isVideoTab) CameraGoldAccent else CameraTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
          )
        }
      }
    }

    // 2. Expandable Sub-Modes Carousel (Portrait, Night, Pano, Slo-Mo, etc.)
    AnimatedVisibility(
      visible = showAllModes,
      enter = expandVertically() + fadeIn(),
      exit = shrinkVertically() + fadeOut()
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(6.dp))
        val currentSubFamily = if (isVideoTab) videoFamily else photoFamily

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.6f))
            .border(1.dp, CameraGlassBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
          items(currentSubFamily, key = { it.name }) { mode ->
            val isSelected = mode == currentMode
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) CameraGoldAccent.copy(alpha = 0.25f) else Color.Transparent)
                .clickable {
                  onModeSelected(mode)
                  showAllModes = false
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("sub_mode_${mode.name.lowercase()}"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = mode.title,
                color = if (isSelected) CameraGoldAccent else CameraTextPrimary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
              )
            }
          }
        }
      }
    }
  }
}
