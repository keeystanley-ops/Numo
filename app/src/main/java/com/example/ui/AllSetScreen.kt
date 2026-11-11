package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AnimatedSuccessCheckmark

/**
 * All Set completion screen shown after onboarding:
 * - Brand logo
 * - "All set" headline at the top
 * - The animated green checkmark
 * - Automatically advances to Dashboard once animation completes without user input
 */
@Composable
fun AllSetScreen(
  onAnimationComplete: () -> Unit
) {
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
              Color(0xFFF4FDF9),
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
          .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // 1. Logo
        Image(
          painter = painterResource(id = R.drawable.ic_numo_leaf),
          contentDescription = "Numo Brand Logo",
          modifier = Modifier
            .size(width = 68.dp, height = 54.dp)
            .testTag("allset_brand_logo"),
          contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Headline: "All set" at the top
        Text(
          text = "All set",
          fontSize = 40.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.SansSerif,
          color = Color(0xFF001A49),
          letterSpacing = (-0.5).sp,
          textAlign = TextAlign.Center,
          modifier = Modifier.testTag("all_set_title")
        )

        Spacer(modifier = Modifier.height(36.dp))

        // 3. Green animated checkmark below "All set"
        // Automatically redirects to the dashboard as soon as animation finishes
        AnimatedSuccessCheckmark(
          size = 140.dp,
          badgeColor = Color(0xFF00C884),
          trackColor = Color(0xFFE5E5E5),
          onAnimationFinished = {
            onAnimationComplete()
          },
          modifier = Modifier.testTag("animated_success_checkmark")
        )
      }
    }
  }
}
