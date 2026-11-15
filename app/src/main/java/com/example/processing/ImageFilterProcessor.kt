package com.example.processing

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.media.ExifInterface
import com.example.model.AspectRatioSetting
import com.example.model.CapturedMedia
import com.example.model.FilterAdjustments
import com.example.model.PhotographicStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

class ImageFilterProcessor(private val context: Context) {

  suspend fun processAndSavePhoto(
    jpegBytes: ByteArray,
    style: PhotographicStyle,
    adjustments: FilterAdjustments,
    aspectRatioSetting: AspectRatioSetting
  ): CapturedMedia = withContext(Dispatchers.IO) {
    // 1. Read EXIF orientation
    val exifInterface = ExifInterface(ByteArrayInputStream(jpegBytes))
    val orientation = exifInterface.getAttributeInt(
      ExifInterface.TAG_ORIENTATION,
      ExifInterface.ORIENTATION_NORMAL
    )

    // 2. Decode raw bitmap
    val rawBitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
      ?: throw IllegalStateException("Failed to decode camera JPEG bytes")

    // 3. Rotate bitmap according to EXIF
    val rotationMatrix = Matrix()
    when (orientation) {
      ExifInterface.ORIENTATION_ROTATE_90 -> rotationMatrix.postRotate(90f)
      ExifInterface.ORIENTATION_ROTATE_180 -> rotationMatrix.postRotate(180f)
      ExifInterface.ORIENTATION_ROTATE_270 -> rotationMatrix.postRotate(270f)
      ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> rotationMatrix.postScale(-1f, 1f)
      ExifInterface.ORIENTATION_FLIP_VERTICAL -> rotationMatrix.postScale(1f, -1f)
    }

    val orientedBitmap = if (!rotationMatrix.isIdentity) {
      Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, rotationMatrix, true).also {
        if (it != rawBitmap) rawBitmap.recycle()
      }
    } else {
      rawBitmap
    }

    // 4. Crop to Aspect Ratio (4:3, 16:9, or 1:1)
    val croppedBitmap = cropToAspectRatio(orientedBitmap, aspectRatioSetting)
    if (croppedBitmap != orientedBitmap) {
      orientedBitmap.recycle()
    }

    // 5. Apply Photographic Style & ColorMatrix filter
    val colorMatrix = FilterColorMatrices.createAndroidColorMatrix(style, adjustments)
    val processedBitmap = Bitmap.createBitmap(
      croppedBitmap.width,
      croppedBitmap.height,
      Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(processedBitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
      colorFilter = ColorMatrixColorFilter(colorMatrix)
    }
    canvas.drawBitmap(croppedBitmap, 0f, 0f, paint)
    croppedBitmap.recycle()

    // 6. Save directly to MediaStore
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val displayName = "IMG_$timeStamp.jpg"
    val outputStream = ByteArrayOutputStream()
    processedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
    val finalBytes = outputStream.toByteArray()
    val width = processedBitmap.width
    val height = processedBitmap.height
    processedBitmap.recycle()

    val contentValues = ContentValues().apply {
      put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
      put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
      put(MediaStore.Images.Media.WIDTH, width)
      put(MediaStore.Images.Media.HEIGHT, height)
      put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
      put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Camera")
        put(MediaStore.Images.Media.IS_PENDING, 1)
      }
    }

    val contentResolver = context.contentResolver
    val uri: Uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
      ?: throw IllegalStateException("Failed to create MediaStore entry for photo")

    contentResolver.openOutputStream(uri)?.use { os ->
      os.write(finalBytes)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      contentValues.clear()
      contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
      contentResolver.update(uri, contentValues, null, null)
    }

    CapturedMedia(
      uri = uri,
      path = uri.toString(),
      displayName = displayName,
      dateTaken = System.currentTimeMillis(),
      isVideo = false,
      width = width,
      height = height,
      sizeBytes = finalBytes.size.toLong()
    )
  }

  private fun cropToAspectRatio(bitmap: Bitmap, setting: AspectRatioSetting): Bitmap {
    val srcWidth = bitmap.width
    val srcHeight = bitmap.height

    val targetRatio = when (setting) {
      AspectRatioSetting.RATIO_4_3 -> if (srcHeight > srcWidth) 3f / 4f else 4f / 3f
      AspectRatioSetting.RATIO_16_9 -> if (srcHeight > srcWidth) 9f / 16f else 16f / 9f
      AspectRatioSetting.RATIO_1_1 -> 1f
    }

    val currentRatio = srcWidth.toFloat() / srcHeight.toFloat()

    val (cropWidth, cropHeight) = if (currentRatio > targetRatio) {
      // Too wide, crop width
      val newWidth = (srcHeight * targetRatio).toInt()
      Pair(newWidth, srcHeight)
    } else {
      // Too tall, crop height
      val newHeight = (srcWidth / targetRatio).toInt()
      Pair(srcWidth, newHeight)
    }

    val startX = (srcWidth - cropWidth) / 2
    val startY = (srcHeight - cropHeight) / 2

    return if (cropWidth < srcWidth || cropHeight < srcHeight) {
      Bitmap.createBitmap(bitmap, startX.coerceAtLeast(0), startY.coerceAtLeast(0), cropWidth, cropHeight)
    } else {
      bitmap
    }
  }
}
