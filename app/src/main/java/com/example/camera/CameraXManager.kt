package com.example.camera

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.DynamicRange
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.extensions.ExtensionMode
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.model.AspectRatioSetting
import com.example.model.CameraMode
import com.example.model.CapturedMedia
import com.example.model.FilterAdjustments
import com.example.model.FlashMode
import com.example.model.PhotographicStyle
import com.example.model.VideoResolution
import com.example.processing.ImageFilterProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraXManager(
  private val context: Context,
  private val lifecycleOwner: LifecycleOwner,
  private val previewView: PreviewView,
  private val imageProcessor: ImageFilterProcessor
) {

  private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
  private var cameraProvider: ProcessCameraProvider? = null
  private var extensionsManager: ExtensionsManager? = null
  private var currentCamera: Camera? = null

  private var preview: Preview? = null
  private var imageCapture: ImageCapture? = null
  private var videoCapture: VideoCapture<Recorder>? = null
  private var activeRecording: Recording? = null

  // Current configurations
  var isFrontCamera: Boolean = false
    private set
  var currentMode: CameraMode = CameraMode.PHOTO
    private set
  var currentFlashMode: FlashMode = FlashMode.AUTO
    private set
  var isTorchEnabled: Boolean = false
    private set
  var currentResolution: VideoResolution = VideoResolution.RES_1080P
    private set

  // Callbacks
  var onZoomRangeChanged: ((min: Float, max: Float) -> Unit)? = null
  var onExposureRangeChanged: ((min: Int, max: Int, step: Float) -> Unit)? = null
  var onRecordingDurationUpdate: ((seconds: Long) -> Unit)? = null
  var onRecordingFinished: ((CapturedMedia?) -> Unit)? = null

  init {
    initCameraProvider()
  }

  private fun initCameraProvider() {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
      try {
        cameraProvider = cameraProviderFuture.get()
        ExtensionsManager.getInstanceAsync(context, cameraProvider!!).addListener({
          try {
            extensionsManager = ExtensionsManager.getInstanceAsync(context, cameraProvider!!).get()
          } catch (e: Exception) {
            Log.w("CameraXManager", "ExtensionsManager not available", e)
          }
          bindCameraUseCases()
        }, ContextCompat.getMainExecutor(context))
      } catch (e: Exception) {
        Log.e("CameraXManager", "Failed to get CameraProvider", e)
      }
    }, ContextCompat.getMainExecutor(context))
  }

  fun configure(
    mode: CameraMode,
    isFront: Boolean,
    flash: FlashMode,
    videoRes: VideoResolution
  ) {
    val needsRebind = (currentMode.isVideoMode != mode.isVideoMode) ||
      (currentMode == CameraMode.NIGHT || mode == CameraMode.NIGHT) ||
      (isFrontCamera != isFront) ||
      (currentResolution != videoRes && mode.isVideoMode)

    currentMode = mode
    isFrontCamera = isFront
    currentFlashMode = flash
    currentResolution = videoRes

    if (needsRebind) {
      bindCameraUseCases()
    } else {
      updateFlashAndTorch()
    }
  }

  private fun bindCameraUseCases() {
    val provider = cameraProvider ?: return
    provider.unbindAll()

    val baseSelector = if (isFrontCamera) {
      CameraSelector.DEFAULT_FRONT_CAMERA
    } else {
      CameraSelector.DEFAULT_BACK_CAMERA
    }

    // Check if Night mode extension is supported and requested
    val selector = if (currentMode == CameraMode.NIGHT && extensionsManager != null) {
      val ext = extensionsManager!!
      if (ext.isExtensionAvailable(baseSelector, ExtensionMode.NIGHT)) {
        ext.getExtensionEnabledCameraSelector(baseSelector, ExtensionMode.NIGHT)
      } else {
        baseSelector
      }
    } else {
      baseSelector
    }

    // 1. Preview
    preview = Preview.Builder().build().also {
      it.surfaceProvider = previewView.surfaceProvider
    }

    // 2. ImageCapture
    val flashModeInt = when (currentFlashMode) {
      FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
      FlashMode.ON -> ImageCapture.FLASH_MODE_ON
      FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
    }

    imageCapture = ImageCapture.Builder()
      .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
      .setFlashMode(flashModeInt)
      .build()

    // 3. VideoCapture - only initialized if currently in a video mode
    if (currentMode.isVideoMode) {
      val recorder = try {
        val cameraInfo = provider.getCameraInfo(selector)
        val videoCaps = Recorder.getVideoCapabilities(cameraInfo)
        val supported = videoCaps.getSupportedQualities(DynamicRange.SDR)
        if (supported.isNotEmpty()) {
          val targetQuality = when (currentResolution) {
            VideoResolution.RES_4K -> Quality.UHD
            VideoResolution.RES_1080P -> Quality.FHD
            VideoResolution.RES_720P -> Quality.HD
          }
          val ordered = if (supported.contains(targetQuality)) {
            listOf(targetQuality) + supported.filter { it != targetQuality }
          } else {
            supported
          }
          Recorder.Builder()
            .setQualitySelector(QualitySelector.fromOrderedList(ordered))
            .setExecutor(cameraExecutor)
            .build()
        } else {
          Recorder.Builder().setExecutor(cameraExecutor).build()
        }
      } catch (e: Exception) {
        Log.w("CameraXManager", "Video capabilities check fallback", e)
        Recorder.Builder().setExecutor(cameraExecutor).build()
      }

      videoCapture = VideoCapture.withOutput(recorder)
    } else {
      videoCapture = null
    }

    try {
      currentCamera = if (currentMode.isVideoMode && videoCapture != null) {
        provider.bindToLifecycle(lifecycleOwner, selector, preview, videoCapture)
      } else {
        provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
      }

      observeCameraState(currentCamera!!)
      updateFlashAndTorch()
    } catch (e: Exception) {
      Log.e("CameraXManager", "Use case binding failed", e)
      // Fallback: bind preview and photo capture
      try {
        currentCamera = provider.bindToLifecycle(lifecycleOwner, baseSelector, preview, imageCapture)
        observeCameraState(currentCamera!!)
      } catch (ex: Exception) {
        Log.e("CameraXManager", "Fallback binding failed", ex)
      }
    }
  }

  private fun observeCameraState(camera: Camera) {
    camera.cameraInfo.zoomState.observe(lifecycleOwner) { zoomState ->
      if (zoomState != null) {
        onZoomRangeChanged?.invoke(zoomState.minZoomRatio, zoomState.maxZoomRatio)
      }
    }

    val exposureState = camera.cameraInfo.exposureState
    val range = exposureState.exposureCompensationRange
    val step = exposureState.exposureCompensationStep.toFloat()
    onExposureRangeChanged?.invoke(range.lower, range.upper, step)
  }

  fun setZoom(ratio: Float) {
    currentCamera?.cameraControl?.setZoomRatio(ratio)
  }

  fun setExposureIndex(index: Int) {
    currentCamera?.cameraControl?.setExposureCompensationIndex(index)
  }

  fun setFlash(flash: FlashMode) {
    currentFlashMode = flash
    val flashModeInt = when (flash) {
      FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
      FlashMode.ON -> ImageCapture.FLASH_MODE_ON
      FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
    }
    imageCapture?.flashMode = flashModeInt

    if (currentMode.isVideoMode) {
      setTorch(flash == FlashMode.ON)
    }
  }

  fun setTorch(enable: Boolean) {
    isTorchEnabled = enable
    currentCamera?.cameraControl?.enableTorch(enable)
  }

  private fun updateFlashAndTorch() {
    setFlash(currentFlashMode)
    if (currentMode.isVideoMode && currentFlashMode == FlashMode.ON) {
      setTorch(true)
    } else if (currentMode.isVideoMode) {
      setTorch(false)
    }
  }

  fun focusAndMeterAt(normalizedX: Float, normalizedY: Float, width: Float, height: Float) {
    val camera = currentCamera ?: return
    val factory = SurfaceOrientedMeteringPointFactory(width, height)
    val point = factory.createPoint(normalizedX * width, normalizedY * height)
    val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
      .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
      .build()
    camera.cameraControl.startFocusAndMetering(action)
  }

  fun lockAeAf(lock: Boolean) {
    val camera = currentCamera ?: return
    if (!lock) {
      camera.cameraControl.cancelFocusAndMetering()
    }
  }

  fun capturePhoto(
    style: PhotographicStyle,
    adjustments: FilterAdjustments,
    aspectRatioSetting: AspectRatioSetting,
    onSuccess: (CapturedMedia) -> Unit,
    onError: (Exception) -> Unit
  ) {
    val capture = imageCapture ?: run {
      onError(IllegalStateException("Camera capture not ready"))
      return
    }

    capture.takePicture(
      cameraExecutor,
      object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
          try {
            val buffer: ByteBuffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            image.close()

            CoroutineScope(Dispatchers.IO).launch {
              try {
                val media = imageProcessor.processAndSavePhoto(
                  jpegBytes = bytes,
                  style = style,
                  adjustments = adjustments,
                  aspectRatioSetting = aspectRatioSetting
                )
                withContext(Dispatchers.Main) {
                  onSuccess(media)
                }
              } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                  onError(e)
                }
              }
            }
          } catch (e: Exception) {
            image.close()
            ContextCompat.getMainExecutor(context).execute { onError(e) }
          }
        }

        override fun onError(exception: ImageCaptureException) {
          ContextCompat.getMainExecutor(context).execute { onError(exception) }
        }
      }
    )
  }

  fun startVideoRecording(
    hasAudioPermission: Boolean,
    onError: (String) -> Unit
  ) {
    val vc = videoCapture ?: run {
      onError("Video capture not initialized")
      return
    }

    if (activeRecording != null) {
      return
    }

    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val displayName = "VID_$timeStamp.mp4"

    val contentValues = ContentValues().apply {
      put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
      put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
      put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/Camera")
      }
    }

    val mediaStoreOutput = MediaStoreOutputOptions.Builder(
      context.contentResolver,
      MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    ).setContentValues(contentValues).build()

    var pendingRecording = vc.output.prepareRecording(context, mediaStoreOutput)
    if (hasAudioPermission) {
      try {
        pendingRecording = pendingRecording.withAudioEnabled()
      } catch (e: SecurityException) {
        Log.w("CameraXManager", "Audio permission missing for recording", e)
      }
    }

    var startTime = 0L

    activeRecording = pendingRecording.start(ContextCompat.getMainExecutor(context)) { event ->
      when (event) {
        is VideoRecordEvent.Start -> {
          startTime = System.currentTimeMillis()
        }
        is VideoRecordEvent.Status -> {
          val durationSec = (event.recordingStats.recordedDurationNanos / 1_000_000_000L)
          onRecordingDurationUpdate?.invoke(durationSec)
        }
        is VideoRecordEvent.Finalize -> {
          activeRecording = null
          if (!event.hasError()) {
            val uri = event.outputResults.outputUri
            val durationMs = if (startTime > 0) System.currentTimeMillis() - startTime else 0L
            val media = CapturedMedia(
              uri = uri,
              path = uri.toString(),
              displayName = displayName,
              dateTaken = System.currentTimeMillis(),
              isVideo = true,
              durationMs = durationMs
            )
            onRecordingFinished?.invoke(media)
          } else {
            Log.e("CameraXManager", "Video recording error code: ${event.error}")
            onRecordingFinished?.invoke(null)
          }
        }
      }
    }
  }

  fun pauseVideoRecording() {
    activeRecording?.pause()
  }

  fun resumeVideoRecording() {
    activeRecording?.resume()
  }

  fun stopVideoRecording() {
    activeRecording?.stop()
    activeRecording = null
  }

  fun release() {
    activeRecording?.stop()
    activeRecording = null
    cameraExecutor.shutdown()
  }
}
