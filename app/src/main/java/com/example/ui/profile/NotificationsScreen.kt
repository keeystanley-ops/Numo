package com.example.ui.profile

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NumoClayIconBadge
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoFloatingDecorations
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoScreenBackground

@Composable
fun NotificationsScreen(
  onBack: () -> Unit
) {
  var transactionAlerts by remember { mutableStateOf(true) }
  var billReminders by remember { mutableStateOf(true) }
  var budgetWarnings by remember { mutableStateOf(true) }
  var savingsUpdates by remember { mutableStateOf(true) }
  var investmentNotifications by remember { mutableStateOf(true) }

  NumoScreenBackground {
    NumoFloatingDecorations(variant = 2)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 22.dp)
    ) {
      Spacer(modifier = Modifier.height(6.dp))

      ProfileSubScreenTopBar(
        title = "Notifications",
        onBack = onBack,
        showBackLabel = true
      )

      Spacer(modifier = Modifier.height(14.dp))

      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NotificationToggleRow(
          title = "Transaction Alerts",
          icon = Icons.Default.Notifications,
          badgeTop = Color(0xFFFF6EC7),
          badgeBottom = Color(0xFFBA0D6B),
          checked = transactionAlerts,
          onCheckedChange = { transactionAlerts = it },
          toggleTop = Color(0xFFD91E85),
          toggleBottom = Color(0xFF8C0D56)
        )

        NotificationToggleRow(
          title = "Bill Reminders",
          icon = Icons.Default.ReceiptLong,
          badgeTop = Color(0xFF56F6EC),
          badgeBottom = NumoDS.HeroTealDark,
          checked = billReminders,
          onCheckedChange = { billReminders = it },
          toggleTop = NumoDS.CyanBright,
          toggleBottom = NumoDS.HeroTealMid
        )

        NotificationToggleRow(
          title = "Budget Warnings",
          icon = Icons.Default.Assessment,
          badgeTop = Color(0xFFFCD34D),
          badgeBottom = NumoDS.DuneOchre,
          checked = budgetWarnings,
          onCheckedChange = { budgetWarnings = it },
          toggleTop = Color(0xFFD91E85),
          toggleBottom = Color(0xFF8C0D56)
        )

        NotificationToggleRow(
          title = "Savings Updates",
          icon = Icons.Default.Savings,
          badgeTop = Color(0xFF5EEAD4),
          badgeBottom = NumoDS.Turquoise,
          checked = savingsUpdates,
          onCheckedChange = { savingsUpdates = it },
          toggleTop = Color(0xFFD91E85),
          toggleBottom = Color(0xFF8C0D56)
        )

        NotificationToggleRow(
          title = "Investment Notifications",
          icon = Icons.Default.TrendingUp,
          badgeTop = Color(0xFF60A5FA),
          badgeBottom = Color(0xFF2563EB),
          checked = investmentNotifications,
          onCheckedChange = { investmentNotifications = it },
          toggleTop = NumoDS.CyanBright,
          toggleBottom = NumoDS.HeroTealMid
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun NotificationToggleRow(
  title: String,
  icon: ImageVector,
  badgeTop: Color,
  badgeBottom: Color,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  toggleTop: Color,
  toggleBottom: Color
) {
  NumoNeumorphicCard(
    modifier = Modifier.fillMaxWidth(),
    cornerRadius = 18.dp,
    onClick = { onCheckedChange(!checked) }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 13.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      NumoClayIconBadge(
        icon = icon,
        topColor = badgeTop,
        bottomColor = badgeBottom,
        size = 36.dp,
        iconSize = 18.dp
      )

      Spacer(modifier = Modifier.width(14.dp))

      Text(
        text = title,
        fontSize = 14.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = NumoDS.TextPrimary,
        modifier = Modifier.weight(1f)
      )

      NumoPillToggle(
        checked = checked,
        onCheckedChange = onCheckedChange,
        activeColorTop = toggleTop,
        activeColorBottom = toggleBottom
      )
    }
  }
}
