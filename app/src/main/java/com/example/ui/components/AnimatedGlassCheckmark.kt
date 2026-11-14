package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated Glassmorphism Checkmark:
 * Combines authentic 3D frosted glass styling with multi-layer box shadows
 * and a 3-phase fluid animation:
 *
 * Phase 1: Soft mint glass ring sweep around the emblem
 * Phase 2: The 3D frosted glass pebble pop-bursts with bouncy spring physics,
 *          accompanied by 8 glowing radiant energy burst rays
 * Phase 3: The glowing emerald checkmark traces smoothly from left to bottom vertex to right tip
 * Phase 4: Gentle celebratory glow pulse
 */
@Composable
fun AnimatedGlassCheckmark(
  modifier: Modifier = Modifier,
  size: Dp = 120.dp,
  onAnimationFinished: () -> Unit = {}
) {
  // Phase 1: Ring sweep progress (0f -> 1f)
  val ringProgress = remember { Animatable(0f) }

  // Phase 2: Glass pebble scale (0f -> 1.12f -> 1f)
  val pebbleScale = remember { Animatable(0f) }
  val rayProgress = remember { Animatable(0f) }
  val rayAlpha = remember { Animatable(0f) }

  // Phase 3: Checkmark trace progress (0f -> 1f)
  val checkProgress = remember { Animatable(0f) }

  // Phase 4: Gentle continuous glow pulse
  val infiniteTransition = rememberInfiniteTransition(label = "glass_glow")
  val glowPulse by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow_pulse"
  )

  LaunchedEffect(Unit) {
    // 1. Initial outer ring sweep (0 to 550ms)
    ringProgress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 520, easing = LinearEasing)
    )

    // 2. Glass pebble pops outward with bouncy spring
    rayAlpha.snapTo(1f)
    pebbleScale.animateTo(
      targetValue = 1f,
      animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
      )
    )

    // Radiant burst rays expand outward
    rayProgress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
    )

    // 3. Glowing checkmark traces from left to vertex to right tip
    checkProgress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
    )

    // Comfortable pause before calling completion
    delay(900)
    onAnimationFinished()
  }

  Box(
    modifier = modifier.size(size * 1.3f),
    contentAlignment = Alignment.Center
  ) {
    // 1. Canvas for Sweep Ring and Burst Rays
    Canvas(modifier = Modifier.size(size * 1.25f)) {
      val d = this.size.minDimension
      val cx = d / 2f
      val cy = d / 2f

      // PHASE 1: Frosted Mint Ring Sweep
      if (pebbleScale.value < 0.7f) {
        val ringStroke = d * 0.035f
        val ringRadius = (d * 0.82f - ringStroke) / 2f
        val ringOpacity = (1f - (pebbleScale.value / 0.7f)).coerceIn(0f, 1f)

        drawArc(
          color = Color(0xFF10B981).copy(alpha = 0.35f * ringOpacity),
          startAngle = -90f,
          sweepAngle = 360f * ringProgress.value,
          useCenter = false,
          topLeft = Offset(cx - ringRadius, cy - ringRadius),
          size = Size(ringRadius * 2, ringRadius * 2),
          style = Stroke(width = ringStroke, cap = StrokeCap.Round)
        )
      }

      // PHASE 2: Radiant Energy Burst Rays (8 rays shooting outward)
      if (pebbleScale.value > 0.15f && rayAlpha.value > 0.05f) {
        val rayColor = Color(0xFF10B981)
        val numRays = 8
        val baseAngleStep = 360.0 / numRays

        for (i in 0 until numRays) {
          val angleDeg = (i * baseAngleStep) + 22.5
          val angleRad = Math.toRadians(angleDeg)
          val cosA = cos(angleRad).toFloat()
          val sinA = sin(angleRad).toFloat()

          val startDist = (d * 0.40f) + (d * 0.06f * rayProgress.value)
          val rayLen = (d * 0.05f) * (1f - rayProgress.value * 0.75f)
          val endDist = startDist + rayLen

          val p1 = Offset(cx + startDist * cosA, cy + startDist * sinA)
          val p2 = Offset(cx + endDist * cosA, cy + endDist * sinA)

          drawLine(
            color = rayColor.copy(alpha = 0.75f * (1f - rayProgress.value * 0.5f)),
            start = p1,
            end = p2,
            strokeWidth = d * 0.018f,
            cap = StrokeCap.Round
          )
        }
      }
    }

    // 2. The Glassmorphic Pebble Emblem with Multi-Layer Box Shadows
    Box(
      modifier = Modifier
        .size(size)
        .scale(pebbleScale.value)
        // Primary Drop Shadow (Deep diffuse green ambient)
        .shadow(
          elevation = 18.dp,
          shape = RoundedCornerShape(28.dp),
          spotColor = Color(0x351E5631),
          ambientColor = Color(0x2510B981)
        )
        // Secondary Glow Shadow (Vibrant mint halo)
        .shadow(
          elevation = 8.dp,
          shape = RoundedCornerShape(28.dp),
          spotColor = Color(0x4010B981),
          ambientColor = Color(0x2034D399)
        )
        .clip(RoundedCornerShape(28.dp))
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xE6FFFFFF),
              Color(0xD9E6FAF3),
              Color(0xE6D0F8EC)
            )
          )
        )
        .border(
          width = 1.5.dp,
          brush = Brush.verticalGradient(
            colors = listOf(
              Color.White.copy(alpha = 0.95f),
              Color(0x80A7F3D0),
              Color(0x40FFFFFF)
            )
          ),
          shape = RoundedCornerShape(28.dp)
        ),
      contentAlignment = Alignment.Center
    ) {
      // 3D Glass Checkmark Asset Base
      Image(
        painter = painterResource(id = R.drawable.ic_numo_glass_checkmark),
        contentDescription = "Glass Checkmark",
        modifier = Modifier
          .size(size)
          .scale(glowPulse),
        contentScale = ContentScale.Fit
      )

      // PHASE 3: Dynamically Tracing Emerald Neon Checkmark Overlay
      if (checkProgress.value > 0.02f) {
        Canvas(modifier = Modifier.size(size)) {
          val w = this.size.width
          val h = this.size.height

          // Precision proportional checkmark path aligned to the glass emblem:
          // Start: (0.33 * w, 0.51 * h)
          // Vertex: (0.46 * w, 0.65 * h)
          // End: (0.69 * w, 0.38 * h)
          val pStart = Offset(w * 0.33f, h * 0.51f)
          val pVertex = Offset(w * 0.46f, h * 0.65f)
          val pEnd = Offset(w * 0.69f, h * 0.38f)

          val checkPath = Path().apply {
            moveTo(pStart.x, pStart.y)
            lineTo(pVertex.x, pVertex.y)
            lineTo(pEnd.x, pEnd.y)
          }

          val pathMeasure = PathMeasure()
          pathMeasure.setPath(checkPath, false)
          val pathLength = pathMeasure.length
          val stopDistance = pathLength * checkProgress.value

          val animatedPath = Path()
          pathMeasure.getSegment(0f, stopDistance, animatedPath, true)

          // Glowing Outer Stroke
          drawPath(
            path = animatedPath,
            color = Color(0x6010B981),
            style = Stroke(
              width = w * 0.12f,
              cap = StrokeCap.Round,
              join = StrokeJoin.Round
            )
          )

          // Core Radiant Gradient Emerald Stroke
          drawPath(
            path = animatedPath,
            brush = Brush.linearGradient(
              colors = listOf(
                Color(0xFF34D399),
                Color(0xFF10B981),
                Color(0xFF059669)
              ),
              start = pStart,
              end = pEnd
            ),
            style = Stroke(
              width = w * 0.065f,
              cap = StrokeCap.Round,
              join = StrokeJoin.Round
            )
          )

          // Inner Specular White Core Line
          drawPath(
            path = animatedPath,
            color = Color.White.copy(alpha = 0.85f),
            style = Stroke(
              width = w * 0.025f,
              cap = StrokeCap.Round,
              join = StrokeJoin.Round
            )
          )
        }
      }
    }
  }
}
