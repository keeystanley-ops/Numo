package com.example.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

/**
 * Reusable press animation modifier giving interactive elements
 * a tactile, springy feedback (scale to 0.96f and gentle alpha) without layout shifts.
 */
@Composable
fun Modifier.bouncyClickable(
  enabled: Boolean = true,
  pressedScale: Float = 0.96f,
  onClick: () -> Unit
): Modifier {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed && enabled) pressedScale else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "bouncyScale"
  )

  val alpha by animateFloatAsState(
    targetValue = if (isPressed && enabled) 0.92f else 1f,
    animationSpec = tween(durationMillis = 120),
    label = "bouncyAlpha"
  )

  return this
    .graphicsLayer {
      scaleX = scale
      scaleY = scale
      this.alpha = alpha
    }
    .clickable(
      interactionSource = interactionSource,
      indication = null,
      enabled = enabled,
      onClick = onClick
    )
}

/**
 * Animated financial counter that smoothly counts up to the target number
 * with tabular formatting and commas.
 */
@Composable
fun animateIntCount(
  targetValue: Int,
  durationMillis: Int = 850,
  durationMs: Int = durationMillis
): Int {
  var previousValue by remember { mutableIntStateOf(0) }
  val animatable = remember { Animatable(0f) }

  LaunchedEffect(targetValue) {
    animatable.animateTo(
      targetValue = targetValue.toFloat(),
      animationSpec = tween(
        durationMillis = durationMs,
        easing = FastOutSlowInEasing
      )
    )
    previousValue = targetValue
  }

  return animatable.value.toInt()
}

/**
 * Formats an integer amount with commas (e.g. 120000 -> "120,000")
 */
fun formatAmount(amount: Int): String {
  return NumberFormat.getNumberInstance(Locale.US).format(amount)
}

/**
 * Animated progress value from 0f to target with custom delay.
 */
@Composable
fun animateProgress(
  targetProgress: Float,
  delayMillis: Int = 100,
  durationMillis: Int = 900,
  durationMs: Int = durationMillis
): Float {
  val animatable = remember { Animatable(0f) }

  LaunchedEffect(targetProgress) {
    kotlinx.coroutines.delay(delayMillis.toLong())
    animatable.animateTo(
      targetValue = targetProgress,
      animationSpec = tween(
        durationMillis = durationMs,
        easing = FastOutSlowInEasing
      )
    )
  }

  return animatable.value
}

/**
 * Staggered entrance modifier that fades in and translates upwards slightly by index.
 */
@Composable
fun Modifier.staggeredEntrance(
  index: Int,
  baseDelayMs: Int = 40,
  stepDelayMs: Int = 50,
  durationMs: Int = 450,
  slideDistanceDp: Float = 16f
): Modifier {
  val animatable = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    kotlinx.coroutines.delay((baseDelayMs + index * stepDelayMs).toLong())
    animatable.animateTo(
      targetValue = 1f,
      animationSpec = tween(
        durationMillis = durationMs,
        easing = LinearOutSlowInEasing
      )
    )
  }

  val progress = animatable.value
  return this.graphicsLayer {
    alpha = progress
    translationY = (1f - progress) * slideDistanceDp * density
  }
}

/**
 * Staggered entrance modifier overload using absolute delay in ms.
 */
@Composable
fun Modifier.staggeredEntrance(
  delayMs: Int = 40,
  durationMs: Int = 450,
  slideDistanceDp: Float = 16f
): Modifier {
  val animatable = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    kotlinx.coroutines.delay(delayMs.toLong())
    animatable.animateTo(
      targetValue = 1f,
      animationSpec = tween(
        durationMillis = durationMs,
        easing = LinearOutSlowInEasing
      )
    )
  }

  val progress = animatable.value
  return this.graphicsLayer {
    alpha = progress
    translationY = (1f - progress) * slideDistanceDp * density
  }
}

/**
 * Sleek 2dp scroll progress indicator at top of page using raw Float progress.
 */
@Composable
fun TopScrollProgressBar(
  scrollProgress: Float,
  modifier: Modifier = Modifier,
  color: Color = Color(0xFF02844E)
) {
  if (scrollProgress > 0.01f) {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .height(2.5.dp)
        .background(Color.Transparent)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(fraction = scrollProgress.coerceIn(0f, 1f))
          .height(2.5.dp)
          .background(
            brush = Brush.horizontalGradient(
              colors = listOf(
                Color(0xFF00C884),
                Color(0xFF02844E),
                Color(0xFF015B35)
              )
            )
          )
      )
    }
  }
}

/**
 * Top scroll progress indicator overload accepting a Compose ScrollState directly.
 */
@Composable
fun TopScrollProgressBar(
  scrollState: ScrollState,
  modifier: Modifier = Modifier,
  color: Color = Color(0xFF02844E)
) {
  val progress by remember {
    derivedStateOf {
      if (scrollState.maxValue > 0) {
        scrollState.value.toFloat() / scrollState.maxValue.toFloat()
      } else {
        0f
      }
    }
  }

  TopScrollProgressBar(
    scrollProgress = progress,
    modifier = modifier,
    color = color
  )
}
