package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.animation.bouncyClickable
import com.example.ui.theme.NumoDarkForestGreen
import com.example.ui.theme.NumoElementsCorners
import com.example.ui.theme.NumoMintBackground
import com.example.ui.theme.NumoSlateGreen
import com.example.ui.theme.NumoSoftGreen

/**
 * Numo Splash & Get Started Screen matching center screen of image_971e9f83.png identically:
 * - Mint background (#E5F6F1)
 * - Top centered Numo leaf icon
 * - Center glass terrarium plant with coin stack
 * - Large bold "Numo" in Dark Forest Green (#1E5631)
 * - "Your green finance journey" subtitle in Slate Green (#2C7360)
 * - Bottom glowing circular chevron button
 */
@Composable
fun WelcomeScreen(
  onGetStarted: () -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "halo_glow")
  val haloScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.12f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "halo_scale"
  )
  val haloAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 0.60f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "halo_alpha"
  )

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = NumoMintBackground
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(NumoMintBackground)
        .statusBarsPadding()
        .navigationBarsPadding()
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // 1. Top Leaf Logo (Centered)
        Box(
          modifier = Modifier
            .padding(top = 28.dp)
            .testTag("welcome_top_leaf"),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.ic_numo_leaf_transparent),
            contentDescription = "Numo Leaf Logo",
            modifier = Modifier.size(46.dp),
            contentScale = ContentScale.Fit
          )
        }

        // 2. Center Hero Section: Terrarium + "Numo" + Subtitle
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
        ) {
          Image(
            painter = painterResource(id = R.drawable.numo_glass_plant_hero),
            contentDescription = "Numo Plant and Coins Hero",
            modifier = Modifier
              .size(width = 220.dp, height = 200.dp)
              .testTag("welcome_hero_illustration"),
            contentScale = ContentScale.Fit
          )

          Spacer(modifier = Modifier.height(28.dp))

          // Brand Title: "Numo"
          Text(
            text = "Numo",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = NumoDarkForestGreen,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.testTag("welcome_brand_title")
          )

          Spacer(modifier = Modifier.height(6.dp))

          // Subtitle: "Your green finance journey"
          Text(
            text = "Your green finance journey",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif,
            color = NumoSlateGreen,
            modifier = Modifier.testTag("welcome_subtitle")
          )
        }

        // 3. Bottom Action Button: Glowing circular chevron button
        Box(
          modifier = Modifier
            .padding(bottom = 44.dp)
            .testTag("btn_welcome_get_started"),
          contentAlignment = Alignment.Center
        ) {
          // Soft radiant halo
          Box(
            modifier = Modifier
              .size(70.dp)
              .scale(haloScale)
              .clip(CircleShape)
              .background(NumoSoftGreen.copy(alpha = haloAlpha))
          )

          // Circular frosted mint button
          Box(
            modifier = Modifier
              .size(52.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.85f))
              .border(1.5.dp, NumoElementsCorners, CircleShape)
              .bouncyClickable {
                onGetStarted()
              },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
              contentDescription = "Get Started",
              tint = NumoSlateGreen,
              modifier = Modifier.size(30.dp)
            )
          }
        }
      }
    }
  }
}
