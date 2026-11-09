package com.dongbantest.app.data.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * 앱 전용 내부 저장소(filesDir) 에 음성/사진 파일을 관리한다.
 * - 외부 저장소 권한이 필요 없다.
 * - 사진은 Photo Picker 로 받은 URI 를 앱 전용 폴더로 복사한다.
 *   (임시 URI 권한이 만료되어도 재실행 후 계속 볼 수 있도록)
 */
class MediaFileStore(context: Context) {

    private val appContext = context.applicationContext
    private val voiceDir = File(appContext.filesDir, "voice").apply { mkdirs() }
    private val photoDir = File(appContext.filesDir, "photos").apply { mkdirs() }

    fun newVoiceFile(): File = File(voiceDir, "voice_${System.currentTimeMillis()}.m4a")

    suspend fun copyPhotoToAppStorage(source: Uri): File = withContext(Dispatchers.IO) {
        val target = File(photoDir, "photo_${UUID.randomUUID()}.jpg")
        val input = appContext.contentResolver.openInputStream(source)
            ?: error("사진을 열 수 없습니다: $source")
        input.use { inp -> target.outputStream().use { out -> inp.copyTo(out) } }
        target
    }

    /** 앱 전용 저장소 내부 파일만 삭제한다 (안전장치). */
    suspend fun delete(path: String?) = withContext(Dispatchers.IO) {
        if (path.isNullOrBlank()) return@withContext
        val file = File(path)
        val root = appContext.filesDir.canonicalPath
        if (file.canonicalPath.startsWith(root) && file.exists()) file.delete()
    }
}
