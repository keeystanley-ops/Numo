package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared Neumorphic Back Button matching Screens 3, 4, 5
 */
@Composable
fun NumoBackCircleButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .size(38.dp)
      .shadow(8.dp, CircleShape, spotColor = NumoDS.CardShadowWarm)
      .clip(CircleShape)
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(NumoDS.CardSurfaceLight, NumoDS.CardSurface)
        )
      )
      .border(1.2.dp, Color.White.copy(alpha = 0.85f), CircleShape)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = Icons.AutoMirrored.Filled.ArrowBack,
      contentDescription = "Back",
      tint = NumoDS.TextPrimary,
      modifier = Modifier.size(18.dp)
    )
  }
}

/**
 * Shared Radio Selection Indicator matching Screens 4 & 5
 */
@Composable
fun NumoRadioIndicator(
  selected: Boolean,
  modifier: Modifier = Modifier
) {
  if (selected) {
    Box(
      modifier = modifier
        .size(22.dp)
        .shadow(4.dp, CircleShape, spotColor = NumoDS.Turquoise.copy(alpha = 0.5f))
        .clip(CircleShape)
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(NumoDS.CyanBright, NumoDS.HeroTealMid)
          )
        ),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(Color.White)
      )
    }
  } else {
    Box(
      modifier = modifier
        .size(22.dp)
        .clip(CircleShape)
        .border(1.8.dp, Color(0xFFD8C2B0), CircleShape)
    )
  }
}

/**
 * Floating 3D Spheres & Organic Waves matching the reference onboarding background
 */
