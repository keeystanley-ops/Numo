package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun WelcomeScreen(
  onGetStarted: () -> Unit
) {
  val density = LocalDensity.current
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  // Dynamic press tactile response
  val pressScale by animateFloatAsState(
    targetValue = if (isPressed) 0.91f else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMediumLow
    ),
    label = "press_scale"
  )

  // Continuous multi-layer button animations
  val infiniteTransition = rememberInfiniteTransition(label = "button_animation")

  // 1. Gentle breathing scale
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.07f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  // 2. Outward expanding ripple ring (sonar pulse)
  val outerRippleScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.45f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1600, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "outer_ripple_scale"
  )
  val outerRippleAlpha by infiniteTransition.animateFloat(
    initialValue = 0.55f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1600, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "outer_ripple_alpha"
  )

  // 3. Inner ambient glow pulsing
  val innerGlowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 0.8f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "inner_glow_alpha"
  )

  // 4. Arrow forward nudge drift
  val arrowNudgeDp by infiniteTransition.animateFloat(
    initialValue = -1.5f,
    targetValue = 4.5f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "arrow_nudge"
  )

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = Color(0xFFF7FDFB)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFFD2FCEF),
              Color(0xFFE6FAF3),
              Color(0xFFF2FCF8),
              Color(0xFFFFFFFF)
            )
          )
        )
        .statusBarsPadding()
        .navigationBarsPadding()
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(26.dp))

        // 1. Single App Brand Mark (ic_numo_leaf)
        Image(
          painter = painterResource(id = R.drawable.ic_numo_leaf),
          contentDescription = "Numo Brand Logo",
          modifier = Modifier
            .size(width = 72.dp, height = 58.dp)
            .testTag("welcome_brand_logo"),
          contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Single App Title: "Numo"
        Text(
          text = "Numo",
          fontSize = 38.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.SansSerif,
          color = Color(0xFF001A49),
          letterSpacing = (-0.5).sp,
          modifier = Modifier.testTag("welcome_title")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 3. Single Tagline: "Grow Wealth. \n Build Tomorrow."
        Text(
          text = "Grow Wealth.\nBuild Tomorrow.",
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.SansSerif,
          color = Color(0xFF0F2F4A),
          lineHeight = 32.sp,
          textAlign = TextAlign.Center,
          modifier = Modifier.testTag("welcome_tagline")
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 4. 3D Growth Illustration (Coins stacks + Green bars with sprout ONLY - no duplicate text)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.welcome_growth_hero),
            contentDescription = "Growth and wealth creation illustration",
            modifier = Modifier
              .fillMaxWidth(0.94f)
              .testTag("welcome_hero_image"),
            contentScale = ContentScale.Fit
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Centered Animated Glowing Green Button with Arrow
        Box(
          modifier = Modifier
            .padding(bottom = 36.dp),
          contentAlignment = Alignment.Center
        ) {
          // A. Outward expanding ripple ring (sonar glow pulse)
          Box(
            modifier = Modifier
              .size(72.dp)
              .scale(outerRippleScale)
              .clip(RoundedCornerShape(26.dp))
              .background(Color(0xFF00D084).copy(alpha = outerRippleAlpha))
          )

          // B. Ambient pulsing glowing halo
          Box(
            modifier = Modifier
              .size(92.dp)
              .scale(pulseScale)
              .clip(RoundedCornerShape(32.dp))
              .background(
                brush = Brush.radialGradient(
                  colors = listOf(
                    Color(0xFF00E699).copy(alpha = innerGlowAlpha),
                    Color(0xFF029766).copy(alpha = innerGlowAlpha * 0.45f),
                    Color.Transparent
                  )
                )
              )
          )

          // C. Main Interactive Button with 3D elevation, gradient, and animated arrow
          val buttonShape = RoundedCornerShape(24.dp)
          Box(
            modifier = Modifier
              .size(width = 72.dp, height = 72.dp)
              .scale(pulseScale * pressScale)
              .shadow(
                elevation = 16.dp,
                shape = buttonShape,
                ambientColor = Color(0x66027858),
                spotColor = Color(0xAA00D084)
              )
              .clip(buttonShape)
              .background(
                brush = Brush.verticalGradient(
                  colors = listOf(
                    Color(0xFF00D488),
                    Color(0xFF029766),
                    Color(0xFF027858)
                  )
                )
              )
              .clickable(
                interactionSource = interactionSource,
                indication = null
              ) {
                onGetStarted()
              }
              .testTag("welcome_continue_button"),
            contentAlignment = Alignment.Center
          ) {
            val arrowTranslationX = with(density) { arrowNudgeDp.dp.toPx() }

            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Continue to Welcome Back",
              tint = Color.White,
              modifier = Modifier
                .size(32.dp)
                .graphicsLayer {
                  translationX = arrowTranslationX
                }
            )
          }
        }
      }
    }
  }
}
