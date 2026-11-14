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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.CalendarToday
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
import com.example.ui.components.NumoClayIconBadge
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoScreenBackground

@Composable
fun UpcomingBillsScreen(
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Upcoming Bills",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )

        Icon(
          imageVector = Icons.Outlined.CalendarToday,
          contentDescription = "Calendar",
          tint = NumoDS.TextPrimary,
          modifier = Modifier
            .size(24.dp)
            .clickable { onBack() }
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Next Bill Mint-Cyan Hero Card
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(
            elevation = 10.dp,
            shape = RoundedCornerShape(22.dp),
            ambientColor = NumoDS.CardShadowWarm,
            spotColor = NumoDS.CardShadowWarm
          )
          .clip(RoundedCornerShape(22.dp))
          .background(
            brush = Brush.linearGradient(
              colors = listOf(
                Color(0xFFA6F4EC),
                Color(0xFFDDF7F3),
                NumoDS.CardSurfaceLight
              ),
              start = Offset(0f, 0f),
              end = Offset(700f, 350f)
            )
          )
          .border(1.2.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(22.dp))
          .padding(18.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Next Bill",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = NumoDS.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Rent",
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              color = NumoDS.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "$currency 15,000",
              fontSize = 19.sp,
              fontWeight = FontWeight.Bold,
              color = NumoDS.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Due in 3 days",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = NumoDS.TextSecondary
            )
          }

          Box(
            modifier = Modifier
              .size(80.dp)
              .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0x280A6F62))
              .clip(RoundedCornerShape(20.dp))
              .background(
                brush = Brush.verticalGradient(
                  colors = listOf(Color(0xFFE7FAF7), Color(0xFFCDEEE8))
                )
              )
              .border(1.2.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Home,
              contentDescription = "Rent",
              tint = NumoDS.HeroTealMid,
              modifier = Modifier.size(46.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // 3. Bill Timeline Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Bill Timeline",
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

      Spacer(modifier = Modifier.height(12.dp))

      BillTimelineRow(
        icon = Icons.Default.Notifications,
        iconTopColor = Color(0xFFF43F5E),
        iconBottomColor = NumoDS.ExpenseRed,
        title = "Rent",
        amount = "$currency 15,000",
        dueDate = "Due: May 17",
        daysText = "3d",
        badgeTopColor = Color(0xFFFBBF24),
        badgeBottomColor = NumoDS.DuneOchre
      )

      Spacer(modifier = Modifier.height(12.dp))

      BillTimelineRow(
        icon = Icons.Default.Bolt,
        iconTopColor = Color(0xFFFCD34D),
        iconBottomColor = NumoDS.DuneOchre,
        title = "KPLC",
        amount = "$currency 3,500",
        dueDate = "Due: May 20",
        daysText = "6d",
        badgeTopColor = Color(0xFFFBBF24),
        badgeBottomColor = NumoDS.DuneOchre
      )

      Spacer(modifier = Modifier.height(12.dp))

      BillTimelineRow(
        icon = Icons.Default.WaterDrop,
        iconTopColor = Color(0xFF38BDF8),
        iconBottomColor = NumoDS.TransportBlue,
        title = "Water",
        amount = "$currency 1,000",
        dueDate = "Due: May 22",
        daysText = "8d",
        badgeTopColor = Color(0xFF0EA5E9),
        badgeBottomColor = NumoDS.TransportBlue
      )

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun BillTimelineRow(
  icon: ImageVector,
  iconTopColor: Color,
  iconBottomColor: Color,
  title: String,
  amount: String,
  dueDate: String,
  daysText: String,
  badgeTopColor: Color,
  badgeBottomColor: Color
) {
  NumoNeumorphicCard(
    modifier = Modifier.fillMaxWidth(),
    cornerRadius = 18.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 14.dp),
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
        Text(
          text = title,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = amount,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = dueDate,
          fontSize = 11.sp,
          color = NumoDS.TextMuted
        )
      }

      Box(
        modifier = Modifier
          .size(38.dp)
          .shadow(5.dp, CircleShape, spotColor = badgeBottomColor.copy(alpha = 0.5f))
          .clip(CircleShape)
          .background(
            brush = Brush.verticalGradient(
              colors = listOf(badgeTopColor, badgeBottomColor)
            )
          )
          .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = daysText,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
    }
  }
}
