package com.example.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NumoClayIconBadge
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoFloatingDecorations
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoPrimaryButton
import com.example.ui.components.NumoScreenBackground

private data class NumoCategoryCardItem(
  val title: String,
  val icon: ImageVector,
  val topColor: Color,
  val bottomColor: Color
)

@Composable
fun CategoriesScreen(
  onBack: () -> Unit
) {
  var selectedTab by remember { mutableStateOf("Expense") }

  val expenseCategories = remember {
    mutableStateListOf(
      NumoCategoryCardItem("Shopping", Icons.Default.ShoppingBag, Color(0xFFFF78C4), NumoDS.Magenta),
      NumoCategoryCardItem("Food & Dining", Icons.Default.Fastfood, Color(0xFFFCD34D), Color(0xFFD97706)),
      NumoCategoryCardItem("Transport", Icons.Default.DirectionsBus, Color(0xFFFBBF24), Color(0xFFB45309)),
      NumoCategoryCardItem("Bills", Icons.Default.Receipt, Color(0xFF93C5FD), Color(0xFF2563EB))
    )
  }

  val incomeCategories = remember {
    mutableStateListOf(
      NumoCategoryCardItem("Salary", Icons.Default.Payments, Color(0xFF5EEAD4), NumoDS.HeroTealDark),
      NumoCategoryCardItem("Freelance", Icons.Default.Work, Color(0xFF93C5FD), Color(0xFF2563EB)),
      NumoCategoryCardItem("Investments", Icons.Default.TrendingUp, Color(0xFFFCD34D), NumoDS.DuneOchre),
      NumoCategoryCardItem("Gifts", Icons.Default.CardGiftcard, Color(0xFFFF78C4), NumoDS.Magenta)
    )
  }

  var showAddDialog by remember { mutableStateOf(false) }
  var customCategoryName by remember { mutableStateOf("") }

  val displayed = if (selectedTab == "Expense") expenseCategories else incomeCategories

  NumoScreenBackground {
    NumoFloatingDecorations(variant = 2)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .padding(horizontal = 22.dp)
    ) {
      Spacer(modifier = Modifier.height(6.dp))

      ProfileSubScreenTopBar(
        title = "Categories",
        onBack = onBack,
        showBackLabel = true
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Segmented Pill: Income | Expense
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp)
          .clip(RoundedCornerShape(50))
          .background(Color(0xFFEBDDD0))
          .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(50))
          .padding(4.dp)
      ) {
        Row(modifier = Modifier.fillMaxSize()) {
          listOf("Income", "Expense").forEach { tab ->
            val isSelected = selectedTab == tab
            Box(
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .then(
                  if (isSelected) {
                    Modifier
                      .shadow(4.dp, RoundedCornerShape(50))
                      .clip(RoundedCornerShape(50))
                      .background(Color(0xFFFFFBF7))
                  } else {
                    Modifier.clip(RoundedCornerShape(50))
                  }
                )
                .clickable { selectedTab = tab },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = tab,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) NumoDS.TextPrimary else NumoDS.TextSecondary
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2-column grid of category cards
      Column(
        modifier = Modifier
          .weight(1f)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        displayed.chunked(2).forEach { rowItems ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            rowItems.forEach { cat ->
              NumoNeumorphicCard(
                modifier = Modifier
                  .weight(1f)
                  .aspectRatio(1.12f),
                cornerRadius = 20.dp
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center
                ) {
                  NumoClayIconBadge(
                    icon = cat.icon,
                    topColor = cat.topColor,
                    bottomColor = cat.bottomColor,
                    size = 54.dp,
                    iconSize = 28.dp
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                  Text(
                    text = cat.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NumoDS.TextPrimary,
                    textAlign = TextAlign.Center
                  )
                }
              }
            }
            if (rowItems.size == 1) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
      }

      Spacer(modifier = Modifier.height(10.dp))

      NumoPrimaryButton(
        text = "+ Add Custom Category",
        onClick = { showAddDialog = true },
        modifier = Modifier.fillMaxWidth(),
        height = 50.dp
      )

      Spacer(modifier = Modifier.height(16.dp))
    }

    if (showAddDialog) {
      AlertDialog(
        onDismissRequest = { showAddDialog = false },
        containerColor = Color(0xFFFFF6EC),
        title = {
          Text(
            text = "Add $selectedTab Category",
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )
        },
        text = {
          ProfileRecessedInput(
            label = "Category Name",
            value = customCategoryName,
            onValueChange = { customCategoryName = it },
            placeholder = "e.g. Health & Fitness"
          )
        },
        confirmButton = {
          TextButton(
            onClick = {
              val clean = customCategoryName.trim()
              if (clean.isNotEmpty()) {
                val item = NumoCategoryCardItem(
                  title = clean,
                  icon = if (selectedTab == "Expense") Icons.Default.ShoppingBag else Icons.Default.Payments,
                  topColor = NumoDS.CyanBright,
                  bottomColor = NumoDS.HeroTealMid
                )
                if (selectedTab == "Expense") expenseCategories.add(item) else incomeCategories.add(item)
                customCategoryName = ""
                showAddDialog = false
              }
            }
          ) {
            Text("Add", fontWeight = FontWeight.Bold, color = NumoDS.Turquoise)
          }
        },
        dismissButton = {
          TextButton(onClick = { showAddDialog = false }) {
            Text("Cancel", color = NumoDS.TextSecondary)
          }
        }
      )
    }
  }
}
