package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraMode
import com.example.ui.theme.CameraGoldAccent
import com.example.ui.theme.CameraTextSecondary

@Composable
fun CameraModeSelector(
  currentMode: CameraMode,
  onModeSelected: (CameraMode) -> Unit,
  modifier: Modifier = Modifier
) {
  val modes = CameraMode.entries
  val listState = rememberLazyListState()

  // Scroll to active mode on change
  LaunchedEffect(currentMode) {
    val index = modes.indexOf(currentMode)
    if (index >= 0) {
      listState.animateScrollToItem(index)
    }
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 10.dp),
    contentAlignment = Alignment.Center
  ) {
    LazyRow(
      state = listState,
      horizontalArrangement = Arrangement.spacedBy(18.dp),
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 40.dp)
    ) {
      items(modes, key = { it.name }) { mode ->
        val isSelected = mode == currentMode
        val textColor by animateColorAsState(
          targetValue = if (isSelected) CameraGoldAccent else CameraTextSecondary,
          animationSpec = spring(),
          label = "mode_text_color"
        )

        Box(
          modifier = Modifier
            .clickable { onModeSelected(mode) }
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .testTag("camera_mode_${mode.name.lowercase()}"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = mode.title,
            color = textColor,
            fontSize = if (isSelected) 13.sp else 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 0.5.sp
          )
        }
      }
    }
  }
}
