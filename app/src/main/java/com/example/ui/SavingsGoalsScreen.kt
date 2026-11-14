package com.example.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
fun SavingsGoalsScreen(
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
          text = "Savings Goals",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )

        NumoBlackPlusButton(onClick = onBack)
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Hero Emergency Fund Card
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

      // 3. Your Progress Header
      Text(
        text = "Your Progress",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = NumoDS.TextPrimary
      )

      Spacer(modifier = Modifier.height(10.dp))

      NumoNeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 22.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier.size(106.dp),
            contentAlignment = Alignment.Center
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val stroke = 15.dp.toPx()
              drawArc(
                color = NumoDS.TrackBg,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
              )
              drawArc(
                brush = Brush.verticalGradient(
                  colors = listOf(NumoDS.Turquoise, NumoDS.HeroTealDark)
                ),
                startAngle = -90f,
                sweepAngle = 108f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
              )
            }
            Text(
              text = "30%",
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              color = NumoDS.TextPrimary
            )
          }

          Spacer(modifier = Modifier.width(22.dp))

          Column {
            Text(
              text = "$currency 70,000",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = NumoDS.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "remaining",
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              color = NumoDS.TextSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 4. Add Contribution CTA
      NumoPrimaryButton(
        text = "Add Contribution",
        onClick = {},
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(18.dp))

      // 5. Goals Summary
      Text(
        text = "Goals Summary",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = NumoDS.TextPrimary
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        NumoNeumorphicCard(
          modifier = Modifier.weight(1f),
          cornerRadius = 18.dp
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "$currency 8,000",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = NumoDS.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "This Month",
              fontSize = 12.sp,
              color = NumoDS.TextSecondary
            )
          }
        }

        NumoNeumorphicCard(
          modifier = Modifier.weight(1f),
          cornerRadius = 18.dp
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "$currency 45,000",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = NumoDS.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Total Saved",
              fontSize = 12.sp,
              color = NumoDS.TextSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
