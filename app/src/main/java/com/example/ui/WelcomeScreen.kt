package com.example.ui

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.components.NumoDS

/**
 * Numo Official Full-Screen Animated Piggy-Bank Splash Screen.
 * Fills the entire viewport edge-to-edge using the supplied 1080p MP4 asset,
 * keeps the video's own downward arrow disabled until playback finishes,
 * and advances to onboarding when the user taps the video's arrow or swipes left.
 */
@Composable
fun WelcomeScreen(
  skipAnimation: Boolean = false,
  onSplashAnimationFinished: () -> Unit = {},
  onGetStarted: () -> Unit
) {
  val density = LocalDensity.current
  val swipeThresholdPx = remember(density) { with(density) { 64.dp.toPx() } }

  var isAnimationComplete by remember { mutableStateOf(skipAnimation) }
  var hasNavigated by remember { mutableStateOf(false) }

  val mediaPlayerHolder = remember { mutableStateOf<MediaPlayer?>(null) }

  fun navigateOnce() {
    if (isAnimationComplete && !hasNavigated) {
      hasNavigated = true
      onSplashAnimationFinished()
      onGetStarted()
    }
  }

  fun applyCenterCropTransform(view: TextureView, videoW: Int, videoH: Int) {
    val viewW = view.width.toFloat()
    val viewH = view.height.toFloat()
    if (viewW <= 0f || viewH <= 0f || videoW <= 0 || videoH <= 0) return

    val videoRatio = videoW.toFloat() / videoH.toFloat()
    val viewRatio = viewW / viewH
    val scaleX: Float
    val scaleY: Float
    if (videoRatio > viewRatio) {
      scaleX = videoRatio / viewRatio
      scaleY = 1f
    } else {
      scaleX = 1f
      scaleY = viewRatio / videoRatio
    }

    val matrix = Matrix().apply {
      setScale(scaleX, scaleY, viewW / 2f, viewH / 2f)
    }
    view.setTransform(matrix)
  }

  DisposableEffect(Unit) {
    onDispose {
      runCatching {
        mediaPlayerHolder.value?.let { mp ->
          mp.setOnCompletionListener(null)
          mp.setOnErrorListener(null)
          mp.setOnPreparedListener(null)
          mp.setOnVideoSizeChangedListener(null)
          if (mp.isPlaying) mp.stop()
          mp.release()
        }
      }
      mediaPlayerHolder.value = null
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(NumoDS.CreamBg)
      .pointerInput(isAnimationComplete) {
        if (!isAnimationComplete) return@pointerInput
        var cumulativeDragX = 0f
        detectHorizontalDragGestures(
          onDragStart = { cumulativeDragX = 0f },
          onDragEnd = {
            if (cumulativeDragX <= -swipeThresholdPx) {
              navigateOnce()
            }
            cumulativeDragX = 0f
          },
          onDragCancel = { cumulativeDragX = 0f },
          onHorizontalDrag = { _, dragAmount ->
            cumulativeDragX += dragAmount
            if (cumulativeDragX <= -swipeThresholdPx) {
              navigateOnce()
            }
          }
        )
      }
  ) {
    // Full-screen edge-to-edge MP4 Video Player
    AndroidView(
      factory = { ctx ->
        var lastVideoW = 1080
        var lastVideoH = 1920
        TextureView(ctx).apply {
          isOpaque = true
          surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
              if (mediaPlayerHolder.value != null) return
              try {
                val uri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.piggy_bank_splash}")
                val mp = MediaPlayer().apply {
                  setDataSource(ctx, uri)
                  setSurface(Surface(surfaceTexture))
                  isLooping = false
                  setOnVideoSizeChangedListener { _, w, h ->
                    if (w > 0 && h > 0) {
                      lastVideoW = w
                      lastVideoH = h
                      applyCenterCropTransform(this@apply, w, h)
                    }
                  }
                  setOnPreparedListener { player ->
                    applyCenterCropTransform(this@apply, lastVideoW, lastVideoH)
                    if (skipAnimation) {
                      val dur = player.duration
                      if (dur > 0) player.seekTo(dur)
                      isAnimationComplete = true
                    } else {
                      player.start()
                    }
                  }
                  setOnCompletionListener {
                    isAnimationComplete = true
                    onSplashAnimationFinished()
                  }
                  setOnErrorListener { _, _, _ ->
                    isAnimationComplete = true
                    onSplashAnimationFinished()
                    true
                  }
                  prepareAsync()
                }
                mediaPlayerHolder.value = mp
              } catch (_: Exception) {
                isAnimationComplete = true
                onSplashAnimationFinished()
              }
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
              applyCenterCropTransform(this@apply, lastVideoW, lastVideoH)
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
          }
        }
      },
      modifier = Modifier
        .fillMaxSize()
        .testTag("welcome_hero_illustration")
    )

    // Invisible touch target aligned directly over the white circular arrow inside the video,
    // activated strictly after the animation playback completes.
    if (isAnimationComplete) {
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 110.dp)
          .size(120.dp)
          .clip(CircleShape)
          .semantics {
            contentDescription = "Continue to Onboarding"
          }
          .clickable(
            enabled = !hasNavigated,
            role = Role.Button,
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          ) {
            navigateOnce()
          }
          .testTag("welcome_get_started_button")
      )
    }
  }
}
