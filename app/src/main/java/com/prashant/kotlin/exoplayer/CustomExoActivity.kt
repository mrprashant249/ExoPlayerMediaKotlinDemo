package com.prashant.kotlin.exoplayer

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.util.Formatter
import java.util.Locale
import androidx.core.net.toUri

/**
 * Created by BytesBee.
 *
 * @author BytesBee Infotech (Prashant Adesara)
 * @link <a href="https://bytesbee.com">BytesBee</a>
 */
class CustomExoActivity : AppCompatActivity() {
    companion object {
        private const val REWIND_TIME_MS = 5000 // 5 seconds
        private const val FORWARD_TIME_MS = 15000 // 15 seconds
        private const val CONTROLS_HIDE_TIMEOUT_MS = 5000 // 5 seconds
    }

    private lateinit var playerView: PlayerView
    private var player: ExoPlayer? = null

    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnRewind: ImageButton
    private lateinit var btnForward: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var currentPosition: TextView
    private lateinit var totalDuration: TextView
    private lateinit var tvLive: TextView
    private lateinit var imgSettings: ImageView
    private lateinit var controlsContainer: View

    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private val formatBuilder = StringBuilder()
    private val formatter = Formatter(formatBuilder, Locale.getDefault())
    private val hideControlsRunnable = Runnable { hideControls() }
    private var currentSpeed = 1.0f
    private var videoUrl: String = Constants.DEFAULT_URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_exo)

        initializeViews()
        getData()
        initializePlayer()
        setupListeners()
    }

    private fun initializeViews() {
        playerView = findViewById(R.id.player_view)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnRewind = findViewById(R.id.btn_rewind)
        btnForward = findViewById(R.id.btn_forward)
        seekBar = findViewById(R.id.seek_bar)
        currentPosition = findViewById(R.id.current_position)
        totalDuration = findViewById(R.id.total_duration)
        tvLive = findViewById(R.id.tv_live)
        imgSettings = findViewById(R.id.imgSettings)
        controlsContainer = findViewById(R.id.controls_container)
    }

    private fun getData() {
        try {
            videoUrl = intent.getStringExtra(Constants.KEY_URL) ?: Constants.DEFAULT_URL
        } catch (ignored: Exception) {
        }
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        player?.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(
                    this@CustomExoActivity,
                    getString(R.string.error_playback),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    updateTotalDuration()
                    updatePlayPauseButton()
                }
            }
        })

        loadVideo()
    }

    private fun setupListeners() {
        btnPlayPause.setOnClickListener {
            player?.let {
                if (isPlaying) {
                    it.pause()
                } else {
                    it.play()
                }
                updatePlayPauseButton()
                resetHideControlsTimer()
            }
        }

        btnRewind.setOnClickListener {
            player?.let {
                it.seekTo((it.currentPosition - REWIND_TIME_MS).coerceAtLeast(0))
                resetHideControlsTimer()
            }
        }

        btnForward.setOnClickListener {
            player?.let {
                it.seekTo((it.currentPosition + FORWARD_TIME_MS).coerceAtMost(it.duration))
                resetHideControlsTimer()
            }
        }

        imgSettings.setOnClickListener { showSpeedPopup() }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player?.seekTo(progress.toLong())
                    updateCurrentPosition()
                    resetHideControlsTimer()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacks(hideControlsRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                resetHideControlsTimer()
            }
        })

//        playerView.setOnTouchListener { v, event -> onTouch(v, event) }
        playerView.setOnTouchListener(this::onTouch);
        startPositionUpdateTimer()
    }

    private fun toggleControls() {
        if (controlsContainer.visibility == View.VISIBLE) {
            hideControls()
        } else {
            showControls()
        }
    }

    private fun showControls() {
        controlsContainer.visibility = View.VISIBLE
        resetHideControlsTimer()
    }

    private fun hideControls() {
        controlsContainer.visibility = View.GONE
    }

    private fun resetHideControlsTimer() {
        handler.removeCallbacks(hideControlsRunnable)
        handler.postDelayed(hideControlsRunnable, CONTROLS_HIDE_TIMEOUT_MS.toLong())
    }

    private fun startPositionUpdateTimer() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                updateCurrentPosition()
                updateSeekBar()
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    private fun updateCurrentPosition() {
        player?.let {
            currentPosition.text = stringForTime(it.currentPosition)
        }
    }

    private fun updateTotalDuration() {
        player?.let {
            totalDuration.text = stringForTime(it.duration)
            seekBar.max = it.duration.toInt()
        }
    }

    private fun updateSeekBar() {
        player?.let {
            seekBar.progress = it.currentPosition.toInt()
        }
    }

    private fun updatePlayPauseButton() {
        isPlaying = player?.isPlaying ?: false
        btnPlayPause.setImageResource(
            if (isPlaying) R.drawable.ic_exo_pause else R.drawable.ic_exo_play
        )
    }

    private fun stringForTime(timeMs: Long): String {
        val timeMs = timeMs.coerceAtLeast(0)
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        formatBuilder.setLength(0)
        return formatter.format("%02d:%02d", minutes, seconds).toString()
    }

    private fun loadVideo() {
        if (videoUrl.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_invalid_url), Toast.LENGTH_SHORT).show()
            return
        }

        val mediaItem = MediaItem.fromUri(videoUrl.toUri())
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()

        tvLive.visibility = if (isLiveStreamUrl(videoUrl)) View.VISIBLE else View.GONE
        updatePlayPauseButton();
        showControls();
    }

    private fun isLiveStreamUrl(url: String): Boolean {
        return url.contains(".m3u8") || url.contains(".mpd")
    }

    @SuppressLint("InflateParams")
    private fun showSpeedPopup() {
        val speeds = floatArrayOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        val popupView = layoutInflater.inflate(R.layout.popup_speed_menu, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            isFocusable = true
            setBackgroundDrawable(ContextCompat.getDrawable(this@CustomExoActivity, android.R.color.transparent))
        }

        speeds.forEach { speed ->
            val button = popupView.findViewById<TextView>(getSpeedButtonId(speed))
            if (speed == currentSpeed) {
                button?.setTextColor(ContextCompat.getColor(this@CustomExoActivity, android.R.color.holo_blue_light))
            }
            button?.setOnClickListener {
                setPlaybackSpeed(speed)
                popupWindow.dismiss()
            }
        }

        // Calculate position to show popup above the settings image
        val location = IntArray(2)
        imgSettings.getLocationOnScreen(location)

        // Show popup above the settings image
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val xOffset = -popupView.measuredWidth + imgSettings.width
        val yOffset = -popupView.measuredHeight - imgSettings.height

        popupWindow.showAsDropDown(imgSettings, xOffset, yOffset)
    }

    private fun getSpeedButtonId(speed: Float): Int {
        return when (speed) {
            0.25f -> R.id.speed_025
            0.5f -> R.id.speed_05
            0.75f -> R.id.speed_075
            1.0f -> R.id.speed_normal
            1.25f -> R.id.speed_125
            1.5f -> R.id.speed_15
            else -> R.id.speed_2
        }
    }

    private fun setPlaybackSpeed(speed: Float) {
        currentSpeed = speed
        player?.setPlaybackParameters(PlaybackParameters(speed))
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
        handler.removeCallbacksAndMessages(null)
    }

    private fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                toggleControls()
                return true
            }
        }
        return false
    }
} 