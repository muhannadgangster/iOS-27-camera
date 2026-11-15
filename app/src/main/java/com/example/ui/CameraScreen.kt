package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.camera.CameraXManager
import com.example.model.CameraMode
import com.example.processing.ImageFilterProcessor
import com.example.ui.components.CameraBottomControls
import com.example.ui.components.CameraModeSelector
import com.example.ui.components.CameraTopBar
import com.example.ui.components.CameraZoomControl
import com.example.ui.components.ViewfinderOverlay
import com.example.ui.gallery.GalleryViewerScreen
import com.example.ui.panels.CameraSettingsSheet
import com.example.ui.panels.CustomizeControlsSheet
import com.example.ui.panels.ExpandedControlsSheet
import com.example.ui.theme.CameraDeepBlack
import com.example.viewmodel.CameraViewModel

@Composable
fun CameraScreen(
  viewModel: CameraViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val uiState by viewModel.uiState.collectAsState()

  val hasAudioPermission = remember(context) {
    ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
  }

  // Handle Back Navigation cleanly: Sub-screens first, then camera
  BackHandler(
    enabled = uiState.isGalleryOpen || uiState.isSettingsOpen || uiState.isCustomizeControlsOpen || uiState.isControlsPanelOpen
  ) {
    when {
      uiState.isGalleryOpen -> viewModel.closeGallery()
      uiState.isSettingsOpen -> viewModel.closeSettings()
      uiState.isCustomizeControlsOpen -> viewModel.closeCustomizeControls()
      uiState.isControlsPanelOpen -> viewModel.closeControlsPanel()
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CameraDeepBlack)
      .testTag("camera_screen_root")
  ) {
    // 1. Live Camera Viewfinder & Touch Area
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val viewWidthPx = constraints.maxWidth.toFloat()
      val viewHeightPx = constraints.maxHeight.toFloat()

      // PreviewView embedded via AndroidView
      AndroidView(
        factory = { ctx ->
          PreviewView(ctx).apply {
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            scaleType = PreviewView.ScaleType.FILL_CENTER
            val manager = CameraXManager(
              context = ctx,
              lifecycleOwner = lifecycleOwner,
              previewView = this,
              imageProcessor = ImageFilterProcessor(ctx)
            ).also { mgr ->
              mgr.onZoomRangeChanged = { minZ, maxZ ->
                viewModel.detectHardwareCapabilities()
              }
              mgr.onRecordingDurationUpdate = { sec ->
                // Handled in viewmodel
              }
              mgr.onRecordingFinished = {
                viewModel.loadRecentMedia()
              }
            }
            viewModel.cameraXManager = manager
            manager.configure(
              mode = uiState.currentMode,
              isFront = uiState.isFrontCamera,
              flash = uiState.flashMode,
              videoRes = uiState.videoResolution
            )
          }
        },
        modifier = Modifier.fillMaxSize()
      )

      // Touch detector over Viewfinder for Tap-to-Focus, AE/AF Lock, and Mode Swipe
      Box(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(Unit) {
            detectTapGestures(
              onTap = { offset ->
                val normX = (offset.x / viewWidthPx).coerceIn(0f, 1f)
                val normY = (offset.y / viewHeightPx).coerceIn(0f, 1f)
                viewModel.onFocusTap(normX, normY, viewWidthPx, viewHeightPx)
              },
              onLongPress = {
                viewModel.toggleAeAfLock()
              }
            )
          }
      )

      // Real-time Viewfinder Overlays (Grid, Level, Reticle, Countdown, Aspect Mask)
      ViewfinderOverlay(
        gridMode = uiState.gridMode,
        isLevelEnabled = uiState.isLevelEnabled,
        levelRollDegrees = uiState.levelRollDegrees,
        isLevelAligned = uiState.isLevelAligned,
        isAeAfLocked = uiState.isAeAfLocked,
        focusPoint = uiState.focusPoint,
        countdownRemaining = uiState.countdownRemaining,
        aspectRatioSetting = uiState.aspectRatio,
        selectedStyle = uiState.selectedFilter,
        onExposureDrag = { delta -> viewModel.adjustExposureByDrag(delta) },
        onUnlockAeAf = { viewModel.toggleAeAfLock() }
      )
    }

    // 2. Camera Controls Foreground Layout
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top HUD / Bar
      CameraTopBar(
        currentMode = uiState.currentMode,
        flashMode = uiState.flashMode,
        selectedStyle = uiState.selectedFilter,
        exposureEvText = uiState.formattedExposureEv,
        isRecording = uiState.isRecordingVideo,
        recordingTimeText = uiState.formattedRecordingTime,
        isControlsPanelOpen = uiState.isControlsPanelOpen,
        videoResolution = uiState.videoResolution,
        videoFps = uiState.videoFps,
        onFlashToggle = { viewModel.toggleFlash() },
        onStylesClick = {
          viewModel.toggleControlsPanel()
        },
        onToggleControlsPanel = { viewModel.toggleControlsPanel() },
        onResolutionToggle = { viewModel.cycleVideoResolution() },
        onFpsToggle = { viewModel.cycleVideoFps() }
      )

      // Expanded Liquid Glass Controls Sheet
      AnimatedVisibility(
        visible = uiState.isControlsPanelOpen,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
      ) {
        ExpandedControlsSheet(
          customControls = uiState.customControlsList,
          flashMode = uiState.flashMode,
          timerSetting = uiState.timerSetting,
          aspectRatio = uiState.aspectRatio,
          gridMode = uiState.gridMode,
          isLevelEnabled = uiState.isLevelEnabled,
          exposureIndex = uiState.exposureIndex,
          minExposure = uiState.minExposureIndex,
          maxExposure = uiState.maxExposureIndex,
          exposureStep = uiState.exposureStep,
          selectedStyle = uiState.selectedFilter,
          filterAdjustments = uiState.filterAdjustments,
          videoResolution = uiState.videoResolution,
          videoFps = uiState.videoFps,
          supportedResolutions = uiState.supportedVideoResolutions,
          supportedFps = uiState.supportedVideoFps,
          isStabilizationEnabled = uiState.isStabilizationEnabled,
          isLiquidGlassEnabled = uiState.isLiquidGlassEnabled,
          isGlassReflectionsEnabled = uiState.isGlassReflectionsEnabled,
          onFlashChange = { viewModel.setFlash(it) },
          onTimerChange = { viewModel.setTimer(it) },
          onAspectRatioChange = { viewModel.setAspectRatio(it) },
          onGridModeChange = { viewModel.setGridMode(it) },
          onLevelToggle = { viewModel.toggleLevel(it) },
          onExposureChange = { viewModel.setExposureIndex(it) },
          onStyleSelected = { viewModel.selectPhotographicStyle(it) },
          onFilterAdjustmentsChange = { viewModel.updateFilterAdjustments(it) },
          onResetFilterAdjustments = { viewModel.resetFilterAdjustments() },
          onVideoResolutionChange = { viewModel.setVideoResolution(it) },
          onVideoFpsChange = { viewModel.setVideoFps(it) },
          onStabilizationToggle = { viewModel.toggleStabilization(it) },
          onOpenCustomizeControls = { viewModel.openCustomizeControls() },
          onOpenSettings = { viewModel.openSettings() },
          onClose = { viewModel.closeControlsPanel() }
        )
      }

      Spacer(modifier = Modifier.weight(1f))

      // Zoom Selector Pills
      CameraZoomControl(
        zoomRatio = uiState.zoomRatio,
        minZoom = uiState.minZoomRatio,
        maxZoom = uiState.maxZoomRatio,
        onZoomChange = { viewModel.setZoom(it) }
      )

      // Mode Selector Dial
      CameraModeSelector(
        currentMode = uiState.currentMode,
        onModeSelected = { viewModel.onModeChange(it) }
      )

      // Bottom Shutter & Gallery Controls
      CameraBottomControls(
        currentMode = uiState.currentMode,
        isRecording = uiState.isRecordingVideo,
        recentMedia = uiState.recentMediaList.firstOrNull(),
        onShutterClick = { viewModel.onShutterPress(hasAudioPermission) },
        onSwitchCamera = { viewModel.switchCamera() },
        onOpenGallery = { viewModel.openGallery() }
      )
    }

    // 3. Customize Controls Dialog Overlay
    if (uiState.isCustomizeControlsOpen) {
      CustomizeControlsSheet(
        initialControls = uiState.customControlsList,
        onSaveControls = { viewModel.saveCustomControls(it) },
        onResetDefaults = { viewModel.resetCustomControlsDefaults() },
        onDismiss = { viewModel.closeCustomizeControls() }
      )
    }

    // 4. Advanced Camera Settings Dialog Overlay
    if (uiState.isSettingsOpen) {
      CameraSettingsSheet(
        videoResolution = uiState.videoResolution,
        videoFps = uiState.videoFps,
        isStabilizationEnabled = uiState.isStabilizationEnabled,
        isLevelEnabled = uiState.isLevelEnabled,
        isLiquidGlassEnabled = uiState.isLiquidGlassEnabled,
        isGlassReflectionsEnabled = uiState.isGlassReflectionsEnabled,
        isBgRefractionEnabled = uiState.isBackgroundRefractionEnabled,
        isMotionEffectsEnabled = uiState.isMotionEffectsEnabled,
        isPerformanceModeEnabled = uiState.isPerformanceModeEnabled,
        animationQuality = uiState.animationQuality,
        onVideoResolutionChange = { viewModel.setVideoResolution(it) },
        onVideoFpsChange = { viewModel.setVideoFps(it) },
        onStabilizationToggle = { viewModel.toggleStabilization(it) },
        onLevelToggle = { viewModel.toggleLevel(it) },
        onLiquidGlassToggle = { viewModel.setLiquidGlassEnabled(it) },
        onGlassReflectionsToggle = { viewModel.setGlassReflectionsEnabled(it) },
        onBgRefractionToggle = { viewModel.setBgRefractionEnabled(it) },
        onMotionEffectsToggle = { viewModel.setMotionEffectsEnabled(it) },
        onPerformanceModeToggle = { viewModel.setPerformanceModeEnabled(it) },
        onAnimationQualityChange = { viewModel.setAnimationQuality(it) },
        onResetAllSettings = { viewModel.resetAllSettings() },
        onDismiss = { viewModel.closeSettings() }
      )
    }

    // 5. In-App Fullscreen Media Gallery Screen
    if (uiState.isGalleryOpen) {
      GalleryViewerScreen(
        mediaList = uiState.recentMediaList,
        initialSelectedMedia = uiState.selectedMediaItem,
        onBackToCamera = { viewModel.closeGallery() },
        onShareMedia = { media ->
          val shareIntent = viewModel.createShareIntent(media)
          context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Media"))
        },
        onDeleteMedia = { media ->
          viewModel.deleteMedia(media)
        }
      )
    }
  }
}
