package com.limpio.app

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Photo(val id: Long, val uri: Uri, val size: Long)

class LimpioViewModel(app: Application) : AndroidViewModel(app) {

    /** Fotos que quedan por revisar (la primera es la más reciente). */
    var photos by mutableStateOf<List<Photo>>(emptyList())
        private set

    /** Posición de la foto que se está mostrando. */
    var index by mutableIntStateOf(0)
        private set

    var loaded by mutableStateOf(false)
        private set

    var keptTotal by mutableIntStateOf(0)
        private set

    var deletedTotal by mutableIntStateOf(0)
        private set

    /** Fotos marcadas para eliminar (se borran todas juntas al confirmar). */
    val pending = mutableStateListOf<Photo>()

    // true = se marcó para eliminar, false = se conservó
    private val history = ArrayList<Boolean>()

    val current: Photo? get() = photos.getOrNull(index)
    val next: Photo? get() = photos.getOrNull(index + 1)
    val canUndo: Boolean get() = index > 0
    val pendingBytes: Long get() = pending.sumOf { it.size }

    fun load() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) { queryPhotos() }
            photos = list
            index = 0
            keptTotal = 0
            deletedTotal = 0
            pending.clear()
            history.clear()
            loaded = true
        }
    }

    private fun queryPhotos(): List<Photo> {
        val out = ArrayList<Photo>()
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.SIZE,
        )
        val order = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        getApplication<Application>().contentResolver
            .query(collection, projection, null, null, order)
            ?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    out.add(Photo(id, ContentUris.withAppendedId(collection, id), c.getLong(sizeCol)))
                }
            }
        return out
    }

    /** delete = true → izquierda (eliminar); false → derecha (conservar). */
    fun decide(delete: Boolean) {
        val photo = current ?: return
        if (delete) pending.add(photo) else keptTotal++
        history.add(delete)
        index++
    }

    fun undo() {
        if (!canUndo || history.isEmpty()) return
        val wasDelete = history.removeAt(history.lastIndex)
        index--
        if (wasDelete) pending.removeAt(pending.lastIndex) else keptTotal--
    }

    /** Se llama cuando el sistema confirma que las fotos marcadas fueron eliminadas. */
    fun onDeleted() {
        deletedTotal += pending.size
        photos = photos.drop(index) // solo quedan las que aún no se revisaron
        index = 0
        pending.clear()
        history.clear()
    }
}
