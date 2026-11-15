package com.example.ui.gallery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CapturedMedia
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.theme.CameraDeepBlack
import com.example.ui.theme.CameraGlassBorder
import com.example.ui.theme.CameraGoldAccent
import com.example.ui.theme.CameraRedRecording
import com.example.ui.theme.CameraTextPrimary
import com.example.ui.theme.CameraTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GalleryViewerScreen(
  mediaList: List<CapturedMedia>,
  initialSelectedMedia: CapturedMedia?,
  onBackToCamera: () -> Unit,
  onShareMedia: (CapturedMedia) -> Unit,
  onDeleteMedia: (CapturedMedia) -> Unit,
  modifier: Modifier = Modifier
) {
  var currentMedia by remember(initialSelectedMedia, mediaList) {
    mutableStateOf(initialSelectedMedia ?: mediaList.firstOrNull())
  }
  var showInfoSheet by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CameraDeepBlack)
      .testTag("gallery_viewer_screen")
  ) {
    if (currentMedia == null) {
      // Empty gallery state
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = "No Photos or Videos Yet",
          color = CameraTextPrimary,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Capture your first shot using the shutter button",
          color = CameraTextSecondary,
          fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
          onClick = onBackToCamera,
          colors = ButtonDefaults.buttonColors(containerColor = CameraGoldAccent)
        ) {
          Text("Return to Camera", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      }
    } else {
      val media = currentMedia!!

      // 1. Fullscreen Media Preview
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = 64.dp, bottom = 100.dp),
        contentAlignment = Alignment.Center
      ) {
        AsyncImage(
          model = media.uri,
          contentDescription = media.displayName,
          contentScale = ContentScale.Fit,
          modifier = Modifier.fillMaxSize()
        )

        // If Video: Play Icon Overlay
        if (media.isVideo) {
          Box(
            modifier = Modifier
              .size(68.dp)
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.55f))
              .border(1.5.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Video Playback",
              tint = Color.White,
              modifier = Modifier.size(42.dp)
            )
          }

          // Duration badge
          val sec = media.durationMs / 1000
          val durationText = "%02d:%02d".format(sec / 60, sec % 60)
          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(16.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color.Black.copy(alpha = 0.7f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = durationText,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // 2. Top Bar (Back, Date, Info, Share, Delete)
      Row(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .fillMaxWidth()
          .height(64.dp)
          .background(Color.Black.copy(alpha = 0.6f))
          .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        IconButton(
          onClick = onBackToCamera,
          modifier = Modifier.testTag("gallery_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to Camera",
            tint = Color.White
          )
        }

        // Formatted Date
        val dateString = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
          .format(Date(media.dateTaken))
        Text(
          text = dateString,
          color = CameraTextPrimary,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )

        Row {
          // Info toggle
          IconButton(
            onClick = { showInfoSheet = !showInfoSheet },
            modifier = Modifier.testTag("gallery_info_button")
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Media Info",
              tint = if (showInfoSheet) CameraGoldAccent else Color.White
            )
          }

          // Share
          IconButton(
            onClick = { onShareMedia(media) },
            modifier = Modifier.testTag("gallery_share_button")
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share Media",
              tint = Color.White
            )
          }

          // Delete
          IconButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.testTag("gallery_delete_button")
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete Media",
              tint = CameraRedRecording
            )
          }
        }
      }

      // 3. Bottom Filmstrip / Thumbnails
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .background(Color.Black.copy(alpha = 0.7f))
          .padding(vertical = 12.dp, horizontal = 16.dp)
      ) {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          items(mediaList, key = { it.uri.toString() }) { item ->
            val isCurrent = item.uri == media.uri
            Box(
              modifier = Modifier
                .size(width = 54.dp, height = 54.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                  width = if (isCurrent) 2.5.dp else 1.dp,
                  color = if (isCurrent) CameraGoldAccent else CameraGlassBorder,
                  shape = RoundedCornerShape(10.dp)
                )
                .clickable { currentMedia = item }
            ) {
              AsyncImage(
                model = item.uri,
                contentDescription = item.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
              if (item.isVideo) {
                Box(
                  modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(3.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                  )
                }
              }
            }
          }
        }
      }

      // 4. Media Metadata Info Card (Overlay)
      AnimatedVisibility(
        visible = showInfoSheet,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 90.dp, start = 16.dp, end = 16.dp)
      ) {
        LiquidGlassSurface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "METADATA",
              color = CameraGoldAccent,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("File Name", media.displayName)
            if (media.width > 0 && media.height > 0) {
              val mp = (media.width.toLong() * media.height.toLong()) / 1_000_000f
              InfoRow("Resolution", "${media.width} × ${media.height} (%.1f MP)".format(mp))
            }
            if (media.sizeBytes > 0) {
              val sizeMb = media.sizeBytes / (1024f * 1024f)
              InfoRow("File Size", "%.2f MB".format(sizeMb))
            }
            if (media.isVideo && media.durationMs > 0) {
              val sec = media.durationMs / 1000
              InfoRow("Duration", "%02d:%02d".format(sec / 60, sec % 60))
            }
            InfoRow("Format", if (media.isVideo) "MPEG-4 Video" else "JPEG Image")
            InfoRow("Location", "On-Device Storage")
          }
        }
      }
    }

    // 5. Delete Confirmation Dialog
    if (showDeleteDialog && currentMedia != null) {
      val mediaToDelete = currentMedia!!
      AlertDialog(
        onDismissRequest = { showDeleteDialog = false },
        title = {
          Text(text = "Delete Media?", color = CameraTextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
          Text(
            text = "Are you sure you want to permanently delete \"${mediaToDelete.displayName}\" from your device storage?",
            color = CameraTextSecondary
          )
        },
        confirmButton = {
          Button(
            onClick = {
              showDeleteDialog = false
              onDeleteMedia(mediaToDelete)
            },
            colors = ButtonDefaults.buttonColors(containerColor = CameraRedRecording),
            modifier = Modifier.testTag("confirm_delete_media_button")
          ) {
            Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showDeleteDialog = false }) {
            Text("Cancel", color = Color.White)
          }
        },
        containerColor = Color(0xFF1C1F2B)
      )
    }
  }
}

@Composable
private fun InfoRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, color = CameraTextSecondary, fontSize = 12.sp)
    Text(text = value, color = CameraTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
  }
}
