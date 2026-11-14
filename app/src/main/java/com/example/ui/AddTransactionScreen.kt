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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
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
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoPrimaryButton
import com.example.ui.components.NumoScreenBackground
import com.example.ui.components.NumoSecondaryButton
import com.example.ui.components.NumoSegmentedControl

@Composable
fun AddTransactionScreen(
  currency: String = "KES",
  onCancel: () -> Unit = {},
  onSave: () -> Unit = {}
) {
  var typeIndex by remember { mutableIntStateOf(0) } // 0: Expense, 1: Income, 2: Transfer
  var selectedCategoryIndex by remember { mutableIntStateOf(1) } // 1: Transport

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
            .clickable { onCancel() }
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
          text = "Add Transaction",
          fontSize = 19.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Expense / Income / Transfer Segmented Control
      NumoSegmentedControl(
        options = listOf("Expense", "Income", "Transfer"),
        selectedIndex = typeIndex,
        onSelect = { typeIndex = it },
        activeTopColor = NumoDS.MagentaMid,
        activeBottomColor = NumoDS.MagentaDeep
      )

      Spacer(modifier = Modifier.height(14.dp))

      // 3. Amount Card
      NumoNeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "$currency 0",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )

          Box(
            modifier = Modifier
              .size(44.dp)
              .shadow(6.dp, RoundedCornerShape(12.dp), spotColor = NumoDS.DuneOchre.copy(alpha = 0.5f))
              .clip(RoundedCornerShape(12.dp))
              .background(
                brush = Brush.verticalGradient(
                  colors = listOf(Color(0xFFFCD34D), NumoDS.DuneOchre)
                )
              )
              .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Calculate,
              contentDescription = "Keypad",
              tint = Color(0xFF422006),
              modifier = Modifier.size(24.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 4. Category Selector Card
      NumoNeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp),
          horizontalArrangement = Arrangement.SpaceAround,
          verticalAlignment = Alignment.CenterVertically
        ) {
          AddTxCategoryItem(
            icon = Icons.Default.Restaurant,
            label = "Food",
            topColor = Color(0xFFFBBF24),
            bottomColor = NumoDS.DuneOchre,
            labelColor = NumoDS.TextPrimary,
            isSelected = selectedCategoryIndex == 0,
            onClick = { selectedCategoryIndex = 0 }
          )
          AddTxCategoryItem(
            icon = Icons.Default.DirectionsCar,
            label = "Transport",
            topColor = Color(0xFF0EA5E9),
            bottomColor = NumoDS.TransportBlue,
            labelColor = NumoDS.TransportBlue,
            isSelected = selectedCategoryIndex == 1,
            onClick = { selectedCategoryIndex = 1 }
          )
          AddTxCategoryItem(
            icon = Icons.Default.ShoppingBag,
            label = "Shopping",
            topColor = Color(0xFFF43F5E),
            bottomColor = NumoDS.ExpenseRed,
            labelColor = NumoDS.ExpenseRed,
            isSelected = selectedCategoryIndex == 2,
            onClick = { selectedCategoryIndex = 2 }
          )
          AddTxCategoryItem(
            icon = Icons.Default.Favorite,
            label = "Health",
            topColor = Color(0xFF27272A),
            bottomColor = NumoDS.Black,
            labelColor = NumoDS.TextPrimary,
            isSelected = selectedCategoryIndex == 3,
            onClick = { selectedCategoryIndex = 3 }
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 5. Form Field Cards
      AddTxFieldCard(label = "Account", value = "Select account")
      Spacer(modifier = Modifier.height(10.dp))
      AddTxFieldCard(label = "Date", value = "Today, May 18, 2025")
      Spacer(modifier = Modifier.height(10.dp))
      AddTxFieldCard(label = "Description", value = "Add description...")

      Spacer(modifier = Modifier.height(18.dp))

      // 6. Cancel & Save Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        NumoSecondaryButton(
          text = "Cancel",
          onClick = onCancel,
          modifier = Modifier.weight(1f),
          height = 48.dp
        )
        NumoPrimaryButton(
          text = "Save",
          onClick = onSave,
          modifier = Modifier.weight(1f),
          height = 48.dp
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun AddTxCategoryItem(
  icon: ImageVector,
  label: String,
  topColor: Color,
  bottomColor: Color,
  labelColor: Color,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(14.dp))
      .then(
        if (isSelected) {
          Modifier
            .background(Color(0xFFE0F2FE).copy(alpha = 0.7f))
            .border(1.dp, Color.White, RoundedCornerShape(14.dp))
        } else {
          Modifier
        }
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    NumoClayIconBadge(
      icon = icon,
      topColor = topColor,
      bottomColor = bottomColor,
      size = 42.dp,
      iconSize = 21.dp
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      color = labelColor
    )
  }
}

@Composable
private fun AddTxFieldCard(
  label: String,
  value: String
) {
  NumoNeumorphicCard(
    modifier = Modifier.fillMaxWidth(),
    cornerRadius = 18.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = label,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = value,
          fontSize = 13.sp,
          color = NumoDS.TextMuted
        )
      }

      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = null,
        tint = NumoDS.TextPrimary,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}
