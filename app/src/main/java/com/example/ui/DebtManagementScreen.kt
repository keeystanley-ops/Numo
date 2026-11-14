package com.example.ui

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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Spa
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
import com.example.ui.components.NumoPrimaryButton
import com.example.ui.components.NumoScreenBackground

@Composable
fun DebtManagementScreen(
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
          text = "Debt Management",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )

        NumoBlackPlusButton(onClick = onBack)
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Hero HELB Loan Card
      NumoClayHeroCard {
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

            Column {
              Text(
                text = "HELB Loan",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.95f)
              )
              Text(
                text = "$currency 80,000",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "12% interest",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.88f)
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
                progress = 0.60f,
                fillTopColor = NumoDS.Cyan,
                fillBottomColor = NumoDS.Turquoise,
                trackColor = Color(0xFF043B32),
                height = 9.dp
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "60%",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 3. My Debts Header
      Text(
        text = "My Debts",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = NumoDS.TextPrimary
      )

      Spacer(modifier = Modifier.height(12.dp))

      DebtListCard(
        icon = Icons.Default.AccountBalance,
        iconTopColor = Color(0xFFA855F7),
        iconBottomColor = NumoDS.PurpleAccent,
        title = "HELB Loan",
        amountText = "$currency 80,000",
        percentText = "60%",
        progress = 0.60f,
        barTopColor = NumoDS.Turquoise,
        barBottomColor = NumoDS.HeroTealMid
      )

      Spacer(modifier = Modifier.height(12.dp))

      DebtListCard(
        icon = Icons.Default.AccountBalance,
        iconTopColor = Color(0xFFF43F5E),
        iconBottomColor = NumoDS.ExpenseRed,
        title = "Bank Loan",
        amountText = "$currency 120,000",
        percentText = "40%",
        progress = 0.40f,
        barTopColor = NumoDS.Magenta,
        barBottomColor = NumoDS.MagentaDeep
      )

      Spacer(modifier = Modifier.height(18.dp))

      // 4. View Strategy CTA
      NumoPrimaryButton(
        text = "View Strategy",
        onClick = {},
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun DebtListCard(
  icon: ImageVector,
  iconTopColor: Color,
  iconBottomColor: Color,
  title: String,
  amountText: String,
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
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = amountText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )
          Spacer(modifier = Modifier.width(12.dp))
          Box(modifier = Modifier.weight(1f)) {
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
  }
}
