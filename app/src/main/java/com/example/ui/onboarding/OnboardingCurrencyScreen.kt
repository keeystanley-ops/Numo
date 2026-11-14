package com.example.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NumoFinancialEngine
import com.example.ui.components.Numo3DCurrencyCoinsIllustration
import com.example.ui.components.NumoBackCircleButton
import com.example.ui.components.NumoFloatingDecorations
import com.example.ui.components.NumoRadioIndicator
import com.example.ui.theme.NumoDS
import com.example.ui.theme.NumoPrimaryButton
import com.example.ui.theme.NumoScreenBackground

private data class OnboardingCurrencyItem(
  val code: String,
  val name: String
)

private val ONBOARDING_CURRENCIES = listOf(
  OnboardingCurrencyItem("KES", "Kenyan Shilling"),
  OnboardingCurrencyItem("USD", "US Dollar"),
  OnboardingCurrencyItem("EUR", "Euro"),
  OnboardingCurrencyItem("GBP", "British Pound"),
  OnboardingCurrencyItem("TZS", "Tanzanian Shilling"),
  OnboardingCurrencyItem("UGX", "Ugandan Shilling")
)

/**
 * Screen 4: Select Your Currency — Exact match to the Numo onboarding reference.
 *
 * Updates both `NumoFinancialEngine.currency` and the parent onboarding state via `onContinue`.
 */
