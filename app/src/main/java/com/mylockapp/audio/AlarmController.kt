package com.mylockapp.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.mylockapp.R

/** High-volume intruder alarm on STREAM_ALARM (bypasses media volume). */
class AlarmController(private val context: Context) {
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val handler = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null
    private var previousVolume = -1

    companion object { const val AUTO_STOP_MS = 60_000L }

    @Synchronized
    fun start() {
        if (player != null) return
        previousVolume = audio.getStreamVolume(AudioManager.STREAM_ALARM)
        audio.setStreamVolume(AudioManager.STREAM_ALARM, audio.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0)

        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            context.resources.openRawResourceFd(R.raw.alarm_siren).use {
                setDataSource(it.fileDescriptor, it.startOffset, it.length)
            }
            isLooping = true
            prepare()
            start()
        }
        handler.postDelayed({ stop() }, AUTO_STOP_MS)
    }

    @Synchronized
    fun stop() {
        handler.removeCallbacksAndMessages(null)
        player?.runCatching { stop(); release() }
        player = null
        if (previousVolume >= 0) {
            audio.setStreamVolume(AudioManager.STREAM_ALARM, previousVolume, 0)
            previousVolume = -1
        }
    }

    val isRinging get() = player != null
}
