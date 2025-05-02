package com.prashant.kotlin.exoplayer

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * Created by BytesBee.
 *
 * @author BytesBee Infotech (Prashant Adesara)
 * @link <a href="https://bytesbee.com">BytesBee</a>
 */
class MainActivity : AppCompatActivity() {
    private lateinit var playerView: PlayerView
    private var player: ExoPlayer? = null
    private var videoUrl: String? = null

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        playerView = findViewById(R.id.player_view)
        playerView.setShowNextButton(false)
        playerView.setShowPreviousButton(false)
        getData()

        // Initialize player
        initializePlayer()
    }

    private fun getData() {
        try {
            videoUrl = intent.getStringExtra(Constants.KEY_URL)
        } catch (ignored: Exception) {
        }
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        // Add a listener to handle playback errors
        player?.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.error_playback),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // Load the default URL
        loadVideo()
    }

    private fun loadVideo() {
        if (videoUrl.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_invalid_url), Toast.LENGTH_SHORT).show()
            return
        }

        // Create a MediaItem
        val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))

        // Set the media item to be played
        player?.setMediaItem(mediaItem)

        // Prepare the player
        player?.prepare()

        // Start playing automatically
        player?.play()
    }

    override fun onResume() {
        super.onResume()
        if (player == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
} 