package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CameraScreen
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.theme.CameraDeepBlack
import com.example.ui.theme.CameraGlassBorder
import com.example.ui.theme.CameraGoldAccent
import com.example.ui.theme.CameraTextPrimary
import com.example.ui.theme.CameraTextSecondary
import com.example.ui.theme.CameraTheme
import com.example.viewmodel.CameraViewModel

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      CameraTheme {
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          containerColor = CameraDeepBlack
        ) { innerPadding ->
          CameraAppRoot(modifier = Modifier.padding(innerPadding))
        }
      }
    }
  }
}

@Composable
fun CameraAppRoot(modifier: Modifier = Modifier) {
  val context = LocalContext.current

  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
      ) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
  }

  if (hasCameraPermission) {
    val cameraViewModel: CameraViewModel = viewModel()
    CameraScreen(
      viewModel = cameraViewModel,
      modifier = modifier
    )
  } else {
    // iOS 27 Liquid Glass Permission Request Prompt
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(CameraDeepBlack)
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      LiquidGlassSurface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("permission_request_card"),
        shape = RoundedCornerShape(32.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(28.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(80.dp)
              .clip(CircleShape)
              .background(CameraGoldAccent.copy(alpha = 0.2f))
              .border(1.5.dp, CameraGoldAccent, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PhotoCamera,
              contentDescription = "Camera Permission",
              tint = CameraGoldAccent,
              modifier = Modifier.size(42.dp)
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          Text(
            text = "Camera & Audio Access",
            color = CameraTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "To take photographs, record high-definition videos with sound, and use real-time photographic styles, allow access to your device camera and microphone.",
            color = CameraTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
          )

          Spacer(modifier = Modifier.height(26.dp))

          Button(
            onClick = {
              permissionLauncher.launch(
                arrayOf(
                  Manifest.permission.CAMERA,
                  Manifest.permission.RECORD_AUDIO
                )
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = CameraGoldAccent),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("request_camera_permission_button")
          ) {
            Text(
              text = "Continue",
              color = Color.Black,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
