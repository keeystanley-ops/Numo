package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
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
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoScreenBackground
import com.example.ui.components.NumoToggleSwitch

@Composable
fun SettingsScreen(
  onBack: () -> Unit = {}
) {
  var budgetMethodEnabled by remember { mutableStateOf(true) }
  var payCycleEnabled by remember { mutableStateOf(true) }
  var themeSystemEnabled by remember { mutableStateOf(true) }
  var notificationsEnabled by remember { mutableStateOf(true) }

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
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = NumoDS.TextPrimary,
          modifier = Modifier
            .size(24.dp)
            .clickable { onBack() }
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
          text = "Settings",
          fontSize = 19.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 2. Currency & Region Section
      Text(
        text = "Currency & Region",
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = NumoDS.TextPrimary
      )

      Spacer(modifier = Modifier.height(10.dp))

      SettingsSelectorCard(
        label = "Currency",
        value = "KES - Kenyan Shilling",
        showInlineChevron = true,
        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward
      )
      Spacer(modifier = Modifier.height(8.dp))

      SettingsSelectorCard(
        label = "Country",
        value = "Kenya",
        showInlineChevron = true,
        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward
      )
      Spacer(modifier = Modifier.height(8.dp))

      SettingsSelectorCard(
        label = "Date Format",
        value = "DD/MM/YYYY",
        showInlineChevron = false,
        trailingIcon = Icons.Default.ArrowDownward
      )

      Spacer(modifier = Modifier.height(18.dp))

      // 3. App Preferences Section
      Text(
        text = "App Preferences",
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = NumoDS.TextPrimary
      )

      Spacer(modifier = Modifier.height(10.dp))

      SettingsToggleCard(
        label = "Budget Method",
        value = "Category Budgets",
        showInlineChevron = true,
        checked = budgetMethodEnabled,
        onCheckedChange = { budgetMethodEnabled = it },
        activeTopColor = NumoDS.BtnTealTop,
        activeBottomColor = NumoDS.BtnTealBottom
      )
      Spacer(modifier = Modifier.height(8.dp))

      SettingsToggleCard(
        label = "Pay Cycle",
        value = "Monthly",
        showInlineChevron = true,
        checked = payCycleEnabled,
        onCheckedChange = { payCycleEnabled = it },
        activeTopColor = NumoDS.TransportBlueLight,
        activeBottomColor = NumoDS.TransportBlue
      )
      Spacer(modifier = Modifier.height(8.dp))

      SettingsToggleCard(
        label = "Theme",
        value = "System",
        showInlineChevron = false,
        checked = themeSystemEnabled,
        onCheckedChange = { themeSystemEnabled = it },
        activeTopColor = Color(0xFF27272A),
        activeBottomColor = NumoDS.Black
      )

      Spacer(modifier = Modifier.height(18.dp))

      // 4. Notifications Section
      Text(
        text = "Notifications",
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = NumoDS.TextPrimary
      )

      Spacer(modifier = Modifier.height(10.dp))

      NumoNeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Enable Notifications",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = NumoDS.TextPrimary
          )
          NumoToggleSwitch(
            checked = notificationsEnabled,
            onCheckedChange = { notificationsEnabled = it },
            activeTopColor = NumoDS.Turquoise,
            activeBottomColor = NumoDS.BtnTealMid
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun SettingsSelectorCard(
  label: String,
  value: String,
  showInlineChevron: Boolean,
  trailingIcon: ImageVector
) {
  NumoNeumorphicCard(
    modifier = Modifier.fillMaxWidth(),
    cornerRadius = 16.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = label,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = NumoDS.TextSecondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )
          if (showInlineChevron) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription = null,
              tint = NumoDS.TextPrimary,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Icon(
        imageVector = trailingIcon,
        contentDescription = null,
        tint = NumoDS.TextPrimary,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

@Composable
private fun SettingsToggleCard(
  label: String,
  value: String,
  showInlineChevron: Boolean,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  activeTopColor: Color,
  activeBottomColor: Color
) {
  NumoNeumorphicCard(
    modifier = Modifier.fillMaxWidth(),
    cornerRadius = 16.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = label,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = NumoDS.TextSecondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )
          if (showInlineChevron) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription = null,
              tint = NumoDS.TextPrimary,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      NumoToggleSwitch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        activeTopColor = activeTopColor,
        activeBottomColor = activeBottomColor
      )
    }
  }
}
