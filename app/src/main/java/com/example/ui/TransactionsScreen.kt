package com.example.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Hexagon
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NumoClayIconBadge
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoScreenBackground
import com.example.ui.components.NumoSegmentedControl

@Composable
fun TransactionsScreen(
  currency: String = "KES",
  onAddTransaction: () -> Unit = {},
  onBack: () -> Unit = {}
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Income, 2: Expenses, 3: Transfers

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
          text = "Transactions",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )

        Box(
          modifier = Modifier
            .size(36.dp)
            .clickable { onAddTransaction() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Hexagon,
            contentDescription = "Options",
            tint = NumoDS.TextPrimary,
            modifier = Modifier.size(26.dp)
          )
          Box(
            modifier = Modifier
              .size(12.dp)
              .clip(CircleShape)
              .background(NumoDS.PeachBgTop)
          )
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .offset(x = (-1).dp, y = 2.dp)
              .size(13.dp)
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

      // 2. Search Bar + Filter Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        NumoNeumorphicCard(
          modifier = Modifier
            .weight(1f)
            .height(42.dp),
          cornerRadius = 50.dp
        ) {
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = NumoDS.TextMuted,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Search transactions...",
              fontSize = 13.sp,
              color = NumoDS.TextMuted
            )
          }
        }

        Spacer(modifier = Modifier.width(10.dp))

        NumoNeumorphicCard(
          modifier = Modifier.size(42.dp),
          cornerRadius = 50.dp
        ) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.FilterList,
              contentDescription = "Filter",
              tint = NumoDS.TextPrimary,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 3. 4-Way Segmented Filter
      NumoSegmentedControl(
        options = listOf("All", "Income", "Expenses", "Transfers"),
        selectedIndex = selectedTab,
        onSelect = { selectedTab = it }
      )

      Spacer(modifier = Modifier.height(14.dp))

      // 4. Transaction List Items
      TransactionRowCard(
        icon = Icons.Default.Restaurant,
        iconTopColor = Color(0xFFFBBF24),
        iconBottomColor = NumoDS.DuneOchre,
        title = "Supermarket",
        category = "Food & Groceries",
        amount = "- $currency 2,500",
        amountColor = NumoDS.ExpenseRed,
        date = "Today"
      )

      Spacer(modifier = Modifier.height(10.dp))

      TransactionRowCard(
        icon = Icons.Default.Person,
        iconTopColor = Color(0xFF4ADE80),
        iconBottomColor = NumoDS.SuccessGreen,
        title = "Salary",
        category = "Income",
        amount = "+ $currency 75,000",
        amountColor = NumoDS.SuccessGreen,
        date = "Today"
      )

      Spacer(modifier = Modifier.height(10.dp))

      TransactionRowCard(
        icon = Icons.Default.DirectionsCar,
        iconTopColor = Color(0xFF0EA5E9),
        iconBottomColor = NumoDS.TransportBlue,
        title = "Matatu",
        category = "Transport",
        amount = "- $currency 200",
        amountColor = NumoDS.ExpenseRed,
        date = "Yesterday"
      )

      Spacer(modifier = Modifier.height(10.dp))

      TransactionRowCard(
        icon = Icons.Default.PhoneAndroid,
        iconTopColor = Color(0xFFF87171),
        iconBottomColor = NumoDS.ExpenseRed,
        title = "Airtime",
        category = "Communication",
        amount = "- $currency 200",
        amountColor = NumoDS.ExpenseRed,
        date = "Yesterday"
      )

      Spacer(modifier = Modifier.height(10.dp))

      TransactionRowCard(
        icon = Icons.Default.PhoneAndroid,
        iconTopColor = Color(0xFFFB923C),
        iconBottomColor = NumoDS.OrangeBright,
        title = "Airtime",
        category = "Communication",
        amount = "- $currency 300",
        amountColor = NumoDS.ExpenseRed,
        date = "Yesterday"
      )

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun TransactionRowCard(
  icon: ImageVector,
  iconTopColor: Color,
  iconBottomColor: Color,
  title: String,
  category: String,
  amount: String,
  amountColor: Color,
  date: String
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
        Text(
          text = title,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = category,
          fontSize = 12.sp,
          color = NumoDS.TextSecondary
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = amount,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = amountColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = date,
          fontSize = 11.sp,
          color = NumoDS.TextMuted
        )
      }
    }
  }
}