@Composable
fun NumoFloatingDecorations(
  variant: Int = 0,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height

    fun draw3DSphere(center: Offset, radius: Float, light: Color, mid: Color, dark: Color) {
      // Soft ambient shadow
      drawCircle(
        color = dark.copy(alpha = 0.22f),
        radius = radius * 1.08f,
        center = Offset(center.x + radius * 0.14f, center.y + radius * 0.22f)
      )
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(light, mid, dark),
          center = Offset(center.x - radius * 0.32f, center.y - radius * 0.32f),
          radius = radius * 1.35f
        ),
        radius = radius,
        center = center
      )
    }

    when (variant) {
      0 -> {
        // Splash screen rich organic waves + spheres
        // Top-left cyan wave
        val topWave = Path().apply {
          moveTo(0f, 0f)
          lineTo(w * 0.42f, 0f)
          cubicTo(w * 0.36f, h * 0.12f, w * 0.14f, h * 0.20f, 0f, h * 0.22f)
          close()
        }
        drawPath(
          topWave,
          brush = Brush.linearGradient(
            colors = listOf(Color(0xFF56F6EC), NumoDS.HeroTealMid),
            start = Offset(0f, 0f),
            end = Offset(w * 0.3f, h * 0.22f)
          )
        )
        draw3DSphere(Offset(w * 0.36f, h * 0.08f), w * 0.032f, Color.White, Color(0xFFF3E7DD), Color(0xFFCBB8A8))
        draw3DSphere(Offset(w * 0.94f, h * 0.08f), w * 0.065f, Color(0xFF9E6B4F), NumoDS.DeepUmber, Color(0xFF2B1810))
        draw3DSphere(Offset(w * 0.98f, h * 0.23f), w * 0.11f, Color(0xFFFF78C4), NumoDS.Magenta, Color(0xFF9E0B57))
        draw3DSphere(Offset(w * 0.91f, h * 0.32f), w * 0.028f, Color.White, Color(0xFFF3E7DD), Color(0xFFCBB8A8))
        draw3DSphere(Offset(w * 0.05f, h * 0.40f), w * 0.085f, Color(0xFFFCD34D), NumoDS.DuneOchre, Color(0xFFB45309))
        draw3DSphere(Offset(w * 0.93f, h * 0.50f), w * 0.065f, Color(0xFF7AF5ED), NumoDS.Cyan, Color(0xFF0A6F62))

        // Bottom organic waves
        val botLeftGold = Path().apply {
          moveTo(0f, h * 0.72f)
          cubicTo(w * 0.28f, h * 0.74f, w * 0.42f, h * 0.88f, w * 0.62f, h)
          lineTo(0f, h)
          close()
        }
        drawPath(
          botLeftGold,
          brush = Brush.linearGradient(colors = listOf(Color(0xFFFCD34D), NumoDS.DuneOchre))
        )

        val botLeftCyan = Path().apply {
          moveTo(0f, h * 0.66f)
          cubicTo(w * 0.20f, h * 0.76f, w * 0.26f, h * 0.90f, w * 0.44f, h)
          lineTo(0f, h)
          close()
        }
        drawPath(
          botLeftCyan,
          brush = Brush.linearGradient(colors = listOf(NumoDS.Cyan, NumoDS.HeroTealDark))
        )

        val botRightMagenta = Path().apply {
          moveTo(w, h * 0.68f)
          cubicTo(w * 0.78f, h * 0.75f, w * 0.72f, h * 0.90f, w * 0.58f, h)
          lineTo(w, h)
          close()
        }
        drawPath(
          botRightMagenta,
          brush = Brush.linearGradient(colors = listOf(NumoDS.Magenta, Color(0xFF830743)))
        )

        draw3DSphere(Offset(w * 0.95f, h * 0.90f), w * 0.09f, Color(0xFF9E6B4F), NumoDS.DeepUmber, Color(0xFF2B1810))
      }

      1 -> {
        // Welcome / Auth & Currency & Connect Account subtle corner spheres
        draw3DSphere(Offset(w * 0.96f, h * 0.06f), w * 0.11f, Color(0xFFFCD34D), NumoDS.DuneOchre, Color(0xFFB45309))
        draw3DSphere(Offset(w * 0.96f, h * 0.36f), w * 0.10f, Color(0xFFFF78C4), NumoDS.Magenta, Color(0xFF9E0B57))
        draw3DSphere(Offset(w * 0.89f, h * 0.14f), w * 0.022f, Color.White, Color(0xFFF3E7DD), Color(0xFFCBB8A8))
      }

      2 -> {
        // Forgot password corner spheres & bottom waves
        draw3DSphere(Offset(w * 0.96f, h * 0.05f), w * 0.10f, Color(0xFFFCD34D), NumoDS.DuneOchre, Color(0xFFB45309))
        draw3DSphere(Offset(w * 0.96f, h * 0.21f), w * 0.07f, Color(0xFF56F6EC), NumoDS.HeroTealMid, NumoDS.HeroTealDark)
        draw3DSphere(Offset(w * 0.08f, h * 0.92f), w * 0.11f, Color(0xFFFF78C4), NumoDS.Magenta, Color(0xFF9E0B57))
        val botCyan = Path().apply {
          moveTo(w, h * 0.82f)
          cubicTo(w * 0.78f, h * 0.84f, w * 0.65f, h * 0.95f, w * 0.52f, h)
          lineTo(w, h)
          close()
        }
        drawPath(
          botCyan,
          brush = Brush.linearGradient(colors = listOf(NumoDS.Cyan, NumoDS.TransportBlue))
        )
      }

      3 -> {
        // Celebration confetti spheres (Account Connected & All Set)
        draw3DSphere(Offset(w * 0.16f, h * 0.16f), w * 0.018f, Color(0xFF7AF5ED), NumoDS.Cyan, NumoDS.HeroTealMid)
        draw3DSphere(Offset(w * 0.22f, h * 0.24f), w * 0.015f, Color(0xFFFCD34D), NumoDS.DuneOchre, Color(0xFFB45309))
        draw3DSphere(Offset(w * 0.78f, h * 0.15f), w * 0.020f, Color(0xFFA855F7), NumoDS.PurpleAccent, Color(0xFF4C1D95))
        draw3DSphere(Offset(w * 0.84f, h * 0.22f), w * 0.022f, Color(0xFFFCD34D), NumoDS.DuneOchre, Color(0xFFB45309))
        draw3DSphere(Offset(w * 0.24f, h * 0.28f), w * 0.024f, Color(0xFFFF78C4), NumoDS.Magenta, Color(0xFF9E0B57))
        draw3DSphere(Offset(w * 0.94f, h * 0.92f), w * 0.11f, Color(0xFF7AF5ED), NumoDS.Cyan, NumoDS.HeroTealDark)
      }
    }
  }
}

/**
 * Screen 1: 3D Claymorphic Numo "N" Logo Emblem
 */
