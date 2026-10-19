package com.example.videocamerasource
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
object PlayerManager {
    var player: ExoPlayer? = null
        private set
    var currentUri: Uri? = null
    fun getOrCreate(context: Context): ExoPlayer {
        if (player == null) {
            player = ExoPlayer.Builder(context.applicationContext).build().apply {
                repeatMode = ExoPlayer.REPEAT_MODE_ALL
                playWhenReady = true
            }
        }
        return player!!
    }
    fun setVideo(uri: Uri) {
        currentUri = uri
        val p = player ?: return
        p.setMediaItem(MediaItem.fromUri(uri))
        p.prepare()
        p.play()
    }
    fun release() {
        player?.release()
        player = null
    }
}
