package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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

/**
 * SCREEN 1: Numo Splash Screen matching 01_splash.png
 */
@Composable
fun WelcomeScreen(
  onGetStarted: () -> Unit
) {
  NumoScreenBackground(
    modifier = Modifier.clickable(
      interactionSource = remember { MutableInteractionSource() },
      indication = null,
      onClick = onGetStarted
    )
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
          modifier = Modifier.testTag("welcome_brand_title")
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Plan • Save • Grow",
          fontSize = 15.sp,
          fontWeight = FontWeight.Medium,
          color = NumoDS.HeroTealMid
        )

        Spacer(modifier = Modifier.height(38.dp))

        Text(
          text = "Take Control\nof Your Financial Future",
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold,
          color = NumoDS.TextPrimary,
          textAlign = TextAlign.Center,
          lineHeight = 22.sp
        )
      }

      // Bottom 3-dot indicator pill
      Box(
        modifier = Modifier
          .padding(bottom = 32.dp)
          .shadow(8.dp, RoundedCornerShape(50), spotColor = Color(0x28FFFFFF))
          .clip(RoundedCornerShape(50))
          .background(Color.White.copy(alpha = 0.82f))
          .clickable { onGetStarted() }
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
