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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.NumoClayHeroCard
import com.example.ui.components.NumoClayIconBadge
import com.example.ui.components.NumoClayProgressBar
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoPrimaryButton
import com.example.ui.components.NumoScreenBackground
import com.example.ui.components.NumoSecondaryButton

@Composable
fun BudgetScreen(
  currency: String = "KES",
  onOpenTransactions: () -> Unit = {},
  onOpenAddTransaction: () -> Unit = {},
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
          text = "Budget Control",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )

        Box(modifier = Modifier.size(40.dp)) {
          Image(
            painter = painterResource(id = R.drawable.img_user_avatar),
            contentDescription = "Avatar",
            modifier = Modifier
              .size(38.dp)
              .shadow(4.dp, CircleShape)
              .clip(CircleShape)
              .border(1.5.dp, Color.White, CircleShape),
            contentScale = ContentScale.Crop
          )
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .offset(x = 2.dp, y = (-2).dp)
              .size(14.dp)
              .clip(CircleShape)
              .background(NumoDS.ExpenseRed),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "1",
              fontSize = 8.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Month Selector Pill
      NumoNeumorphicCard(
        modifier = Modifier
          .fillMaxWidth()
          .height(42.dp),
        cornerRadius = 50.dp
      ) {
        Row(
          modifier = Modifier.fillMaxSize(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "May 2025",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = NumoDS.TextPrimary
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = NumoDS.TextPrimary,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 3. Monthly Budget Hero Card
      NumoClayHeroCard {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Monthly Budget",
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              color = Color.White.copy(alpha = 0.9f)
            )
            Icon(
              imageVector = Icons.Outlined.Wifi,
              contentDescription = null,
              tint = Color.White.copy(alpha = 0.8f),
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "$currency 75,000",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = (-0.5).sp
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "$currency 52,000 spent",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f)
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(modifier = Modifier.weight(1f)) {
              NumoClayProgressBar(
                progress = 0.69f,
                fillTopColor = NumoDS.Cyan,
                fillBottomColor = NumoDS.Turquoise,
                trackColor = Color(0xFF043B32),
                height = 10.dp
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "69%",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "$currency 23,000 remaining",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 4. Edit & Details Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        NumoSecondaryButton(
          text = "Edit",
          onClick = {},
          modifier = Modifier.weight(1f)
        )
        NumoSecondaryButton(
          text = "Details",
          onClick = onOpenTransactions,
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 5. Categories Header
      Text(
        text = "Categories",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = NumoDS.TextPrimary
      )

      Spacer(modifier = Modifier.height(10.dp))

      BudgetControlCategoryRow(
        icon = Icons.Default.Restaurant,
        iconTopColor = Color(0xFFFBBF24),
        iconBottomColor = NumoDS.DuneOchre,
        title = "Food & Groceries",
        percentText = "85%",
        progress = 0.85f,
        barTopColor = Color(0xFFFBBF24),
        barBottomColor = NumoDS.DuneOchre
      )

      Spacer(modifier = Modifier.height(10.dp))

      BudgetControlCategoryRow(
        icon = Icons.Default.DirectionsCar,
        iconTopColor = Color(0xFF0EA5E9),
        iconBottomColor = NumoDS.TransportBlue,
        title = "Transport",
        percentText = "62%",
        progress = 0.62f,
        barTopColor = NumoDS.CyanBright,
        barBottomColor = NumoDS.TransportBlueLight
      )

      Spacer(modifier = Modifier.height(10.dp))

      BudgetControlCategoryRow(
        icon = Icons.Default.ShoppingBag,
        iconTopColor = NumoDS.Magenta,
        iconBottomColor = NumoDS.ExpenseRed,
        title = "Shopping",
        percentText = "110%",
        progress = 0.45f,
        barTopColor = NumoDS.Turquoise,
        barBottomColor = NumoDS.TransportBlue
      )

      Spacer(modifier = Modifier.height(14.dp))

      // 6. + Add Category Primary CTA
      NumoPrimaryButton(
        text = "+ Add Category",
        onClick = onOpenAddTransaction,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun BudgetControlCategoryRow(
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
        .padding(horizontal = 14.dp, vertical = 11.dp),
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

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = NumoDS.TextPrimary
          )
          Text(
            text = percentText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

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
