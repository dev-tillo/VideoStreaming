package uz.devtillo.testappscreenrecorder

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecAdapter
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


class MainActivity : AppCompatActivity() {

    private lateinit var constraintLayoutRoot: ConstraintLayout
    private lateinit var exoPlayerView: PlayerView
    private lateinit var simpleEcoPlayer: SimpleExoPlayer
    private lateinit var mediaSource: MediaSource
    private lateinit var urlType: URLType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        findView()
        initPlayer()
    }

    private fun findView() {
        constraintLayoutRoot = findViewById(R.id.constraintLayoutRoot)
        exoPlayerView = findViewById(R.id.exoPlayerView)
    }

    private fun initPlayer() {
        simpleEcoPlayer = SimpleExoPlayer.Builder(this).build()
        simpleEcoPlayer.addListener(playListener)
        exoPlayerView.player = simpleEcoPlayer

        createMediaSource()
        simpleEcoPlayer.setMediaSource(mediaSource)
        simpleEcoPlayer.prepare()
    }

    private fun createMediaSource() {
//        urlType = URLType.MP4
//        urlType.url = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"

//        urlType = URLType.HLS
//        urlType.url = "https:/cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"
//        urlType.url = "https://bitmovin-a.akamaihd.net/content/art-of-motion_drm/m3u8s/11331.m3u8"
//        urlType.url = "https://s3-us-west-2.amazonaws.com/hls-playground/hls_gprs.m3u8"

        urlType = URLType.DASH
        urlType.url = "https://s3.amazonaws.com/_bc_dml/example-content/sintel_dash/sintel_vod.mpd"
//        urlType.url = "https://bitmovin-a.akamaihd.net/content/art-of-motion_drm/mpds/11331.mpd"
//

        simpleEcoPlayer.seekTo(0)
        when (urlType) {
            URLType.MP4 -> {
                val dataSourceFactory: DataSource.Factory =
                    DefaultDataSourceFactory(this, Util.getUserAgent(this, applicationInfo.name))

                mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                    MediaItem.fromUri(
                        Uri.parse(urlType.url)
                    )
                )
            }

            URLType.HLS -> {
                val dataSourceFactory: DataSource.Factory =
                    DefaultDataSourceFactory(this, Util.getUserAgent(this, applicationInfo.name))

                mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(
                    MediaItem.fromUri(
                        Uri.parse(urlType.url)
                    )
                )
            }

            URLType.DASH -> {
                val dataSourceFactory: DataSource.Factory =
                    DefaultDataSourceFactory(this, Util.getUserAgent(this, applicationInfo.name))

                mediaSource = DashMediaSource.Factory(dataSourceFactory).createMediaSource(
                    MediaItem.fromUri(
                        Uri.parse(urlType.url)
                    )
                )
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val constrainSet = ConstraintSet()

        constrainSet.connect(
            exoPlayerView.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP,
            0
        )
        constrainSet.connect(
            exoPlayerView.id,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM,
            0
        )
        constrainSet.connect(
            exoPlayerView.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            0
        )
        constrainSet.connect(
            exoPlayerView.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END,
            0
        )

        constrainSet.applyTo(constraintLayoutRoot)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUi()
        } else {
            showSystemUi()
            val layoutParams = exoPlayerView.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.dimensionRatio = "16:9"
        }
        window.decorView.requestLayout()
    }

    override fun onResume() {
        super.onResume()
        simpleEcoPlayer.playWhenReady = true
        simpleEcoPlayer.play()
    }

    override fun onPause() {
        super.onPause()
        simpleEcoPlayer.pause()
        simpleEcoPlayer.playWhenReady = false
    }

    override fun onStop() {
        super.onStop()
        simpleEcoPlayer.pause()
        simpleEcoPlayer.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        simpleEcoPlayer.removeListener(playListener)
        simpleEcoPlayer.stop()
        simpleEcoPlayer.clearMediaItems()

        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private var playListener = object : Player.Listener {
        override fun onRenderedFirstFrame() {
            super.onRenderedFirstFrame()

            if (urlType == URLType.HLS) {
                exoPlayerView.useController = false
            }
            if (urlType == URLType.MP4) {
                exoPlayerView.useController = true
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideSystemUi() {
        actionBar?.hide()

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }

    private fun showSystemUi() {
        actionBar?.show()

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }
}

enum class URLType(var url: String) {
    MP4(""),
    HLS(""),
    DASH("")
}