@Composable
fun Numo3DLogoEmblem(
  size: Dp = 92.dp,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier.size(size)) {
    val w = this.size.width
    val h = this.size.height
    val strokeW = w * 0.24f

    // Soft ambient shadow
    drawRoundRect(
      color = Color(0x280A6F62),
      topLeft = Offset(w * 0.06f, h * 0.14f),
      size = Size(w * 0.88f, h * 0.84f),
      cornerRadius = CornerRadius(w * 0.18f)
    )

    // Left vertical pillar
    drawRoundRect(
      brush = Brush.verticalGradient(
        colors = listOf(Color(0xFF7AF5ED), NumoDS.Cyan, NumoDS.Turquoise)
      ),
      topLeft = Offset(w * 0.08f, h * 0.08f),
      size = Size(strokeW, h * 0.84f),
      cornerRadius = CornerRadius(strokeW * 0.45f)
    )

    // Right vertical pillar
    drawRoundRect(
      brush = Brush.verticalGradient(
        colors = listOf(Color(0xFF56F6EC), NumoDS.Turquoise, NumoDS.HeroTealDark)
      ),
      topLeft = Offset(w * 0.68f, h * 0.08f),
      size = Size(strokeW, h * 0.84f),
      cornerRadius = CornerRadius(strokeW * 0.45f)
    )

    // Diagonal folded ribbon
    val diagPath = Path().apply {
      moveTo(w * 0.12f, h * 0.14f)
      lineTo(w * 0.30f, h * 0.10f)
      lineTo(w * 0.88f, h * 0.84f)
      lineTo(w * 0.70f, h * 0.88f)
      close()
    }
    drawPath(
      path = diagPath,
      brush = Brush.linearGradient(
        colors = listOf(Color(0xFF56F6EC), NumoDS.HeroTealMid, NumoDS.HeroTealDark),
        start = Offset(w * 0.15f, h * 0.12f),
        end = Offset(w * 0.85f, h * 0.86f)
      )
    )
  }
}

/**
 * Screen 2: 3D Claymorphic Teal Wallet & Gold Coins Illustration
 */
@Composable
fun Numo3DWalletIllustration(
  size: Dp = 112.dp,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier.size(size)) {
    val w = this.size.width
    val h = this.size.height

    // Gold coins peeking from top
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(Color(0xFFFDE68A), NumoDS.DuneOchre, Color(0xFFB45309))
      ),
      radius = w * 0.15f,
      center = Offset(w * 0.24f, h * 0.28f)
    )
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(Color(0xFFFDE68A), NumoDS.DuneOchre, Color(0xFFB45309))
      ),
      radius = w * 0.15f,
      center = Offset(w * 0.72f, h * 0.22f)
    )

    // Banknote peeking out
    drawRoundRect(
      brush = Brush.linearGradient(colors = listOf(Color(0xFF7AF5ED), NumoDS.Turquoise)),
      topLeft = Offset(w * 0.40f, h * 0.16f),
      size = Size(w * 0.34f, h * 0.28f),
      cornerRadius = CornerRadius(w * 0.06f)
    )

    // Wallet drop shadow
    drawRoundRect(
      color = Color(0x30033B31),
      topLeft = Offset(w * 0.18f, h * 0.36f),
      size = Size(w * 0.72f, h * 0.56f),
      cornerRadius = CornerRadius(w * 0.14f)
    )

    // Main Wallet Body
    drawRoundRect(
      brush = Brush.linearGradient(
        colors = listOf(Color(0xFF2CE6D8), NumoDS.HeroTealMid, NumoDS.HeroTealDark)
      ),
      topLeft = Offset(w * 0.16f, h * 0.30f),
      size = Size(w * 0.72f, h * 0.56f),
      cornerRadius = CornerRadius(w * 0.14f)
    )

    // Wallet Clasp Flap
    drawRoundRect(
      brush = Brush.horizontalGradient(
        colors = listOf(Color(0xFF56F6EC), NumoDS.Turquoise)
      ),
      topLeft = Offset(w * 0.58f, h * 0.48f),
      size = Size(w * 0.34f, h * 0.20f),
      cornerRadius = CornerRadius(w * 0.10f)
    )

    // Gold Button on Clasp
    drawCircle(
      brush = Brush.radialGradient(colors = listOf(Color(0xFFFDE68A), NumoDS.DuneOchre)),
      radius = w * 0.045f,
      center = Offset(w * 0.68f, h * 0.58f)
    )
  }
}

