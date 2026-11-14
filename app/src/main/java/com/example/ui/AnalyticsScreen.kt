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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Hexagon
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NumoClayIconBadge
import com.example.ui.components.NumoClayProgressBar
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoScreenBackground
import com.example.ui.components.NumoSegmentedControl

@Composable
fun AnalyticsScreen(
  currency: String = "KES",
  onOpenInsights: () -> Unit = {},
  onOpenSettings: () -> Unit = {},
  onBack: () -> Unit = {}
) {
  var selectedPeriodIndex by remember { mutableIntStateOf(1) } // 0: Week, 1: Month, 2: Year

  val barHeights = listOf(0.32f, 0.56f, 0.24f, 0.38f, 0.34f, 0.54f, 0.64f, 0.72f, 0.85f, 1.0f, 0.58f)

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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Analytics",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )

        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable { onOpenSettings() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Hexagon,
            contentDescription = "Settings",
            tint = NumoDS.TextPrimary,
            modifier = Modifier.size(26.dp)
          )
          Box(
            modifier = Modifier
              .size(12.dp)
              .clip(CircleShape)
              .background(NumoDS.PeachBgTop)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Period Segmented Control
      NumoSegmentedControl(
        options = listOf("Week", "Month", "Year"),
        selectedIndex = selectedPeriodIndex,
        onSelect = { selectedPeriodIndex = it }
      )

      Spacer(modifier = Modifier.height(16.dp))

      // 3. Total Spending & 3D Bar Chart Card
      NumoNeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 22.dp
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Total Spending",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = NumoDS.TextSecondary
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "$currency 52,000",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary,
            letterSpacing = (-0.4).sp
          )

          Spacer(modifier = Modifier.height(4.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.ArrowDownward,
              contentDescription = null,
              tint = NumoDS.SuccessGreen,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "12.5% vs last month",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = NumoDS.SuccessGreen
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          // 3D Glossy Teal Bar Chart
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(126.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
          ) {
            barHeights.forEach { frac ->
              Box(
                modifier = Modifier
                  .width(18.dp)
                  .height((118 * frac).dp)
                  .shadow(
                    elevation = 5.dp,
                    shape = RoundedCornerShape(50),
                    spotColor = Color(0x40033B31)
                  )
                  .clip(RoundedCornerShape(50))
                  .background(
                    brush = Brush.verticalGradient(
                      colors = listOf(
                        NumoDS.HeroTealTopLeft,
                        NumoDS.HeroTealMid,
                        NumoDS.HeroTealDark
                      )
                    )
                  )
                  .border(0.8.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(50))
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            listOf("1", "7", "14", "21", "28").forEach { day ->
              Text(
                text = day,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = NumoDS.TextMuted
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 4. Top Categories Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Top Categories",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )
        Text(
          text = "See All >",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = NumoDS.TextSecondary,
          modifier = Modifier.clickable { onOpenInsights() }
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      AnalyticsCategoryRow(
        icon = Icons.Default.Restaurant,
        iconTopColor = Color(0xFFFBBF24),
        iconBottomColor = NumoDS.DuneOchre,
        title = "Food & Groceries",
        percentText = "32%",
        progress = 0.56f,
        barTopColor = NumoDS.CyanBright,
        barBottomColor = NumoDS.Turquoise
      )

      Spacer(modifier = Modifier.height(10.dp))

      AnalyticsCategoryRow(
        icon = Icons.Default.DirectionsCar,
        iconTopColor = Color(0xFF0EA5E9),
        iconBottomColor = NumoDS.TransportBlue,
        title = "Transport",
        percentText = "22%",
        progress = 0.44f,
        barTopColor = Color(0xFF38BDF8),
        barBottomColor = NumoDS.Turquoise
      )

      Spacer(modifier = Modifier.height(10.dp))

      AnalyticsCategoryRow(
        icon = Icons.Default.ShoppingBag,
        iconTopColor = Color(0xFFF43F5E),
        iconBottomColor = NumoDS.ExpenseRed,
        title = "Shopping",
        percentText = "18%",
        progress = 0.36f,
        barTopColor = NumoDS.TransportBlueLight,
        barBottomColor = NumoDS.TransportBlue
      )

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun AnalyticsCategoryRow(
  icon: ImageVector,
  iconTopColor: Color,
  iconBottomColor: Color,
  title: String,
  percentText: String,
  progress: Float,
  barTopColor: Color,
  barBottomColor: Color
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
        size = 38.dp,
        iconSize = 19.dp
      )

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = NumoDS.TextPrimary
          )
          Text(
            text = percentText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )
        }

        Spacer(modifier = Modifier.height(7.dp))

        NumoClayProgressBar(
          progress = progress,
          fillTopColor = barTopColor,
          fillBottomColor = barBottomColor,
          height = 7.dp
        )
      }
    }
  }
}
