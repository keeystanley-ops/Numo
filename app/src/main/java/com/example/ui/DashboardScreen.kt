package com.example.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.example.ui.theme.NumoDarkForestGreen
import com.example.ui.theme.NumoGlassCardBg
import com.example.ui.theme.NumoGlassCardBorder
import com.example.ui.theme.NumoMintBackground
import com.example.ui.theme.NumoSlateGreen

/**
 * Clean "Welcome to Numo" Screen:
 * The post-onboarding landing destination requested by the user.
 * Stripped of all extra dashboard tabs, budgets, and analytics so building
 * can proceed fresh.
 */
@Composable
fun DashboardScreen(
  userEmail: String = "Ayesha Khan",
  provider: String = "Google",
  userCurrency: String = "KES",
  customAccounts: List<Pair<String, String>> = emptyList(),
  onSignOut: () -> Unit = {}
) {
  val displayName = remember(userEmail) {
    if (userEmail.contains("@")) {
      val namePart = userEmail.substringBefore("@")
      if (namePart.equals("user", ignoreCase = true) || namePart.isBlank()) {
        "Ayesha Khan"
      } else {
        namePart.replaceFirstChar { it.uppercase() }
      }
    } else if (userEmail.isNotBlank()) {
      userEmail
    } else {
      "Ayesha Khan"
    }
  }

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

        // Top Header: App Branding + Sign Out
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
              painter = painterResource(id = R.drawable.ic_numo_leaf_transparent),
              contentDescription = "Numo Leaf",
              modifier = Modifier.size(28.dp),
              contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Numo",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = NumoDarkForestGreen
            )
          }

          // Sign Out Pill
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(Color.White.copy(alpha = 0.85f))
              .border(1.dp, NumoGlassCardBorder, RoundedCornerShape(20.dp))
              .clickable { onSignOut() }
              .padding(horizontal = 12.dp, vertical = 6.dp)
              .testTag("dashboard_sign_out_button"),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Logout,
              contentDescription = "Sign Out",
              tint = NumoSlateGreen,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Sign Out",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = NumoDarkForestGreen
            )
          }
        }

        Spacer(modifier = Modifier.weight(0.4f))

        // Centered Glass Terrarium / Numo Brand Art
        Box(
          modifier = Modifier
            .size(140.dp)
            .shadow(
              elevation = 16.dp,
              shape = CircleShape,
              spotColor = Color(0x331E5631),
              ambientColor = Color(0x1A1E5631)
            )
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.7f))
            .border(2.dp, Color.White, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.numo_glass_plant_hero),
            contentDescription = "Numo Plant",
            modifier = Modifier
              .size(118.dp)
              .testTag("dashboard_welcome_art"),
            contentScale = ContentScale.Fit
          )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Welcome Headline
        Text(
          text = "Welcome to Numo",
          fontSize = 30.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.SansSerif,
          color = NumoDarkForestGreen,
          textAlign = TextAlign.Center,
          modifier = Modifier.testTag("welcome_to_numo_headline")
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Hello, $displayName! Your green finance journey begins here.",
          fontSize = 14.sp,
          color = NumoSlateGreen,
          textAlign = TextAlign.Center,
          lineHeight = 20.sp,
          modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Frosted Setup Confirmation Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(
              elevation = 6.dp,
              shape = RoundedCornerShape(22.dp),
              spotColor = Color(0x181E5631),
              ambientColor = Color(0x0C1E5631)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(NumoGlassCardBg)
            .border(1.dp, NumoGlassCardBorder, RoundedCornerShape(22.dp))
            .padding(20.dp)
            .testTag("dashboard_setup_card")
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "ACCOUNT STATUS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NumoSlateGreen,
                letterSpacing = 0.8.sp
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = "Active",
                  tint = Color(0xFF10B981),
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Active & Ready",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF10B981)
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = "Currency",
                  fontSize = 12.sp,
                  color = NumoSlateGreen
                )
                Text(
                  text = userCurrency,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = NumoDarkForestGreen
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "Connected Accounts",
                  fontSize = 12.sp,
                  color = NumoSlateGreen
                )
                Text(
                  text = if (customAccounts.isNotEmpty()) "${customAccounts.size} Linked" else "Primary Account",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = NumoDarkForestGreen
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.weight(0.6f))
      }
    }
  }
}
