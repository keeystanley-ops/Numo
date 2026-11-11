package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated Checkmark reproducing the EXACT visual sequence from success_d67bcc21.mp4:
 *
 * Phase 1 (0 to ~650ms):
 *   - Clean grey ring track (#E5E5E5) sweeps/draws smoothly around the perimeter.
 *
 * Phase 2 (~650ms to 950ms):
 *   - The green scalloped badge pop-bursts outward with spring/elastic bounce (#00C884).
 *   - 8 radiant energy burst rays/dots shoot outward and shrink into small accent points.
 *
 * Phase 3 (~950ms to 1350ms):
 *   - The clean white checkmark inside the green scalloped badge traces from left
 *     down to the bottom vertex, then springs up to the right tip.
 */
@Composable
fun AnimatedSuccessCheckmark(
  modifier: Modifier = Modifier,
  size: Dp = 130.dp,
  badgeColor: Color = Color(0xFF00C884),
  trackColor: Color = Color(0xFFE5E5E5),
  onAnimationFinished: () -> Unit = {}
) {
  // Phase 1: Grey ring progress (0f -> 1f)
  val ringProgress = remember { Animatable(0f) }

  // Phase 2: Green rosette badge scale (0f -> 1.15f -> 1f) and ray expansion
  val badgeScale = remember { Animatable(0f) }
  val rayProgress = remember { Animatable(0f) }
  val rayAlpha = remember { Animatable(0f) }

  // Phase 3: White checkmark draw progress (0f -> 1f)
  val checkProgress = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    // 1. Initial grey ring sweep (0 -> 1)
    ringProgress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 620, easing = LinearEasing)
    )

    // 2. Rosette badge pops outward with bounce
    rayAlpha.snapTo(1f)
    badgeScale.animateTo(
      targetValue = 1f,
      animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
      )
    )

    // Ray burst expands and settles into accent dots
    rayProgress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
    )

    // 3. Checkmark traces inside the badge
    checkProgress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
    )

    // Comfortable pause to let the user perceive the completed success badge
    delay(650)
    onAnimationFinished()
  }

  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(size)) {
      val d = this.size.minDimension
      val cx = d / 2f
      val cy = d / 2f

      // 1. DRAW GREY RING (Phase 1, fades as badge bursts)
      if (badgeScale.value < 0.6f) {
        val ringStroke = d * 0.045f
        val ringRadius = (d * 0.76f - ringStroke) / 2f
        val ringOpacity = (1f - (badgeScale.value / 0.6f)).coerceIn(0f, 1f)

        drawArc(
          color = trackColor.copy(alpha = ringOpacity),
          startAngle = -90f,
          sweepAngle = 360f * ringProgress.value,
          useCenter = false,
          topLeft = Offset(cx - ringRadius, cy - ringRadius),
          size = Size(ringRadius * 2, ringRadius * 2),
          style = Stroke(width = ringStroke, cap = StrokeCap.Round)
        )
      }

      // 2. DRAW BURST RAYS (Phase 2, 8 rays radiating outward from center)
      if (badgeScale.value > 0.1f) {
        val rayColor = Color(0xFF34D399)
        val numRays = 8
        val baseAngleStep = (360.0 / numRays)

        for (i in 0 until numRays) {
          val angleDeg = (i * baseAngleStep) + 22.5
          val angleRad = Math.toRadians(angleDeg)
          val cosA = cos(angleRad).toFloat()
          val sinA = sin(angleRad).toFloat()

          // As rayProgress goes from 0 to 1, ray travels from 0.42*d to 0.47*d and shrinks to dot
          val startDist = (d * 0.38f) + (d * 0.05f * rayProgress.value)
          val rayLen = (d * 0.045f) * (1f - rayProgress.value * 0.7f)
          val endDist = startDist + rayLen

          val p1 = Offset(cx + startDist * cosA, cy + startDist * sinA)
          val p2 = Offset(cx + endDist * cosA, cy + endDist * sinA)

          drawLine(
            color = rayColor,
            start = p1,
            end = p2,
            strokeWidth = d * 0.016f,
            cap = StrokeCap.Round
          )
        }
      }

      // 3. DRAW SCALLOPED GREEN BADGE / ROSETTE (Exact 14-point wavy emblem)
      if (badgeScale.value > 0.01f) {
        val badgePath = Path()
        val numScallops = 14
        val totalSteps = numScallops * 2

        // Outer radius ~ 0.36*d, Inner valley radius ~ 0.31*d
        val rOuter = d * 0.355f * badgeScale.value
        val rInner = d * 0.305f * badgeScale.value

        for (step in 0 until totalSteps) {
          val angleRad = Math.toRadians((step * (360.0 / totalSteps)) - 90.0)
          val r = if (step % 2 == 0) rOuter else rInner
          val x = cx + r * cos(angleRad).toFloat()
          val y = cy + r * sin(angleRad).toFloat()

          if (step == 0) {
            badgePath.moveTo(x, y)
          } else {
            // Smooth curve to each scallop point
            val prevAngleRad = Math.toRadians(((step - 1) * (360.0 / totalSteps)) - 90.0)
            val prevR = if ((step - 1) % 2 == 0) rOuter else rInner
            val midAngleRad = (angleRad + prevAngleRad) / 2.0
            val midR = (r + prevR) / 2.0f * 1.02f
            val cxCtrl = cx + (midR * cos(midAngleRad)).toFloat()
            val cyCtrl = cy + (midR * sin(midAngleRad)).toFloat()

            badgePath.quadraticTo(cxCtrl, cyCtrl, x, y)
          }
        }
        badgePath.close()

        drawPath(
          path = badgePath,
          color = badgeColor,
          style = Fill
        )
      }

      // 4. DRAW WHITE CHECKMARK INSIDE THE BADGE
      if (checkProgress.value > 0.01f && badgeScale.value > 0.5f) {
        // Proportional checkmark points extracted from milestone_4.png:
        // Left Tip = (0.360 * d, 0.507 * d)
        // Bottom Vertex = (0.447 * d, 0.607 * d)
        // Right Tip = (0.620 * d, 0.427 * d)
        val pStart = Offset(d * 0.360f, d * 0.507f)
        val pVertex = Offset(d * 0.447f, d * 0.607f)
        val pEnd = Offset(d * 0.620f, d * 0.427f)

        val fullCheckPath = Path().apply {
          moveTo(pStart.x, pStart.y)
          lineTo(pVertex.x, pVertex.y)
          lineTo(pEnd.x, pEnd.y)
        }

        val pathMeasure = PathMeasure()
        pathMeasure.setPath(fullCheckPath, false)
        val pathLength = pathMeasure.length
        val stopDistance = pathLength * checkProgress.value

        val animatedCheck = Path()
        pathMeasure.getSegment(0f, stopDistance, animatedCheck, true)

        drawPath(
          path = animatedCheck,
          color = Color.White,
          style = Stroke(
            width = d * 0.055f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
          )
        )
      }
    }
  }
}
