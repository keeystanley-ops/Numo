package com.example.ui.voice

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.Numo3DLogoEmblem
import com.example.ui.components.NumoDS
import com.example.voice.VoiceOrbState
import kotlin.math.sin

/**
 * Shared "Hey Numo" status pill matching Screens 1 & 3 top bar.
 */
@Composable
fun HeyNumoStatusPill(
  isActive: Boolean = true,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .shadow(8.dp, RoundedCornerShape(50), spotColor = NumoDS.CardShadowWarm)
      .clip(RoundedCornerShape(50))
      .background(
        Brush.verticalGradient(
          colors = listOf(NumoDS.CardSurfaceLight, NumoDS.CardSurface)
        )
      )
      .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(50))
      .padding(horizontal = 16.dp, vertical = 9.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = Icons.Default.GraphicEq,
        contentDescription = null,
        tint = NumoDS.HeroTealMid,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "Hey Numo",
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Bold,
        color = NumoDS.TextPrimary
      )
      Spacer(modifier = Modifier.width(6.dp))
      Box(
        modifier = Modifier
          .size(7.dp)
          .clip(CircleShape)
          .background(if (isActive) Color(0xFF22C55E) else NumoDS.DuneOchre)
      )
    }
  }
}

/**
 * Shared circular neumorphic icon button (Back, Menu, Home, Keyboard, etc.).
 */
@Composable
fun HeyNumoCircleButton(
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  size: Dp = 44.dp,
  iconSize: Dp = 20.dp
) {
  Box(
    modifier = modifier
      .size(size)
      .shadow(8.dp, CircleShape, spotColor = NumoDS.CardShadowWarm)
      .clip(CircleShape)
      .background(
        Brush.verticalGradient(
          colors = listOf(NumoDS.CardSurfaceLight, NumoDS.CardSurface)
        )
      )
      .border(1.2.dp, Color.White.copy(alpha = 0.88f), CircleShape)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = NumoDS.TextPrimary,
      modifier = Modifier.size(iconSize)
    )
  }
}

/**
 * Animated concentric-ring orb wrapping the exact unmodified Numo3DLogoEmblem.
 * Supports IDLE, LISTENING, THINKING, SPEAKING, and ERROR states.
 */
@Composable
fun HeyNumoOrb(
  orbState: VoiceOrbState,
  rmsLevel: Float = 0.2f,
  size: Dp = 210.dp,
  logoSize: Dp = 84.dp,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "hey_numo_orb")

  val breathe by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "orb_breathe"
  )

  val spin by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "orb_spin"
  )

  val speakPulse by infiniteTransition.animateFloat(
    initialValue = 0.94f,
    targetValue = 1.07f,
    animationSpec = infiniteRepeatable(
      animation = tween(380, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "orb_speak"
  )

  val smoothRms by animateFloatAsState(
    targetValue = rmsLevel.coerceIn(0f, 1f),
    animationSpec = tween(100),
    label = "orb_rms"
  )

  val floatY = when (orbState) {
    VoiceOrbState.IDLE -> (breathe - 0.5f) * 8f
    else -> 0f
  }

  val ringScale = when (orbState) {
    VoiceOrbState.IDLE -> 1f + breathe * 0.04f
    VoiceOrbState.LISTENING -> 1f + smoothRms * 0.14f
    VoiceOrbState.THINKING -> 1.02f
    VoiceOrbState.SPEAKING -> speakPulse
    VoiceOrbState.ERROR -> 0.98f
  }

  val glowPrimary = when (orbState) {
    VoiceOrbState.ERROR -> NumoDS.Magenta
    VoiceOrbState.THINKING -> NumoDS.CyanBright
    else -> NumoDS.Turquoise
  }

  Box(
    modifier = modifier
      .size(size)
      .graphicsLayer {
        translationY = floatY
      },
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(size)) {
      val c = center
      val maxR = this.size.minDimension / 2f

      // Outer ambient halo
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            glowPrimary.copy(alpha = 0.28f),
            NumoDS.Cyan.copy(alpha = 0.10f),
            Color.Transparent
          ),
          center = c,
          radius = maxR * ringScale
        ),
        radius = maxR * ringScale,
        center = c
      )

      // Outer frosted ring
      drawCircle(
        color = Color.White.copy(alpha = 0.55f),
        radius = maxR * 0.78f * ringScale,
        center = c,
        style = Stroke(width = 4.dp.toPx())
      )

      // Inner claymorphic disc
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(Color(0xFFFFFBF7), Color(0xFFF6EBE0)),
          center = c,
          radius = maxR * 0.64f
        ),
        radius = maxR * 0.64f,
        center = c
      )

      // Rotating arc when THINKING
      if (orbState == VoiceOrbState.THINKING) {
        drawArc(
          brush = Brush.sweepGradient(
            colors = listOf(Color.Transparent, NumoDS.CyanBright, NumoDS.Turquoise, Color.Transparent)
          ),
          startAngle = spin,
          sweepAngle = 220f,
          useCenter = false,
          topLeft = Offset(c.x - maxR * 0.72f, c.y - maxR * 0.72f),
          size = Size(maxR * 1.44f, maxR * 1.44f),
          style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
      }
    }

    // Exact unmodified Numo 3D N logo in center
    Numo3DLogoEmblem(size = logoSize)
  }
}

/**
 * Live audio waveform bars reactive to microphone RMS / voice state.
 */
@Composable
fun HeyNumoWaveform(
  orbState: VoiceOrbState,
  rmsLevel: Float,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "waveform")
  val phase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = (2f * Math.PI).toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "wave_phase"
  )

  Canvas(
    modifier = modifier
      .width(180.dp)
      .height(34.dp)
  ) {
    val barCount = 15
    val barWidth = 4.dp.toPx()
    val gap = (size.width - barCount * barWidth) / (barCount - 1)
    val midY = size.height / 2f

    for (i in 0 until barCount) {
      val envelope = sin((i.toFloat() / (barCount - 1)) * Math.PI).toFloat().coerceAtLeast(0.25f)
      val amp = when (orbState) {
        VoiceOrbState.LISTENING -> (0.25f + rmsLevel * 0.75f) *
          (0.55f + 0.45f * kotlin.math.abs(sin(phase + i * 0.65f)))
        VoiceOrbState.SPEAKING -> 0.35f + 0.55f * kotlin.math.abs(sin(phase * 1.4f + i * 0.55f))
        VoiceOrbState.THINKING -> 0.25f + 0.25f * kotlin.math.abs(sin(phase + i * 0.4f))
        VoiceOrbState.ERROR -> 0.16f
        VoiceOrbState.IDLE -> 0.14f
      }
      val barH = (size.height * amp * envelope).coerceIn(5.dp.toPx(), size.height)
      val x = i * (barWidth + gap)
      val barColor = if (orbState == VoiceOrbState.ERROR) {
        NumoDS.Magenta.copy(alpha = 0.7f)
      } else {
        NumoDS.Turquoise.copy(alpha = 0.45f + 0.55f * envelope)
      }

      drawRoundRect(
        color = barColor,
        topLeft = Offset(x, midY - barH / 2f),
        size = Size(barWidth, barH),
        cornerRadius = CornerRadius(barWidth / 2f)
      )
    }
  }
}
