package com.example.ui

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.Numo3DLogoEmblem
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoFloatingDecorations
import com.example.ui.components.NumoScreenBackground
import kotlinx.coroutines.launch

/**
 * SCREEN 1: Numo Animated Splash Screen
 *
 * Progressively reveals the exact Numo 3D "N" emblem without modifying its
 * geometry, proportions, gradients, or 3D shading, then keeps the completed
 * logo, brand name, and tagline visible until the user swipes LEFT (or taps
 * the indicator pill) to advance to the Welcome screen.
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

  val isReducedMotion = remember(context) {
    try {
      Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
      ) == 0f
    } catch (_: Exception) {
      false
    }
  }

  var isFormationComplete by remember { mutableStateOf(skipAnimation) }
  var hasNavigated by remember { mutableStateOf(false) }

  fun navigateToWelcomeOnce() {
    if (isFormationComplete && !hasNavigated) {
      hasNavigated = true
      onSplashAnimationFinished()
      onGetStarted()
    }
  }

  val ambientGlow = remember { Animatable(0f) }
  val logoReveal = remember { Animatable(if (skipAnimation) 1f else 0f) }
  val logoAlpha = remember { Animatable(if (skipAnimation || !isReducedMotion) 1f else 0f) }
  val lightSweep = remember { Animatable(-0.15f) }
  val brandAlpha = remember { Animatable(if (skipAnimation) 1f else 0f) }
  val brandOffsetY = remember { Animatable(if (skipAnimation) 0f else with(density) { 12.dp.toPx() }) }
  val taglineAlpha = remember { Animatable(if (skipAnimation) 1f else 0f) }
  val taglineOffsetY = remember { Animatable(if (skipAnimation) 0f else with(density) { 8.dp.toPx() }) }

  LaunchedEffect(Unit) {
    if (skipAnimation) {
      isFormationComplete = true
      return@LaunchedEffect
    }

    if (isReducedMotion) {
      // Accessibility: reveal the complete original N + text with a short fade
      logoReveal.snapTo(1f)
      launch { logoAlpha.animateTo(1f, tween(250, easing = FastOutSlowInEasing)) }
      launch { brandAlpha.animateTo(1f, tween(250, easing = FastOutSlowInEasing)) }
      launch { brandOffsetY.animateTo(0f, tween(250, easing = FastOutSlowInEasing)) }
      launch { taglineAlpha.animateTo(1f, tween(250, easing = FastOutSlowInEasing)) }
      taglineOffsetY.animateTo(0f, tween(250, easing = FastOutSlowInEasing))
      isFormationComplete = true
      onSplashAnimationFinished()
      return@LaunchedEffect
    }

    val formationEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

    // Phase 1: Initial ambient glow (300 ms) while N remains invisible
    ambientGlow.animateTo(1f, tween(durationMillis = 300, easing = FastOutSlowInEasing))

    // Phase 2: N logo formation (1,400 ms) — progressive mask reveal along N silhouette
    logoReveal.animateTo(1f, tween(durationMillis = 1400, easing = formationEasing))

    // Phase 3: Subtle turquoise/emerald light sweep across completed N (350 ms)
    launch {
      ambientGlow.animateTo(0.45f, tween(durationMillis = 350, easing = FastOutSlowInEasing))
    }
    lightSweep.animateTo(1.15f, tween(durationMillis = 350, easing = FastOutSlowInEasing))

    // Phase 4: Brand name reveal (400 ms) + Tagline reveal (250 ms)
    launch {
      brandAlpha.animateTo(1f, tween(durationMillis = 400, easing = FastOutSlowInEasing))
    }
    brandOffsetY.animateTo(0f, tween(durationMillis = 400, easing = FastOutSlowInEasing))

    launch {
      taglineAlpha.animateTo(1f, tween(durationMillis = 250, easing = FastOutSlowInEasing))
    }
    taglineOffsetY.animateTo(0f, tween(durationMillis = 250, easing = FastOutSlowInEasing))

    // Phase 5: Final settling (300 ms) into static resting logo, then unlock left-swipe
    ambientGlow.animateTo(0f, tween(durationMillis = 300, easing = FastOutSlowInEasing))
    isFormationComplete = true
    onSplashAnimationFinished()
  }

  NumoScreenBackground(
    modifier = Modifier.pointerInput(isFormationComplete) {
      if (!isFormationComplete) return@pointerInput
      var cumulativeDragX = 0f
      detectHorizontalDragGestures(
        onDragStart = { cumulativeDragX = 0f },
        onDragEnd = {
          if (cumulativeDragX <= -swipeThresholdPx) {
            navigateToWelcomeOnce()
          }
          cumulativeDragX = 0f
        },
        onDragCancel = { cumulativeDragX = 0f },
        onHorizontalDrag = { _, dragAmount ->
          cumulativeDragX += dragAmount
          if (cumulativeDragX <= -swipeThresholdPx) {
            navigateToWelcomeOnce()
          }
        }
      )
    }
  ) {
    NumoFloatingDecorations(variant = 0)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Spacer(modifier = Modifier.height(64.dp))

      // Centered Logo + Brand Name + Tagline
      Column(
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Numo3DLogoEmblem(
          size = 104.dp,
          revealProgress = logoReveal.value,
          ambientGlowAlpha = ambientGlow.value,
          lightSweepProgress = lightSweep.value,
          logoAlpha = logoAlpha.value,
          modifier = Modifier.testTag("welcome_hero_illustration")
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
          text = "Numo",
          fontSize = 40.sp,
          fontWeight = FontWeight.ExtraBold,
          fontFamily = FontFamily.SansSerif,
          color = NumoDS.TextPrimary,
          letterSpacing = (-0.8).sp,
          modifier = Modifier
            .graphicsLayer {
              alpha = brandAlpha.value
              translationY = brandOffsetY.value
            }
            .testTag("welcome_brand_title")
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Plan • Save • Grow",
          fontSize = 15.sp,
          fontWeight = FontWeight.Medium,
          color = NumoDS.HeroTealMid,
          modifier = Modifier.graphicsLayer {
            alpha = taglineAlpha.value
            translationY = taglineOffsetY.value
          }
        )

        Spacer(modifier = Modifier.height(38.dp))

        Text(
          text = "Take Control\nof Your Financial Future",
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold,
          color = NumoDS.TextPrimary,
          textAlign = TextAlign.Center,
          lineHeight = 22.sp,
          modifier = Modifier.graphicsLayer {
            alpha = taglineAlpha.value
            translationY = taglineOffsetY.value
          }
        )
      }

      // Bottom 3-dot indicator pill
      Box(
        modifier = Modifier
          .padding(bottom = 32.dp)
          .graphicsLayer {
            alpha = taglineAlpha.value
            translationY = taglineOffsetY.value
          }
          .shadow(8.dp, RoundedCornerShape(50), spotColor = Color(0x28FFFFFF))
          .clip(RoundedCornerShape(50))
          .background(Color.White.copy(alpha = 0.82f))
          .clickable(enabled = isFormationComplete) {
            navigateToWelcomeOnce()
          }
          .padding(horizontal = 16.dp, vertical = 10.dp)
          .testTag("welcome_get_started_button")
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(Color(0xFF9CA3AF))
          )
          Box(
            modifier = Modifier
              .width(12.dp)
              .height(8.dp)
              .clip(CircleShape)
              .background(NumoDS.Turquoise)
          )
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(NumoDS.Turquoise.copy(alpha = 0.45f))
          )
        }
      }
    }
  }
}
