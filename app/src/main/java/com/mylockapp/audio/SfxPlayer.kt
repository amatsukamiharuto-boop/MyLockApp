package com.mylockapp.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.mylockapp.R

/** Low-latency voice clips via SoundPool. Assets: res/raw/access_granted.*, access_denied.* */
class SfxPlayer(context: Context) {
    enum class Clip { GRANTED, DENIED }

    private val pool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        ).build()

    private val ids = mutableMapOf<Clip, Int>()
    private val ready = mutableSetOf<Int>()
    private val pending = mutableSetOf<Int>()

    init {
        pool.setOnLoadCompleteListener { sp, id, status ->
            if (status == 0) {
                ready += id
                if (pending.remove(id)) sp.play(id, 1f, 1f, 1, 0, 1f)
            }
        }
        ids[Clip.GRANTED] = pool.load(context, R.raw.access_granted, 1)
        ids[Clip.DENIED] = pool.load(context, R.raw.access_denied, 1)
    }

    fun play(clip: Clip) {
        val id = ids[clip] ?: return
        if (id in ready) pool.play(id, 1f, 1f, 1, 0, 1f) else pending += id
    }

    fun release() = pool.release()
}
