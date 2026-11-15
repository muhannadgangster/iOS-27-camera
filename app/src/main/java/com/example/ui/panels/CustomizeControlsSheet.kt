package com.example.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.model.ControlItem
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.theme.CameraDeepBlack
import com.example.ui.theme.CameraGlassBorder
import com.example.ui.theme.CameraGoldAccent
import com.example.ui.theme.CameraTextPrimary
import com.example.ui.theme.CameraTextSecondary

@Composable
fun CustomizeControlsSheet(
  initialControls: List<ControlItem>,
  onSaveControls: (List<ControlItem>) -> Unit,
  onResetDefaults: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  var workingList by remember(initialControls) { mutableStateOf(initialControls) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CameraDeepBlack.copy(alpha = 0.85f))
      .padding(16.dp),
    contentAlignment = Alignment.Center
  ) {
    LiquidGlassSurface(
      modifier = Modifier
        .fillMaxWidth()
        .height(620.dp)
        .testTag("customize_controls_dialog"),
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
              text = "Customize Camera Controls",
              color = CameraTextPrimary,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Add, remove, reorder and pin controls",
              color = CameraTextSecondary,
              fontSize = 12.sp
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("dismiss_customize_controls")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Controls List with Reordering & Pinning
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          itemsIndexed(workingList, key = { _, item -> item.id.name }) { index, item ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, CameraGlassBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                // Checkbox to toggle Enable / Disable
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Checkbox(
                    checked = item.isEnabled,
                    onCheckedChange = { checked ->
                      val updated = workingList.toMutableList()
                      updated[index] = item.copy(isEnabled = checked)
                      workingList = updated
                    },
                    colors = CheckboxDefaults.colors(
                      checkedColor = CameraGoldAccent,
                      checkmarkColor = Color.Black
                    ),
                    modifier = Modifier.testTag("control_checkbox_${item.id.name.lowercase()}")
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = item.title,
                    color = if (item.isEnabled) CameraTextPrimary else CameraTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = if (item.isEnabled) FontWeight.SemiBold else FontWeight.Normal
                  )
                }

                // Action buttons: Pin + Move Up / Down
                Row(verticalAlignment = Alignment.CenterVertically) {
                  // Pin toggle
                  IconButton(
                    onClick = {
                      val updated = workingList.toMutableList()
                      updated[index] = item.copy(isPinned = !item.isPinned)
                      workingList = updated
                    },
                    modifier = Modifier.size(34.dp).testTag("pin_button_${item.id.name.lowercase()}")
                  ) {
                    Icon(
                      imageVector = if (item.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                      contentDescription = if (item.isPinned) "Unpin" else "Pin",
                      tint = if (item.isPinned) CameraGoldAccent else Color.White.copy(alpha = 0.5f),
                      modifier = Modifier.size(18.dp)
                    )
                  }

                  // Move Up
                  IconButton(
                    onClick = {
                      if (index > 0) {
                        val updated = workingList.toMutableList()
                        val temp = updated[index]
                        updated[index] = updated[index - 1]
                        updated[index - 1] = temp
                        workingList = updated
                      }
                    },
                    enabled = index > 0,
                    modifier = Modifier.size(34.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.ArrowUpward,
                      contentDescription = "Move Up",
                      tint = if (index > 0) Color.White else Color.White.copy(alpha = 0.2f),
                      modifier = Modifier.size(18.dp)
                    )
                  }

                  // Move Down
                  IconButton(
                    onClick = {
                      if (index < workingList.size - 1) {
                        val updated = workingList.toMutableList()
                        val temp = updated[index]
                        updated[index] = updated[index + 1]
                        updated[index + 1] = temp
                        workingList = updated
                      }
                    },
                    enabled = index < workingList.size - 1,
                    modifier = Modifier.size(34.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.ArrowDownward,
                      contentDescription = "Move Down",
                      tint = if (index < workingList.size - 1) Color.White else Color.White.copy(alpha = 0.2f),
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons: Reset & Save
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Button(
            onClick = {
              onResetDefaults()
              onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("reset_defaults_controls_button")
          ) {
            Icon(
              imageVector = Icons.Default.RestartAlt,
              contentDescription = "Reset Defaults",
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Reset", color = Color.White, fontSize = 13.sp)
          }

          Button(
            onClick = {
              onSaveControls(workingList)
              onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = CameraGoldAccent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("save_custom_controls_button")
          ) {
            Icon(
              imageVector = Icons.Default.Done,
              contentDescription = "Save",
              tint = Color.Black,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Save Layout", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
