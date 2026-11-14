package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.Numo3DTrophyIllustration
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoFloatingDecorations
import com.example.ui.components.NumoPrimaryButton
import com.example.ui.components.NumoScreenBackground

/**
 * Screen 7: You're All Set! — Exact match to the Numo onboarding reference.
 *
 * Final onboarding screen; tapping "Go to Dashboard" navigates directly to the
 * existing Numo budget and money-management DashboardScreen.
 */
@Composable
fun AllSetScreen(
  onGoToHome: () -> Unit,
  @Suppress("UNUSED_PARAMETER") onAddMoreAccounts: () -> Unit = {},
  onExploreFeatures: () -> Unit = onGoToHome
) {
  NumoScreenBackground(modifier = Modifier.testTag("all_set_screen")) {
    NumoFloatingDecorations(variant = 3)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.weight(0.65f))

      // Large golden 3D Trophy illustration
      Numo3DTrophyIllustration(
        modifier = Modifier.size(196.dp)
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Heading
      Text(
        text = "You're All Set!",
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        color = NumoDS.TextPrimary,
        letterSpacing = (-0.5).sp,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Supporting welcome message
      Text(
        text = "Your Numo journey is ready to begin.\nLet's take control of your finances!",
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        color = NumoDS.TextSecondary,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.weight(0.75f))

      // Go to Dashboard primary button
      NumoPrimaryButton(
        text = "Go to Dashboard",
        onClick = onGoToHome,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("btn_go_to_dashboard"),
        height = 54.dp
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Explore Features secondary link
      Text(
        text = "Explore Features",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = NumoDS.Turquoise,
        modifier = Modifier
          .clickable { onExploreFeatures() }
          .padding(vertical = 6.dp, horizontal = 14.dp)
          .testTag("btn_explore_features")
      )

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}
