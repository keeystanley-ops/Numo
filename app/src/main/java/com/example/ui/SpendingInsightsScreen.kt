package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.animation.animateIntCount
import com.example.ui.animation.animateProgress
import com.example.ui.animation.bouncyClickable
import com.example.ui.animation.formatAmount
import com.example.ui.animation.staggeredEntrance

/**
 * Spending Insights Screen matching Screen 8 of the user reference mockup:
 * - Header: "<- Spending Insights" with calendar action
 * - Pill toggle: [This Month | Last Month | Custom]
 * - Donut chart: Total Spent KES 78,420 with Needs 62% (green) & Wants 38% (coral/orange)
 * - Top Merchants: Naivas 32%, Uber 18%, Jumia 12%
 */
@Composable
fun SpendingInsightsScreen(
  currency: String = "KES",
  onBack: () -> Unit = {}
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: This Month, 1: Last Month, 2: Custom
  val scrollState = rememberScrollState()

  val totalSpent = 78420
  val animatedSpent = animateIntCount(totalSpent, durationMs = 850)

  val animatedNeeds = animateProgress(0.62f, durationMs = 950)
  val animatedWants = animateProgress(0.38f, durationMs = 950)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAF9))
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      // Top bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.White)
            .bouncyClickable { onBack() }
            .testTag("btn_insights_back")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Back",
            tint = Color(0xFF0F172A)
          )
        }

        Text(
          text = "Spending Insights",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )

        IconButton(onClick = {}) {
          Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = "Calendar",
            tint = Color(0xFF334155),
            modifier = Modifier.size(22.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Pill toggle [This Month | Last Month | Custom]
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(Color(0xFFEDF2F7))
          .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        listOf("This Month", "Last Month", "Custom").forEachIndexed { index, label ->
          val isSelected = (selectedTab == index)
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(16.dp))
              .background(if (isSelected) Color(0xFF02844E) else Color.Transparent)
              .bouncyClickable { selectedTab = index }
              .padding(vertical = 9.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = label,
              fontSize = 13.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color.White else Color(0xFF64748B)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // DONUT CHART CARD (Screen 8)
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .staggeredEntrance(delayMs = 40),
        color = Color.White,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 1.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Donut Chart
          Box(
            modifier = Modifier.size(130.dp),
            contentAlignment = Alignment.Center
          ) {
            Canvas(modifier = Modifier.size(120.dp)) {
              val strokeWidth = 14.dp.toPx()

              // Needs: 62% (Green)
              drawArc(
                color = Color(0xFF02844E),
                startAngle = -90f,
                sweepAngle = 360f * 0.62f * animatedNeeds,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
              )

              // Wants: 38% (Orange/Coral)
              drawArc(
                color = Color(0xFFFB923C),
                startAngle = -90f + (360f * 0.62f),
                sweepAngle = 360f * 0.38f * animatedWants,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
              )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = currency,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
              )
              Text(
                text = formatAmount(animatedSpent),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
              )
              Text(
                text = "Total Spent",
                fontSize = 10.sp,
                color = Color(0xFF64748B)
              )
            }
          }

          Spacer(modifier = Modifier.width(24.dp))

          // Legend
          Column(modifier = Modifier.weight(1f)) {
            // Needs: 62%
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF02844E))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Needs",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Medium,
                  color = Color(0xFF334155)
                )
              }
              Text(
                text = "62%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Wants: 38%
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFB923C))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Wants",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Medium,
                  color = Color(0xFF334155)
                )
              }
              Text(
                text = "38%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // TOP MERCHANTS (Screen 8)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Top Merchants",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "See All",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF02844E)
          )
          Spacer(modifier = Modifier.width(2.dp))
          Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "See All",
            tint = Color(0xFF02844E),
            modifier = Modifier.size(14.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 1. Naivas (32%)
      MerchantRowItem(
        name = "Naivas",
        percentage = 32,
        icon = Icons.Default.Storefront,
        iconBg = Color(0xFFFEF3C7),
        iconTint = Color(0xFFF59E0B),
        delayMs = 80
      )

      Spacer(modifier = Modifier.height(10.dp))

      // 2. Uber (18%)
      MerchantRowItem(
        name = "Uber",
        percentage = 18,
        icon = Icons.Default.DirectionsCar,
        iconBg = Color(0xFF0F172A),
        iconTint = Color.White,
        delayMs = 120
      )

      Spacer(modifier = Modifier.height(10.dp))

      // 3. Jumia (12%)
      MerchantRowItem(
        name = "Jumia",
        percentage = 12,
        icon = Icons.Default.LocalMall,
        iconBg = Color(0xFFFFEDD5),
        iconTint = Color(0xFFF97316),
        delayMs = 160
      )

      Spacer(modifier = Modifier.height(60.dp))
    }
  }
}

@Composable
fun MerchantRowItem(
  name: String,
  percentage: Int,
  icon: ImageVector,
  iconBg: Color,
  iconTint: Color,
  delayMs: Int
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .staggeredEntrance(delayMs = delayMs),
    color = Color.White,
    shape = RoundedCornerShape(16.dp),
    shadowElevation = 0.5.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(iconBg),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = name,
          tint = iconTint,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Text(
        text = name,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0F172A),
        modifier = Modifier.weight(1f)
      )

      Text(
        text = "$percentage%",
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0F172A)
      )
    }
  }
}
