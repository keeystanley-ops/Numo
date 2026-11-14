package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AnimatedGlassCheckmark
import com.example.ui.theme.NumoDarkForestGreen
import com.example.ui.theme.NumoGlassCardBg
import com.example.ui.theme.NumoGlassCardBorder
import com.example.ui.theme.NumoMintBackground
import com.example.ui.theme.NumoSlateGreen

/**
 * Screen 3 of Onboarding: "All Set!"
 * Matches screenshot image_c32814bd.png (right screen) with dynamic animated glassmorphism:
 * - Mint background (#E5F6F1)
 * - Top Header: "All Set!"
 * - Centered Numo Leaf logo
 * - Large frosted glass card with multi-layer box shadows:
 *     - AnimatedGlassCheckmark: 3D frosted glass pebble with sweep ring, pop bounce,
 *       radiant energy burst rays, and tracing neon emerald checkmark
 *     - "Your green finance\njourney is ready!" headline
 *     - "Go to Home Screen" frosted button
 *     - "Add More Accounts" secondary text link
 */
@Composable
fun AllSetScreen(
  onGoToHome: () -> Unit,
  onAddMoreAccounts: () -> Unit = {}
) {
  Surface(
    modifier = Modifier.fillMaxSize(),
    color = NumoMintBackground
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Top Header: "All Set!"
        Text(
          text = "All Set!",
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
          color = NumoDarkForestGreen,
          textAlign = TextAlign.Center,
          modifier = Modifier.testTag("allset_top_header")
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Leaf Logo
        Image(
          painter = painterResource(id = R.drawable.ic_numo_leaf_transparent),
          contentDescription = "Numo Brand Logo",
          modifier = Modifier
            .size(48.dp)
            .testTag("allset_brand_logo"),
          contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 3. Central Frosted Glass Card with Multi-Layer Shadows
        Box(
          modifier = Modifier
            .fillMaxWidth()
            // Ambient soft shadow
            .shadow(
              elevation = 6.dp,
              shape = RoundedCornerShape(26.dp),
              spotColor = Color(0x181E5631),
              ambientColor = Color(0x0A1E5631)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(NumoGlassCardBg)
            .border(1.dp, NumoGlassCardBorder, RoundedCornerShape(26.dp))
            .padding(horizontal = 24.dp, vertical = 28.dp)
            .testTag("allset_glass_card"),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
          ) {
            // Animated Glassmorphism Checkmark
            AnimatedGlassCheckmark(
              size = 110.dp,
              onAnimationFinished = {
                // When animation completes, automatically advances to home
                onGoToHome()
              },
              modifier = Modifier.testTag("allset_animated_glass_checkmark")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Headline
            Text(
              text = "Your green finance\njourney is ready!",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = NumoDarkForestGreen,
              textAlign = TextAlign.Center,
              lineHeight = 28.sp,
              modifier = Modifier.testTag("allset_journey_ready_text")
            )

            Spacer(modifier = Modifier.height(28.dp))

            // "Go to Home Screen" Frosted Button
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .shadow(
                  elevation = 2.dp,
                  shape = RoundedCornerShape(14.dp),
                  spotColor = Color(0x121E5631),
                  ambientColor = Color(0x081E5631)
                )
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.85f))
                .border(1.dp, NumoGlassCardBorder, RoundedCornerShape(14.dp))
                .clickable { onGoToHome() }
                .testTag("go_to_home_button"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Go to Home Screen",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = NumoDarkForestGreen,
                letterSpacing = 0.2.sp
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "Add More Accounts" Secondary Link
            Text(
              text = "Add More Accounts",
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              color = NumoSlateGreen,
              textAlign = TextAlign.Center,
              modifier = Modifier
                .clickable { onAddMoreAccounts() }
                .testTag("add_more_accounts_link")
                .padding(vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.weight(1f))
      }
    }
  }
}
