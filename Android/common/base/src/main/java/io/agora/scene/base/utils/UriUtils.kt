package io.agora.scene.base.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore

object UriUtils {

    fun getFilePathByUri(context: Context, uri: Uri): String? {
        var path: String? = null
        
        when {
            uri.scheme == "file" -> {
                path = uri.path
            }
            
            DocumentsContract.isDocumentUri(context, uri) -> {
                when {
                    isExternalStorageDocument(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":").filter { it.isNotEmpty() }
                        val type = split[0]
                        if (type.equals("primary", ignoreCase = true)) {
                            path = "${Environment.getExternalStorageDirectory()}/${split[1]}"
                        }
                    }
                    
                    isDownloadsDocument(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            docId.toLong()
                        )
                        path = getDataColumn(context, contentUri, null, null)
                    }
                    
                    isMediaDocument(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":").filter { it.isNotEmpty() }
                        val type = split[0]
                        
                        val contentUri = when (type) {
                            "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            else -> null
                        }
                        
                        val selection = "_id=?"
                        val selectionArgs = arrayOf(split[1])
                        path = getDataColumn(context, contentUri, selection, selectionArgs)
                    }
                }
            }
            
            uri.scheme == "content" -> {
                context.contentResolver.query(uri, arrayOf("_data"), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndexOrThrow("_data")
                        if (columnIndex > -1) {
                            path = cursor.getString(columnIndex)
                        }
                    }
                }
            }
            
            else -> path = uri.toString()
        }
        
        return path
    }

    private fun getDataColumn(
        context: Context, 
        uri: Uri?, 
        selection: String?, 
        selectionArgs: Array<String>?
    ): String? {
        uri ?: return null
        
        val column = "_data"
        val projection = arrayOf(column)
        
        return try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(column)
                    cursor.getString(columnIndex)
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun isExternalStorageDocument(uri: Uri) =
        uri.authority == "com.android.externalstorage.documents"

    private fun isDownloadsDocument(uri: Uri) =
        uri.authority == "com.android.providers.downloads.documents"

    private fun isMediaDocument(uri: Uri) =
        uri.authority == "com.android.providers.media.documents"
} 