@Composable
fun OnboardingCurrencyScreen(
  initialCurrency: String = "KES",
  onBack: () -> Unit = {},
  onContinue: (selectedCurrency: String) -> Unit
) {
  var selectedCode by remember {
    mutableStateOf(
      if (ONBOARDING_CURRENCIES.any { it.code == initialCurrency }) initialCurrency else "KES"
    )
  }

  NumoScreenBackground(modifier = Modifier.testTag("onboarding_currency_screen")) {
    NumoFloatingDecorations(variant = 1)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      // Top bar with Back button
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        NumoBackCircleButton(
          onClick = onBack,
          modifier = Modifier.testTag("btn_currency_back")
        )
      }

      // 3D Kenyan Currency Coins illustration
      Numo3DCurrencyCoinsIllustration(
        modifier = Modifier.size(134.dp)
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Heading
      Text(
        text = "Select Your Currency",
        fontSize = 25.sp,
        fontWeight = FontWeight.ExtraBold,
        color = NumoDS.TextPrimary,
        letterSpacing = (-0.5).sp,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Subtitle
      Text(
        text = "Choose your preferred currency to\ncontinue with Numo.",
        fontSize = 13.5.sp,
        lineHeight = 19.sp,
        fontWeight = FontWeight.Normal,
        color = NumoDS.TextSecondary,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Currency cards list
      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        ONBOARDING_CURRENCIES.forEach { item ->
          val isSelected = selectedCode == item.code
          CurrencySelectionCard(
            item = item,
            isSelected = isSelected,
            onClick = { selectedCode = item.code },
            modifier = Modifier.testTag("currency_option_${item.code}")
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Continue primary button — updates real application currency state
      NumoPrimaryButton(
        text = "Continue",
        onClick = {
          NumoFinancialEngine.currency = selectedCode
          onContinue(selectedCode)
        },
        modifier = Modifier.testTag("btn_currency_continue")
      )

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun CurrencySelectionCard(
  item: OnboardingCurrencyItem,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(18.dp)
  val cardBrush = if (isSelected) {
    Brush.verticalGradient(
      colors = listOf(Color(0xFFFFF8F0), Color(0xFFF6E7D6))
    )
  } else {
    Brush.verticalGradient(
      colors = listOf(Color(0xFFFFF5EA), Color(0xFFF3E2D0))
    )
  }
  val borderColor = if (isSelected) NumoDS.Turquoise.copy(alpha = 0.55f) else Color(0xFFFFFBF6)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(56.dp)
      .shadow(
        elevation = if (isSelected) 10.dp else 6.dp,
        shape = shape,
        ambientColor = NumoDS.CardShadowWarm.copy(alpha = 0.3f),
        spotColor = NumoDS.CardShadowWarm.copy(alpha = 0.35f)
      )
      .clip(shape)
      .background(cardBrush)
      .border(if (isSelected) 1.5.dp else 1.dp, borderColor, shape)
      .clickable { onClick() }
      .padding(horizontal = 16.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      CircularFlagIcon(code = item.code, modifier = Modifier.size(28.dp))

      Spacer(modifier = Modifier.width(14.dp))

      Text(
        text = buildAnnotatedString {
          withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)) {
            append(item.code)
          }
          withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = NumoDS.TextSecondary)) {
            append(" – ${item.name}")
          }
        },
        fontSize = 14.5.sp,
        modifier = Modifier.weight(1f)
      )

      NumoRadioIndicator(selected = isSelected)
    }
  }
}

@Composable
private fun CircularFlagIcon(code: String, modifier: Modifier = Modifier) {
  Canvas(
    modifier = modifier
      .clip(CircleShape)
      .border(0.8.dp, Color(0x22000000), CircleShape)
  ) {
    val w = size.width
    val h = size.height
    when (code) {
      "KES" -> {
        drawRect(Color(0xFF000000), Offset.Zero, Size(w, h * 0.28f))
        drawRect(Color(0xFFFFFFFF), Offset(0f, h * 0.28f), Size(w, h * 0.06f))
        drawRect(Color(0xFFBB0000), Offset(0f, h * 0.34f), Size(w, h * 0.32f))
        drawRect(Color(0xFFFFFFFF), Offset(0f, h * 0.66f), Size(w, h * 0.06f))
        drawRect(Color(0xFF006600), Offset(0f, h * 0.72f), Size(w, h * 0.28f))
        // Maasai shield center accent
        drawOval(
          color = Color(0xFF9E1B1B),
          topLeft = Offset(w * 0.40f, h * 0.30f),
          size = Size(w * 0.20f, h * 0.40f)
        )
        drawCircle(Color.White, radius = w * 0.045f, center = Offset(w * 0.50f, h * 0.50f))
      }
      "USD" -> {
        val stripeH = h / 7f
        for (i in 0 until 7) {
          drawRect(
            color = if (i % 2 == 0) Color(0xFFB22234) else Color.White,
            topLeft = Offset(0f, i * stripeH),
            size = Size(w, stripeH + 0.5f)
          )
        }
        drawRect(Color(0xFF3C3B6E), Offset.Zero, Size(w * 0.50f, h * 0.54f))
        for (r in 0..2) {
          for (c in 0..2) {
            drawCircle(
              color = Color.White,
              radius = w * 0.025f,
              center = Offset(w * (0.11f + c * 0.14f), h * (0.13f + r * 0.14f))
            )
          }
        }
      }
      "EUR" -> {
        drawRect(Color(0xFF003399), Offset.Zero, Size(w, h))
        val cx = w * 0.5f
        val cy = h * 0.5f
        val ringR = w * 0.30f
        for (i in 0 until 12) {
          val angle = (i * 30.0) * Math.PI / 180.0
          val sx = cx + (ringR * kotlin.math.cos(angle)).toFloat()
          val sy = cy + (ringR * kotlin.math.sin(angle)).toFloat()
          drawCircle(Color(0xFFFFCC00), radius = w * 0.04f, center = Offset(sx, sy))
        }
      }
      "GBP" -> {
        drawRect(Color(0xFF012169), Offset.Zero, Size(w, h))
        drawLine(Color.White, Offset(0f, 0f), Offset(w, h), strokeWidth = w * 0.16f)
        drawLine(Color.White, Offset(w, 0f), Offset(0f, h), strokeWidth = w * 0.16f)
        drawLine(Color(0xFFC8102E), Offset(0f, 0f), Offset(w, h), strokeWidth = w * 0.07f)
        drawLine(Color(0xFFC8102E), Offset(w, 0f), Offset(0f, h), strokeWidth = w * 0.07f)
        drawRect(Color.White, Offset(w * 0.38f, 0f), Size(w * 0.24f, h))
        drawRect(Color.White, Offset(0f, h * 0.38f), Size(w, h * 0.24f))
        drawRect(Color(0xFFC8102E), Offset(w * 0.43f, 0f), Size(w * 0.14f, h))
        drawRect(Color(0xFFC8102E), Offset(0f, h * 0.43f), Size(w, h * 0.14f))
      }
      "TZS" -> {
        drawRect(Color(0xFF1EB53A), Offset.Zero, Size(w, h))
        val blueTriangle = Path().apply {
          moveTo(w, 0f)
          lineTo(w, h)
          lineTo(0f, h)
          close()
        }
        drawPath(blueTriangle, Color(0xFF00A3DD))
        drawLine(Color(0xFFFCD116), Offset(0f, h), Offset(w, 0f), strokeWidth = w * 0.30f)
        drawLine(Color.Black, Offset(0f, h), Offset(w, 0f), strokeWidth = w * 0.19f)
      }
      "UGX" -> {
        val stripeH = h / 6f
        val ugColors = listOf(
          Color.Black,
          Color(0xFFFCDC04),
          Color(0xFFD90000),
          Color.Black,
          Color(0xFFFCDC04),
          Color(0xFFD90000)
        )
        ugColors.forEachIndexed { idx, col ->
          drawRect(col, Offset(0f, idx * stripeH), Size(w, stripeH + 0.5f))
        }
        drawCircle(Color.White, radius = w * 0.17f, center = Offset(w * 0.5f, h * 0.5f))
        drawCircle(Color(0xFFD90000), radius = w * 0.06f, center = Offset(w * 0.5f, h * 0.48f))
      }
    }
  }
}
