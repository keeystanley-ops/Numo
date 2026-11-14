package com.example.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NumoClayHeroCard
import com.example.ui.components.NumoClayIconBadge
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoScreenBackground

@Composable
fun SpendingInsightsScreen(
  currency: String = "KES",
  onBack: () -> Unit = {}
) {
  NumoScreenBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
    ) {
      Spacer(modifier = Modifier.height(14.dp))

      // 1. Top Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onBack() },
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Outlined.Lightbulb,
          contentDescription = "Insights",
          tint = NumoDS.TextPrimary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Spending Insights",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Hero "You're on track!" Card
      NumoClayHeroCard {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "You're on track!",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Your spending is 12% lower than last month.",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = Color.White.copy(alpha = 0.92f),
              lineHeight = 18.sp
            )
          }

          Spacer(modifier = Modifier.width(16.dp))

          // 3 Glowing Cyan Ascending Bars
          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom
          ) {
            listOf(22.dp, 36.dp, 52.dp).forEach { barH ->
              Box(
                modifier = Modifier
                  .width(11.dp)
                  .height(barH)
                  .shadow(4.dp, RoundedCornerShape(50), spotColor = NumoDS.Cyan)
                  .clip(RoundedCornerShape(50))
                  .background(
                    brush = Brush.verticalGradient(
                      colors = listOf(Color(0xFF9DFDF6), NumoDS.Cyan, NumoDS.Turquoise)
                    )
                  )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 3. Key Insights Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Key Insights",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )
        Text(
          text = "See All >",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = NumoDS.TextSecondary
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      InsightRowCard(
        icon = Icons.Default.Restaurant,
        iconTopColor = Color(0xFFFBBF24),
        iconBottomColor = NumoDS.DuneOchre,
        text = "Food spending is up 18%",
        showChevron = true
      )

      Spacer(modifier = Modifier.height(10.dp))

      InsightRowCard(
        icon = Icons.Default.DirectionsCar,
        iconTopColor = Color(0xFF0EA5E9),
        iconBottomColor = NumoDS.TransportBlue,
        text = "Transport costs are down 12%",
        showChevron = false
      )

      Spacer(modifier = Modifier.height(10.dp))

      InsightRowCard(
        icon = Icons.Default.Savings,
        iconTopColor = Color(0xFF4ADE80),
        iconBottomColor = NumoDS.SuccessGreen,
        text = "You saved $currency 8,000 this month",
        showChevron = false
      )

      Spacer(modifier = Modifier.height(16.dp))

      // 4. Smart Tip Mint Glass Card
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(20.dp),
            ambientColor = NumoDS.CardShadowWarm,
            spotColor = NumoDS.CardShadowWarm
          )
          .clip(RoundedCornerShape(20.dp))
          .background(
            brush = Brush.linearGradient(
              colors = listOf(Color(0xFFB5EFE5), Color(0xFFDFF7F2)),
              start = Offset(0f, 0f),
              end = Offset(700f, 300f)
            )
          )
          .border(1.2.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          NumoClayIconBadge(
            icon = Icons.Default.Lightbulb,
            topColor = NumoDS.CyanBright,
            bottomColor = NumoDS.HeroTealMid,
            size = 44.dp,
            iconSize = 24.dp
          )

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Smart Tip",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = NumoDS.TextPrimary
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
              text = "Try cooking more at home to save up to 30% on food.",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = NumoDS.TextSecondary,
              lineHeight = 16.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun InsightRowCard(
  icon: ImageVector,
  iconTopColor: Color,
  iconBottomColor: Color,
  text: String,
  showChevron: Boolean
) {
  NumoNeumorphicCard(
    modifier = Modifier.fillMaxWidth(),
    cornerRadius = 18.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      NumoClayIconBadge(
        icon = icon,
        topColor = iconTopColor,
        bottomColor = iconBottomColor,
        size = 36.dp,
        iconSize = 18.dp
      )

      Spacer(modifier = Modifier.width(12.dp))

      Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = NumoDS.TextPrimary,
        modifier = Modifier.weight(1f)
      )

      if (showChevron) {
        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = null,
          tint = NumoDS.TextSecondary,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}