/**
 * Screen 3: 3D Turquoise Envelope + Black/Gold Security Lock Badge
 */
@Composable
fun Numo3DEnvelopeLockIllustration(
  size: Dp = 110.dp,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier.size(size)) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = this.size.width
      val h = this.size.height

      // Envelope shadow
      drawRoundRect(
        color = Color(0x280A6F62),
        topLeft = Offset(w * 0.08f, h * 0.24f),
        size = Size(w * 0.76f, h * 0.56f),
        cornerRadius = CornerRadius(w * 0.12f)
      )

      // Envelope body
      drawRoundRect(
        brush = Brush.linearGradient(
          colors = listOf(Color(0xFF56F6EC), NumoDS.Turquoise, NumoDS.HeroTealMid)
        ),
        topLeft = Offset(w * 0.06f, h * 0.18f),
        size = Size(w * 0.76f, h * 0.56f),
        cornerRadius = CornerRadius(w * 0.12f)
      )

      // Envelope flap lines
      val flap = Path().apply {
        moveTo(w * 0.08f, h * 0.22f)
        lineTo(w * 0.44f, h * 0.48f)
        lineTo(w * 0.80f, h * 0.22f)
      }
      drawPath(
        path = flap,
        color = NumoDS.HeroTealDark.copy(alpha = 0.55f),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
      )
    }

    // Black circular security lock badge at bottom-right
    Box(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 6.dp, bottom = 10.dp)
        .size(44.dp)
        .shadow(8.dp, CircleShape, spotColor = Color(0x66000000))
        .clip(CircleShape)
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF3F3F46), NumoDS.Black)
          )
        )
        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = "Security Lock",
        tint = Color(0xFFFBBF24),
        modifier = Modifier.size(22.dp)
      )
    }
  }
}

/**
 * Screen 4: 3D Gold Kenyan Coins Stack + Black "K" Coin
 */
@Composable
fun Numo3DCurrencyCoinsIllustration(
  size: Dp = 108.dp,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = this.size.width
      val h = this.size.height

      // Stack of 4 gold coins on the right
      val coinX = w * 0.46f
      val coinW = w * 0.40f
      val coinH = h * 0.16f
      for (i in 0..3) {
        val y = h * (0.68f - i * 0.12f)
        drawRoundRect(
          brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFFDE68A), NumoDS.DuneOchre, Color(0xFFB45309))
          ),
          topLeft = Offset(coinX, y),
          size = Size(coinW, coinH),
          cornerRadius = CornerRadius(coinH * 0.5f)
        )
      }

      // Top upright gold coin
      drawCircle(
        brush = Brush.radialGradient(colors = listOf(Color(0xFFFDE68A), NumoDS.DuneOchre)),
        radius = w * 0.11f,
        center = Offset(w * 0.66f, h * 0.22f)
      )
    }

    // Front Black "K" Coin
    Box(
      modifier = Modifier
        .padding(end = 26.dp, top = 16.dp)
        .size(48.dp)
        .shadow(8.dp, CircleShape, spotColor = Color(0x66000000))
        .clip(CircleShape)
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF3F3F46), NumoDS.Black)
          )
        )
        .border(1.5.dp, Color(0xFFFBBF24).copy(alpha = 0.7f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "K",
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFFFBBF24)
      )
    }
  }
}

/**
 * Screen 5: 3D Turquoise Banking Institution + Black/Gold "+" Badge
 */
