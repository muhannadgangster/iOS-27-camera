package com.example.data

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.model.CapturedMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository(private val context: Context) {

  suspend fun getRecentCapturedMedia(limit: Int = 50): List<CapturedMedia> = withContext(Dispatchers.IO) {
    val mediaList = mutableListOf<CapturedMedia>()
    val contentResolver = context.contentResolver

    // Query Images
    val imageProjection = arrayOf(
      MediaStore.Images.Media._ID,
      MediaStore.Images.Media.DISPLAY_NAME,
      MediaStore.Images.Media.DATE_ADDED,
      MediaStore.Images.Media.WIDTH,
      MediaStore.Images.Media.HEIGHT,
      MediaStore.Images.Media.SIZE
    )

    try {
      contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        imageProjection,
        null,
        null,
        "${MediaStore.Images.Media.DATE_ADDED} DESC"
      )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
        val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
        val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
        val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

        var count = 0
        while (cursor.moveToNext() && count < limit) {
          val id = cursor.getLong(idColumn)
          val name = cursor.getString(nameColumn) ?: "IMG_$id.jpg"
          val dateAdded = cursor.getLong(dateColumn) * 1000
          val width = cursor.getInt(widthColumn)
          val height = cursor.getInt(heightColumn)
          val size = cursor.getLong(sizeColumn)
          val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

          mediaList.add(
            CapturedMedia(
              uri = uri,
              path = uri.toString(),
              displayName = name,
              dateTaken = dateAdded,
              isVideo = false,
              width = width,
              height = height,
              sizeBytes = size
            )
          )
          count++
        }
      }
    } catch (_: Exception) {}

    // Query Videos
    val videoProjection = arrayOf(
      MediaStore.Video.Media._ID,
      MediaStore.Video.Media.DISPLAY_NAME,
      MediaStore.Video.Media.DATE_ADDED,
      MediaStore.Video.Media.DURATION,
      MediaStore.Video.Media.WIDTH,
      MediaStore.Video.Media.HEIGHT,
      MediaStore.Video.Media.SIZE
    )

    try {
      contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        videoProjection,
        null,
        null,
        "${MediaStore.Video.Media.DATE_ADDED} DESC"
      )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
        val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
        val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
        val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
        val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

        var count = 0
        while (cursor.moveToNext() && count < limit) {
          val id = cursor.getLong(idColumn)
          val name = cursor.getString(nameColumn) ?: "VID_$id.mp4"
          val dateAdded = cursor.getLong(dateColumn) * 1000
          val duration = cursor.getLong(durationColumn)
          val width = cursor.getInt(widthColumn)
          val height = cursor.getInt(heightColumn)
          val size = cursor.getLong(sizeColumn)
          val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

          mediaList.add(
            CapturedMedia(
              uri = uri,
              path = uri.toString(),
              displayName = name,
              dateTaken = dateAdded,
              isVideo = true,
              durationMs = duration,
              width = width,
              height = height,
              sizeBytes = size
            )
          )
          count++
        }
      }
    } catch (_: Exception) {}

    // Sort descending by dateTaken
    mediaList.sortedByDescending { it.dateTaken }
  }

  suspend fun deleteMedia(media: CapturedMedia): Boolean = withContext(Dispatchers.IO) {
    try {
      val deletedRows = context.contentResolver.delete(media.uri, null, null)
      deletedRows > 0
    } catch (e: Exception) {
      false
    }
  }

  fun createShareIntent(media: CapturedMedia): Intent {
    val mimeType = if (media.isVideo) "video/mp4" else "image/jpeg"
    return Intent(Intent.ACTION_SEND).apply {
      type = mimeType
      putExtra(Intent.EXTRA_STREAM, media.uri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
  }
}
