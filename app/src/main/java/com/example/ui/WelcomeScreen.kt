package com.example.ui

import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import android.graphics.SurfaceTexture
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoScreenBackground

/**
 * Numo Official Animated Piggy-Bank Splash Screen.
 * Plays the supplied MP4 animation automatically on warm cream background,
 * reveals the bottom-center downward arrow ONLY on actual playback completion,
 * and advances on arrow tap or left swipe without auto-redirecting.
 */
@Composable
fun WelcomeScreen(
  skipAnimation: Boolean = false,
  onSplashAnimationFinished: () -> Unit = {},
  onGetStarted: () -> Unit
) {
  val context = LocalContext.current
  val density = LocalDensity.current
  val swipeThresholdPx = remember(density) { with(density) { 64.dp.toPx() } }

  var isAnimationComplete by remember { mutableStateOf(skipAnimation) }
  var hasNavigated by remember { mutableStateOf(false) }
  var videoAspectRatio by remember { mutableFloatStateOf(9f / 16f) }

  val mediaPlayerHolder = remember { mutableStateOf<MediaPlayer?>(null) }

  fun navigateOnce() {
    if (isAnimationComplete && !hasNavigated) {
      hasNavigated = true
      onSplashAnimationFinished()
      onGetStarted()
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      runCatching {
        mediaPlayerHolder.value?.let { mp ->
          mp.setOnCompletionListener(null)
          mp.setOnErrorListener(null)
          mp.setOnPreparedListener(null)
          if (mp.isPlaying) mp.stop()
          mp.release()
        }
      }
      mediaPlayerHolder.value = null
    }
  }

  // Subtle downward floating & pulsing animation for the completion arrow
  val infiniteTransition = rememberInfiniteTransition(label = "splashArrowPulse")
  val arrowOffsetY by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 8f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "arrowOffsetY"
  )
  val arrowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.78f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "arrowAlpha"
  )

  NumoScreenBackground(
    modifier = Modifier.pointerInput(isAnimationComplete) {
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
    Box(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp)
    ) {
      // Centered Piggy-Bank & Coins MP4 Animation preserving aspect ratio
      Box(
        modifier = Modifier
          .align(Alignment.Center)
          .fillMaxWidth()
          .aspectRatio(videoAspectRatio.coerceIn(0.45f, 1.8f))
          .testTag("welcome_hero_illustration"),
        contentAlignment = Alignment.Center
      ) {
        AndroidView(
          factory = { ctx ->
            TextureView(ctx).apply {
              isOpaque = false
              surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                  if (mediaPlayerHolder.value != null) return
                  try {
                    val uri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.piggy_bank_splash}")
                    val mp = MediaPlayer().apply {
                      setDataSource(ctx, uri)
                      setSurface(Surface(surfaceTexture))
                      isLooping = false
                      setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                      setOnVideoSizeChangedListener { _, w, h ->
                        if (w > 0 && h > 0) {
                          videoAspectRatio = w.toFloat() / h.toFloat()
                        }
                      }
                      setOnPreparedListener { player ->
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
                        // Graceful fallback so user is never stuck if codec fails
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

                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) = Unit
                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
              }
            }
          },
          modifier = Modifier.fillMaxSize()
        )
      }

      // Bottom-Center Downward Arrow: strictly hidden until animation genuinely finishes
      AnimatedVisibility(
        visible = isAnimationComplete,
        enter = fadeIn(tween(350)),
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 32.dp)
      ) {
        Box(
          modifier = Modifier
            .size(56.dp)
            .graphicsLayer {
              translationY = with(density) { arrowOffsetY.dp.toPx() }
              alpha = arrowAlpha
            }
            .shadow(10.dp, CircleShape, spotColor = NumoDS.HeroTealMid)
            .clip(CircleShape)
            .background(NumoDS.Turquoise)
            .clickable(enabled = isAnimationComplete && !hasNavigated) {
              navigateOnce()
            }
            .testTag("welcome_get_started_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Continue to Onboarding",
            tint = Color.White,
            modifier = Modifier.size(30.dp)
          )
        }
      }
    }
  }
}
