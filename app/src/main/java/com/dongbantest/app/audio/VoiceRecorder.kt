package com.dongbantest.app.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

/**
 * 명시적 시작/종료 방식의 음성 메모 녹음기 (Android MediaRecorder).
 * - 상시/백그라운드 녹음을 하지 않는다. 사용자가 버튼을 눌렀을 때만 녹음한다.
 * - 화면을 벗어나면 ViewModel 이 [cancel] 을 호출해 녹음을 중단한다.
 * - 차량 마이크 선택/좌석별 분리는 공식 API 지원이 확인되지 않아 구현하지 않는다 (기본 MIC 사용).
 */
class VoiceRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startedAt: Long = 0L

    val isRecording: Boolean get() = recorder != null

    /** @throws Exception 녹음 시작 실패 시 (마이크 사용 불가 등) */
    fun start(target: File) {
        check(recorder == null) { "이미 녹음 중입니다" }
        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
        try {
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioEncodingBitRate(96_000)
            r.setAudioSamplingRate(44_100)
            r.setOutputFile(target.absolutePath)
            r.prepare()
            r.start()
        } catch (e: Exception) {
            runCatching { r.release() }
            target.delete()
            throw e
        }
        recorder = r
        outputFile = target
        startedAt = System.currentTimeMillis()
    }

    /** 녹음을 정상 종료하고 결과 파일과 길이를 반환한다. 너무 짧아 실패하면 null. */
    fun stop(): Pair<File, Long>? {
        val r = recorder ?: return null
        val file = outputFile
        val duration = System.currentTimeMillis() - startedAt
        recorder = null
        outputFile = null
        return try {
            r.stop()
            if (file != null && file.exists() && file.length() > 0) file to duration else null
        } catch (e: RuntimeException) {
            // 녹음이 너무 짧으면 stop() 이 예외를 던진다.
            Log.w(TAG, "stop failed", e)
            file?.delete()
            null
        } finally {
            runCatching { r.release() }
        }
    }

    /** 녹음을 취소하고 파일을 지운다. */
    fun cancel() {
        val file = outputFile
        stop()
        file?.delete()
    }

    companion object {
        private const val TAG = "VoiceRecorder"
    }
}
