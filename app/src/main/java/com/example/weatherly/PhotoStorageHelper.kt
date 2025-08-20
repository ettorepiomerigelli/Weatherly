package com.example.weatherly

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object PhotoStorageHelper {

    private const val PHOTO_FILE_NAME = "memory_photo.jpg"

    // Salva la foto nella cartella privata dell'app
    fun savePhoto(context: Context, locationName: String, uri: Uri) {
        try {
            val dir = File(context.getExternalFilesDir("photos/$locationName")?.absolutePath ?: "")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, PHOTO_FILE_NAME)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Carica la foto salvata (se esiste)
    fun loadPhoto(context: Context, locationName: String): Uri? {
        val file = File(context.getExternalFilesDir("photos/$locationName")?.absolutePath, PHOTO_FILE_NAME)
        return if (file.exists()) Uri.fromFile(file) else null
    }
}
