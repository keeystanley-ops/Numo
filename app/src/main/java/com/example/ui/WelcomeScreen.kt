package com.example.ui

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
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
 * reveals the white & gold-glowing downward arrow button ONLY after playback completes,
 * and advances to onboarding on tap or left swipe without auto-redirecting.
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
  val surfaceHolder = remember { mutableStateOf<Surface?>(null) }
  val isSurfaceActive = remember { mutableStateOf(false) }

  fun navigateOnce() {
    if (isAnimationComplete && !hasNavigated) {
      hasNavigated = true
      onSplashAnimationFinished()
      onGetStarted()
    }
  }

  fun releasePlayerAndSurface() {
    isSurfaceActive.value = false
    val mp = mediaPlayerHolder.value
    mediaPlayerHolder.value = null
    if (mp != null) {
      runCatching { mp.setOnCompletionListener(null) }
      runCatching { mp.setOnErrorListener(null) }
      runCatching { mp.setOnPreparedListener(null) }
      runCatching { mp.setOnVideoSizeChangedListener(null) }
      runCatching { mp.setSurface(null) }
      runCatching { if (mp.isPlaying) mp.stop() }
      runCatching { mp.reset() }
      runCatching { mp.release() }
    }
    val surf = surfaceHolder.value
    surfaceHolder.value = null
    if (surf != null) {
      runCatching { surf.release() }
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
      releasePlayerAndSurface()
    }
  }

  // Subtle floating & pulsing glow for the white + gold completion arrow
  val infiniteTransition = rememberInfiniteTransition(label = "whiteGoldArrowPulse")
  val arrowFloatY by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 7f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "arrowFloatY"
  )
  val glowPulse by infiniteTransition.animateFloat(
    initialValue = 0.72f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glowPulse"
  )

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
        val textureView = TextureView(ctx)
        textureView.apply {
          isOpaque = true
          surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
              releasePlayerAndSurface()
              isSurfaceActive.value = true
              val surface = Surface(surfaceTexture)
              surfaceHolder.value = surface
              try {
                val mp = MediaPlayer().apply {
                  val afd = runCatching { ctx.resources.openRawResourceFd(R.raw.piggy_bank_splash) }.getOrNull()
                  if (afd != null) {
                    afd.use {
                      setDataSource(it.fileDescriptor, it.startOffset, it.length)
                    }
                  } else {
                    val uri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.piggy_bank_splash}")
                    setDataSource(ctx, uri)
                  }
                  setSurface(surface)
                  isLooping = false
                  setOnVideoSizeChangedListener { _, w, h ->
                    if (isSurfaceActive.value && w > 0 && h > 0) {
                      lastVideoW = w
                      lastVideoH = h
                      applyCenterCropTransform(textureView, w, h)
                    }
                  }
                  setOnPreparedListener { player ->
                    if (!isSurfaceActive.value || !textureView.isAvailable) {
                      releasePlayerAndSurface()
                      return@setOnPreparedListener
                    }
                    applyCenterCropTransform(textureView, lastVideoW, lastVideoH)
                    if (skipAnimation) {
                      val targetPos = (player.duration - 40).coerceAtLeast(0)
                      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        player.seekTo(targetPos.toLong(), MediaPlayer.SEEK_CLOSEST)
                      } else {
                        player.seekTo(targetPos)
                      }
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
                releasePlayerAndSurface()
                isAnimationComplete = true
                onSplashAnimationFinished()
              }
            }

            override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
              applyCenterCropTransform(textureView, lastVideoW, lastVideoH)
            }

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
              releasePlayerAndSurface()
              return true
            }

            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit
          }
        }
      },
      onRelease = {
        releasePlayerAndSurface()
      },
      modifier = Modifier
        .fillMaxSize()
        .testTag("welcome_hero_illustration")
    )

    // Bottom-center White Circular Arrow with Warm Goldish Glow & Sparkles
    // Hidden completely while animation plays; appears only when playback finishes.
    AnimatedVisibility(
      visible = isAnimationComplete,
      enter = fadeIn(tween(380)),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .navigationBarsPadding()
        .padding(bottom = 42.dp)
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer {
          translationY = with(density) { arrowFloatY.dp.toPx() }
        }
      ) {
        // 3 little gold sparkle rays above the white circle
        Canvas(modifier = Modifier.size(width = 36.dp, height = 14.dp)) {
          val rayColor = Color(0xFFF59E0B).copy(alpha = glowPulse)
          val cx = size.width / 2f
          drawLine(
            color = rayColor,
            start = Offset(cx, 2f),
            end = Offset(cx, size.height - 2f),
            strokeWidth = 2.4.dp.toPx(),
            cap = StrokeCap.Round
          )
          drawLine(
            color = rayColor,
            start = Offset(cx - 10.dp.toPx(), 5f),
            end = Offset(cx - 7.dp.toPx(), size.height - 1f),
            strokeWidth = 2.2.dp.toPx(),
            cap = StrokeCap.Round
          )
          drawLine(
            color = rayColor,
            start = Offset(cx + 10.dp.toPx(), 5f),
            end = Offset(cx + 7.dp.toPx(), size.height - 1f),
            strokeWidth = 2.2.dp.toPx(),
            cap = StrokeCap.Round
          )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Box(
          modifier = Modifier
            .size(92.dp)
            .semantics { contentDescription = "Continue to Onboarding" }
            .clickable(
              enabled = !hasNavigated,
              role = Role.Button
            ) {
              navigateOnce()
            }
            .testTag("welcome_get_started_button"),
          contentAlignment = Alignment.Center
        ) {
          // Warm goldish outer glow halo
          Box(
            modifier = Modifier
              .size(86.dp)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  colors = listOf(
                    Color(0xFFFBBF24).copy(alpha = 0.42f * glowPulse),
                    Color(0xFFF59E0B).copy(alpha = 0.16f * glowPulse),
                    Color.Transparent
                  )
                )
              )
          )

          // Crisp White Circular Button with Dark Teal/Umber Downward Chevron
          Box(
            modifier = Modifier
              .size(60.dp)
              .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = Color(0xFFF59E0B),
                spotColor = Color(0xFFF59E0B)
              )
              .clip(CircleShape)
              .background(Color.White),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription = null,
              tint = Color(0xFF134E4A),
              modifier = Modifier.size(32.dp)
            )
          }
        }
      }
    }
  }
}
