package com.example.ui

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NumoBlackPlusButton
import com.example.ui.components.NumoClayHeroCard
import com.example.ui.components.NumoClayIconBadge
import com.example.ui.components.NumoClayProgressBar
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoScreenBackground

@Composable
fun GoalsScreen(
  currency: String = "KES",
  onOpenSavingsGoalDetail: () -> Unit = {},
  onOpenDebtManagement: () -> Unit = {},
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
          text = "Financial Goals",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )

        NumoBlackPlusButton(onClick = onOpenSavingsGoalDetail)
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Hero Emergency Fund Card
      NumoClayHeroCard(
        modifier = Modifier.clickable { onOpenSavingsGoalDetail() }
      ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            NumoClayIconBadge(
              icon = Icons.Default.Spa,
              topColor = Color(0xFF9DFDF6),
              bottomColor = Color(0xFF2CE6D8),
              size = 54.dp,
              iconSize = 28.dp,
              iconTint = NumoDS.HeroTealDark
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Emergency Fund",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = "$currency 30,000 / 100,000",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(modifier = Modifier.weight(1f)) {
              NumoClayProgressBar(
                progress = 0.30f,
                fillTopColor = NumoDS.Cyan,
                fillBottomColor = NumoDS.Turquoise,
                trackColor = Color(0xFF043B32),
                height = 9.dp
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "30%",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 3. My Goals Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "My Goals",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )
        Text(
          text = "Debts >",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = NumoDS.TextSecondary,
          modifier = Modifier.clickable { onOpenDebtManagement() }
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      GoalListCard(
        icon = Icons.Default.DirectionsCar,
        iconTopColor = Color(0xFF38BDF8),
        iconBottomColor = NumoDS.TransportBlue,
        title = "Car Deposit",
        amountText = "$currency 80,000 / 200,000",
        percentText = "40%",
        progress = 0.40f,
        onClick = onOpenSavingsGoalDetail
      )

      Spacer(modifier = Modifier.height(10.dp))

      GoalListCard(
        icon = Icons.Default.Flight,
        iconTopColor = NumoDS.Magenta,
        iconBottomColor = NumoDS.MagentaDeep,
        title = "Holiday",
        amountText = "$currency 50,000 / 100,000",
        percentText = "50%",
        progress = 0.50f,
        onClick = onOpenSavingsGoalDetail
      )

      Spacer(modifier = Modifier.height(10.dp))

      GoalListCard(
        icon = Icons.Default.Terrain,
        iconTopColor = Color(0xFFFBBF24),
        iconBottomColor = NumoDS.DuneOchre,
        title = "Land",
        amountText = "$currency 100,000 / 500,000",
        percentText = "20%",
        progress = 0.20f,
        onClick = onOpenSavingsGoalDetail
      )

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun GoalListCard(
  icon: ImageVector,
  iconTopColor: Color,
  iconBottomColor: Color,
  title: String,
  amountText: String,
  percentText: String,
  progress: Float,
  onClick: () -> Unit
) {
  NumoNeumorphicCard(
    modifier = Modifier.fillMaxWidth(),
    cornerRadius = 18.dp,
    onClick = onClick
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
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )
          Text(
            text = percentText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
          text = amountText,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = NumoDS.TextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        NumoClayProgressBar(
          progress = progress,
          fillTopColor = NumoDS.CyanBright,
          fillBottomColor = NumoDS.HeroTealMid,
          height = 7.dp
        )
      }
    }
  }
}