@Composable
fun Numo3DBankIllustration(
  size: Dp = 104.dp,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier.size(size)) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = this.size.width
      val h = this.size.height

      // Roof Triangle
      val roof = Path().apply {
        moveTo(w * 0.45f, h * 0.10f)
        lineTo(w * 0.84f, h * 0.34f)
        lineTo(w * 0.06f, h * 0.34f)
        close()
      }
      drawPath(
        roof,
        brush = Brush.verticalGradient(colors = listOf(Color(0xFF56F6EC), NumoDS.HeroTealMid))
      )

      // 3 Columns
      listOf(0.18f, 0.41f, 0.64f).forEach { colX ->
        drawRoundRect(
          brush = Brush.verticalGradient(colors = listOf(Color(0xFF56F6EC), NumoDS.HeroTealDark)),
          topLeft = Offset(w * colX, h * 0.38f),
          size = Size(w * 0.12f, h * 0.34f),
          cornerRadius = CornerRadius(w * 0.04f)
        )
      }

      // Base Step
      drawRoundRect(
        brush = Brush.horizontalGradient(colors = listOf(NumoDS.Turquoise, NumoDS.HeroTealDark)),
        topLeft = Offset(w * 0.08f, h * 0.72f),
        size = Size(w * 0.74f, h * 0.12f),
        cornerRadius = CornerRadius(w * 0.06f)
      )
    }

    // Black "+" badge
    Box(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 6.dp, bottom = 10.dp)
        .size(40.dp)
        .shadow(8.dp, CircleShape)
        .clip(CircleShape)
        .background(
          brush = Brush.verticalGradient(colors = listOf(Color(0xFF3F3F46), NumoDS.Black))
        )
        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "+",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFFBBF24)
      )
    }
  }
}

/**
 * Screen 6: Large Green Success Checkmark inside a Raised Circular Neumorphic Surface
 */
@Composable
fun NumoSuccessCheckmarkEmblem(
  size: Dp = 106.dp,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .size(size)
      .shadow(14.dp, CircleShape, spotColor = NumoDS.CardShadowWarm)
      .clip(CircleShape)
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(NumoDS.CardSurfaceLight, NumoDS.CardSurface)
        )
      )
      .border(2.dp, Color.White, CircleShape),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .size(size * 0.72f)
        .shadow(8.dp, CircleShape, spotColor = Color(0x5516A34A))
        .clip(CircleShape)
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF22C55E), Color(0xFF059669))
          )
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Check,
        contentDescription = "Connected",
        tint = Color.White,
        modifier = Modifier.size(size * 0.42f)
      )
    }
  }
}

/**
 * Screen 7: Large Golden 3D Trophy with Star & Black Base
 */
@Composable
fun Numo3DTrophyIllustration(
  size: Dp = 132.dp,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = this.size.width
      val h = this.size.height

      // Handles
      drawArc(
        color = NumoDS.DuneOchre,
        startAngle = 90f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(w * 0.12f, h * 0.16f),
        size = Size(w * 0.24f, h * 0.30f),
        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
      )
      drawArc(
        color = NumoDS.DuneOchre,
        startAngle = -90f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(w * 0.64f, h * 0.16f),
        size = Size(w * 0.24f, h * 0.30f),
        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
      )

      // Trophy Cup Body
      val cup = Path().apply {
        moveTo(w * 0.24f, h * 0.12f)
        lineTo(w * 0.76f, h * 0.12f)
        cubicTo(w * 0.76f, h * 0.48f, w * 0.62f, h * 0.56f, w * 0.50f, h * 0.58f)
        cubicTo(w * 0.38f, h * 0.56f, w * 0.24f, h * 0.48f, w * 0.24f, h * 0.12f)
        close()
      }
      drawPath(
        cup,
        brush = Brush.linearGradient(
          colors = listOf(Color(0xFFFDE68A), NumoDS.DuneOchre, Color(0xFFB45309))
        )
      )

      // Stem
      drawRoundRect(
        brush = Brush.horizontalGradient(colors = listOf(Color(0xFFFCD34D), NumoDS.DuneOchre)),
        topLeft = Offset(w * 0.44f, h * 0.56f),
        size = Size(w * 0.12f, h * 0.18f),
        cornerRadius = CornerRadius(4.dp.toPx())
      )

      // Black Base
      drawRoundRect(
        brush = Brush.verticalGradient(colors = listOf(Color(0xFF3F3F46), NumoDS.Black)),
        topLeft = Offset(w * 0.30f, h * 0.72f),
        size = Size(w * 0.40f, h * 0.16f),
        cornerRadius = CornerRadius(w * 0.18f)
      )
    }

    Icon(
      imageVector = Icons.Default.Star,
      contentDescription = "Trophy Star",
      tint = Color(0xFFFEF08A),
      modifier = Modifier
        .padding(bottom = 28.dp)
        .size(34.dp)
    )
  }
}
