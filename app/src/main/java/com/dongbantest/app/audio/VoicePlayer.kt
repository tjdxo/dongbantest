package com.dongbantest.app.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 저장된 음성 메모 재생기. 한 번에 하나의 파일만 재생한다. */
class VoicePlayer {

    private var player: MediaPlayer? = null
    private val _playingPath = MutableStateFlow<String?>(null)
    val playingPath: StateFlow<String?> = _playingPath.asStateFlow()

    /** @return 재생 시작 성공 여부 */
    fun toggle(path: String): Boolean {
        if (_playingPath.value == path) {
            stop()
            return true
        }
        stop()
        return try {
            val p = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(path)
                setOnCompletionListener { stop() }
                prepare()
                start()
            }
            player = p
            _playingPath.value = path
            true
        } catch (e: Exception) {
            Log.w("VoicePlayer", "play failed", e)
            stop()
            false
        }
    }

    fun stop() {
        player?.let { p -> runCatching { p.stop() }; runCatching { p.release() } }
        player = null
        _playingPath.value = null
    }
}
