package com.example.ui

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoPrimaryButton
import kotlinx.coroutines.delay

/**
 * Screen 7: Account Ready Celebration Video Screen.
 * Plays the silent bundled account-ready animation edge-to-edge (audio track stripped and
 * MediaPlayer volume muted), then reveals "Go to Dashboard" and "Explore Features".
 */
@Composable
fun AllSetScreen(
  onGoToHome: () -> Unit,
  @Suppress("UNUSED_PARAMETER") onAddMoreAccounts: () -> Unit = {},
  onExploreFeatures: () -> Unit = onGoToHome
) {
  val lifecycleOwner = LocalLifecycleOwner.current

  var showActions by remember { mutableStateOf(false) }
  var isAnimationComplete by remember { mutableStateOf(false) }
  var isPausedByLifecycle by remember { mutableStateOf(false) }

  val mediaPlayerHolder = remember { mutableStateOf<MediaPlayer?>(null) }
  val surfaceHolder = remember { mutableStateOf<Surface?>(null) }
  val isSurfaceActive = remember { mutableStateOf(false) }

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

  fun replayAnimation() {
    val mp = mediaPlayerHolder.value ?: return
    isAnimationComplete = false
    runCatching {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        mp.seekTo(0L, MediaPlayer.SEEK_CLOSEST)
      } else {
        mp.seekTo(0)
      }
      mp.setVolume(0f, 0f)
      mp.start()
    }
  }

  // Reveal the bottom CTA cleanly once the Numo card settles (~2.2s into the animation)
  LaunchedEffect(Unit) {
    delay(2200L)
    showActions = true
  }

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      val mp = mediaPlayerHolder.value ?: return@LifecycleEventObserver
      when (event) {
        Lifecycle.Event.ON_PAUSE -> {
          runCatching {
            if (mp.isPlaying) {
              mp.pause()
              isPausedByLifecycle = true
            }
          }
        }
        Lifecycle.Event.ON_RESUME -> {
          if (isPausedByLifecycle && !isAnimationComplete) {
            isPausedByLifecycle = false
            runCatching {
              mp.setVolume(0f, 0f)
              mp.start()
            }
          }
        }
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
      releasePlayerAndSurface()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(NumoDS.PeachBgMid)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
      ) {
        if (!showActions) showActions = true
      }
      .testTag("all_set_screen")
  ) {
    // Full-screen edge-to-edge silent MP4 animation
    AndroidView(
      factory = { ctx ->
        var lastVideoW = 1080
        var lastVideoH = 1920
        val textureView = TextureView(ctx)
        textureView.apply {
          isOpaque = true
          surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
              surfaceTexture: SurfaceTexture,
              width: Int,
              height: Int
            ) {
              releasePlayerAndSurface()
              isSurfaceActive.value = true
              val surface = Surface(surfaceTexture)
              surfaceHolder.value = surface
              try {
                val mp = MediaPlayer().apply {
                  val afd = runCatching {
                    ctx.resources.openRawResourceFd(R.raw.account_ready_animation)
                  }.getOrNull()
                  if (afd != null) {
                    afd.use {
                      setDataSource(it.fileDescriptor, it.startOffset, it.length)
                    }
                  } else {
                    val uri = Uri.parse(
                      "android.resource://${ctx.packageName}/${R.raw.account_ready_animation}"
                    )
                    setDataSource(ctx, uri)
                  }
                  setSurface(surface)
                  isLooping = false
                  setVolume(0f, 0f)
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
                    player.setVolume(0f, 0f)
                    player.start()
                  }
                  setOnCompletionListener {
                    isAnimationComplete = true
                    showActions = true
                  }
                  setOnErrorListener { _, _, _ ->
                    isAnimationComplete = true
                    showActions = true
                    true
                  }
                  prepareAsync()
                }
                mediaPlayerHolder.value = mp
              } catch (_: Exception) {
                releasePlayerAndSurface()
                isAnimationComplete = true
                showActions = true
              }
            }

            override fun onSurfaceTextureSizeChanged(
              surfaceTexture: SurfaceTexture,
              width: Int,
              height: Int
            ) {
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
        .testTag("all_set_video_player")
    )

    // Subtle top-right replay control once the full sequence finishes
    AnimatedVisibility(
      visible = isAnimationComplete,
      enter = fadeIn(tween(300)),
      modifier = Modifier
        .align(Alignment.TopEnd)
        .statusBarsPadding()
        .padding(top = 12.dp, end = 16.dp)
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(NumoDS.CardSurfaceLight.copy(alpha = 0.85f))
          .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
          .clickable(role = Role.Button) { replayAnimation() }
          .testTag("btn_replay_account_ready"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = stringResource(R.string.all_set_replay_animation),
          tint = NumoDS.HeroTealMid,
          modifier = Modifier.size(20.dp)
        )
      }
    }

    // Bottom CTA controls revealed smoothly once the card lands
    AnimatedVisibility(
      visible = showActions,
      enter = fadeIn(tween(420)) + slideInVertically(tween(420)) { it / 3 },
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color.Transparent,
                NumoDS.PeachBgMid.copy(alpha = 0.72f),
                NumoDS.PeachBgBottom.copy(alpha = 0.94f)
              )
            )
          )
          .navigationBarsPadding()
          .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
      ) {
        NumoPrimaryButton(
          text = stringResource(R.string.all_set_go_to_dashboard),
          onClick = onGoToHome,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_go_to_dashboard"),
          height = 54.dp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .clickable(role = Role.Button) { onExploreFeatures() }
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .testTag("btn_explore_features"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = stringResource(R.string.all_set_explore_features),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.HeroTealMid
          )
        }
      }
    }
  }
}
