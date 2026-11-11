package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.animation.TopScrollProgressBar
import com.example.ui.animation.animateIntCount
import com.example.ui.animation.animateProgress
import com.example.ui.animation.bouncyClickable
import com.example.ui.animation.formatAmount
import com.example.ui.animation.staggeredEntrance

import com.example.data.NumoFinancialEngine

/**
 * Budget Screen matching Screen 6 & Transactions from the provided reference mockup.
 * Features:
 * - Top header with settings action
 * - Sub-view selector: [Budget Overview] and [Transactions]
 * - Monthly Budget Card (KES 120,000, KES 78,420 spent, KES 41,580 remaining) with interactive Edit modal
 * - Category Limits with animated progress bars (Food & Dining 70%, Transport 60%, Shopping 50%)
 * - Overspending Alert Banner ("Food is over by KES 1,500. Tap to rebalance") with action dialog
 * - Integrated Transactions view (Screen 4) with [All | Income | Expenses] pill tabs
 * - Add Transaction dialog (Screen 5) with category grid and amount
 */
@Composable
fun BudgetScreen(
  currency: String = "KES",
  onBack: () -> Unit = {}
) {
  var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Budget, 1: Transactions
  var showEditBudgetDialog by remember { mutableStateOf(false) }
  var showRebalanceDialog by remember { mutableStateOf(false) }
  var showAddTransactionDialog by remember { mutableStateOf(false) }
  var showSettingsDialog by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  // Financial values pulled live from NumoFinancialEngine
  val monthlyBudget = NumoFinancialEngine.monthlyBudgetTarget.toInt()
  val spentAmount = NumoFinancialEngine.totalSpent.toInt()
  val remainingAmount = NumoFinancialEngine.totalRemaining.toInt()

  val animatedBudget = animateIntCount(monthlyBudget, durationMs = 900)
  val animatedSpent = animateIntCount(spentAmount, durationMs = 850)
  val animatedRemaining = animateIntCount(remainingAmount, durationMs = 950)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAF9))
  ) {
    // Top Scroll Progress Bar
    TopScrollProgressBar(scrollState = scrollState)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      // Top Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (selectedSubTab == 0) "Budget" else "Transactions",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A),
          modifier = Modifier.testTag("budget_title")
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (selectedSubTab == 1) {
            IconButton(
              onClick = { showAddTransactionDialog = true },
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F8F2))
                .bouncyClickable { showAddTransactionDialog = true }
                .testTag("btn_add_transaction_header")
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Transaction",
                tint = Color(0xFF02844E),
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
          }

          IconButton(
            onClick = {
              if (selectedSubTab == 1) {
                // Search or toggle back
                selectedSubTab = 0
              } else {
                showSettingsDialog = true
              }
            },
            modifier = Modifier
              .size(36.dp)
              .testTag("budget_header_action")
          ) {
            Icon(
              imageVector = if (selectedSubTab == 1) Icons.Outlined.Search else Icons.Outlined.Settings,
              contentDescription = if (selectedSubTab == 1) "Search" else "Settings",
              tint = Color(0xFF334155),
              modifier = Modifier.size(22.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Segmented Pill Toggle: [Budget Overview | Transactions]
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(24.dp))
          .background(Color(0xFFEDF2F7))
          .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(if (selectedSubTab == 0) Color(0xFF02844E) else Color.Transparent)
            .bouncyClickable { selectedSubTab = 0 }
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Budget Overview",
            fontSize = 13.sp,
            fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Medium,
            color = if (selectedSubTab == 0) Color.White else Color(0xFF64748B)
          )
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(if (selectedSubTab == 1) Color(0xFF02844E) else Color.Transparent)
            .bouncyClickable { selectedSubTab = 1 }
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Transactions",
            fontSize = 13.sp,
            fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Medium,
            color = if (selectedSubTab == 1) Color.White else Color(0xFF64748B)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (selectedSubTab == 0) {
        // ==========================================
        // 1. MONTHLY BUDGET HERO CARD
        // ==========================================
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(delayMs = 40)
            .shadow(
              elevation = 10.dp,
              shape = RoundedCornerShape(24.dp),
              spotColor = Color(0xFF02844E).copy(alpha = 0.25f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
              Brush.linearGradient(
                colors = listOf(
                  Color(0xFF016339),
                  Color(0xFF02844E),
                  Color(0xFF059669)
                )
              )
            )
            .padding(22.dp)
        ) {
          Column {
            // Top Row: "Monthly Budget" and "[Edit]" pill button
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

              // Edit pill
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color.White.copy(alpha = 0.18f))
                  .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                  .bouncyClickable { showEditBudgetDialog = true }
                  .padding(horizontal = 14.dp, vertical = 6.dp)
                  .testTag("btn_edit_budget"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "Edit",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color.White
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Large amount
            Text(
              text = "$currency ${formatAmount(animatedBudget)}",
              fontSize = 28.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              letterSpacing = (-0.5).sp,
              modifier = Modifier.testTag("txt_monthly_budget_amount")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // White progress bar
            val budgetProgress = (spentAmount.toFloat() / monthlyBudget.toFloat()).coerceIn(0f, 1f)
            val animatedBar = animateProgress(targetProgress = budgetProgress, durationMs = 1100)

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f))
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth(animatedBar)
                  .height(6.dp)
                  .clip(CircleShape)
                  .background(Color.White)
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom spent and remaining amounts
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "$currency ${formatAmount(animatedSpent)} spent",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
              )
              Text(
                text = "$currency ${formatAmount(animatedRemaining)} remaining",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 2. CATEGORY LIMITS SECTION
        // ==========================================
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Category Limits",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .bouncyClickable { selectedSubTab = 1 }
              .padding(4.dp)
          ) {
            Text(
              text = "See All",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF02844E)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
              imageVector = Icons.Default.ArrowForward,
              contentDescription = "See All",
              tint = Color(0xFF02844E),
              modifier = Modifier.size(14.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Category Limit Cards dynamically driven by NumoFinancialEngine
        NumoFinancialEngine.categories.take(3).forEachIndexed { index, cat ->
          CategoryLimitItem(
            title = cat.name,
            limitAmount = cat.limit.toInt(),
            percentage = cat.percentage,
            currency = currency,
            icon = when (cat.iconName) {
              "Car" -> Icons.Default.DirectionsCar
              "Bag" -> Icons.Default.ShoppingBag
              else -> Icons.Default.Restaurant
            },
            iconColor = Color(cat.colorHex),
            iconBg = Color(cat.colorHex).copy(alpha = 0.15f),
            barColor = Color(cat.colorHex),
            delayMs = 80 + index * 40
          )
          Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ==========================================
        // 3. OVERSPENDING ALERT BANNER (Screen 6)
        // ==========================================
        if (NumoFinancialEngine.isOverspendAlertActive) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .staggeredEntrance(delayMs = 200)
              .clip(RoundedCornerShape(16.dp))
              .background(Color(0xFFFFF1F2))
              .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(16.dp))
              .bouncyClickable { showRebalanceDialog = true }
              .padding(16.dp)
              .testTag("card_overspend_alert")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Warning,
                  contentDescription = "Warning",
                  tint = Color(0xFFE11D48),
                  modifier = Modifier.size(20.dp)
                )
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Food is over by $currency ${formatAmount(NumoFinancialEngine.overspendAmount.toInt())}",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFE11D48)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Tap to rebalance",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = Color(0xFFBE123C)
                )
              }

              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Rebalance",
                tint = Color(0xFFE11D48),
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick button to view transactions or add new transaction
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Button(
            onClick = { selectedSubTab = 1 },
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("btn_view_transactions"),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFFE8F8F2),
              contentColor = Color(0xFF02844E)
            ),
            shape = RoundedCornerShape(14.dp)
          ) {
            Text(
              text = "Transactions List",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Button(
            onClick = { showAddTransactionDialog = true },
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("btn_add_transaction_quick"),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF02844E),
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Add",
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Add Expense",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      } else {
        // ==========================================
        // TRANSACTIONS VIEW (Screen 4)
        // ==========================================
        TransactionsContent(
          currency = currency,
          onAddNew = { showAddTransactionDialog = true }
        )
      }

      Spacer(modifier = Modifier.height(80.dp))
    }
  }

  // ==========================================
  // DIALOGS & MODALS
  // ==========================================

  // 1. Edit Monthly Budget Dialog
  if (showEditBudgetDialog) {
    EditBudgetDialog(
      currentBudget = monthlyBudget,
      currency = currency,
      onDismiss = { showEditBudgetDialog = false },
      onSave = { newAmount ->
        monthlyBudget = newAmount
        showEditBudgetDialog = false
      }
    )
  }

  // 2. Rebalance Alert Dialog
  if (showRebalanceDialog) {
    RebalanceBudgetDialog(
      currency = currency,
      onDismiss = { showRebalanceDialog = false },
      onConfirm = {
        showRebalanceDialog = false
      }
    )
  }

  // 3. Add Transaction Dialog (Screen 5)
  if (showAddTransactionDialog) {
    AddTransactionDialog(
      currency = currency,
      onDismiss = { showAddTransactionDialog = false },
      onAdd = { _, _, _ ->
        showAddTransactionDialog = false
      }
    )
  }

  // 4. Budget Settings Dialog
  if (showSettingsDialog) {
    BudgetSettingsDialog(
      onDismiss = { showSettingsDialog = false }
    )
  }
}

/**
 * Individual Category Limit Item matching Screen 6
 */
@Composable
fun CategoryLimitItem(
  title: String,
  limitAmount: Int,
  percentage: Int,
  currency: String,
  icon: ImageVector,
  iconColor: Color,
  iconBg: Color,
  barColor: Color,
  delayMs: Int
) {
  val animatedProgress = animateProgress(
    targetProgress = percentage / 100f,
    durationMs = 900
  )

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .staggeredEntrance(delayMs = delayMs),
    color = Color.White,
    shape = RoundedCornerShape(18.dp),
    shadowElevation = 1.dp
  ) {
    Column(
      modifier = Modifier.padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(iconBg),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "$currency ${formatAmount(limitAmount)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B)
          )
        }

        Text(
          text = "$percentage%",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Linear Progress Bar
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(CircleShape)
          .background(Color(0xFFF1F5F9))
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth(animatedProgress)
            .height(6.dp)
            .clip(CircleShape)
            .background(barColor)
        )
      }
    }
  }
}

/**
 * Transactions Screen Content matching Screen 4 of the reference mockup:
 * - Pill Filters: [All | Income | Expenses]
 * - Group "Today":
 *   - M-PESA Top Up (+KES 2,000, 09:24 AM)
 *   - Groceries (-KES 1,250, 08:45 AM)
 *   - Uber Ride (-KES 320, 08:12 AM)
 * - Group "Yesterday":
 *   - Salary (+KES 120,000, Thu, Apr 24)
 *   - Electricity Bill (-KES 3,200, Thu, Apr 24)
 */
@Composable
fun TransactionsContent(
  currency: String = "KES",
  onAddNew: () -> Unit = {}
) {
  var selectedFilter by remember { mutableIntStateOf(0) } // 0: All, 1: Income, 2: Expenses

  Column(modifier = Modifier.fillMaxWidth()) {
    // Pill Filters
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(Color(0xFFF1F5F9))
        .padding(4.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      listOf("All", "Income", "Expenses").forEachIndexed { index, label ->
        val isSelected = (selectedFilter == index)
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFF02844E) else Color.Transparent)
            .bouncyClickable { selectedFilter = index }
            .padding(vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF64748B)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // TODAY SECTION
    if (selectedFilter == 0 || selectedFilter == 1 || selectedFilter == 2) {
      Text(
        text = "Today",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF64748B),
        modifier = Modifier.padding(bottom = 10.dp)
      )

      // 1. M-PESA Top Up (+2,000)
      if (selectedFilter != 2) {
        TransactionRowItem(
          title = "M-PESA Top Up",
          time = "09:24 AM",
          amount = "+$currency 2,000",
          isPositive = true,
          icon = Icons.Default.LocalAtm,
          iconBg = Color(0xFFE8F8F2),
          iconTint = Color(0xFF02844E)
        )
        Spacer(modifier = Modifier.height(10.dp))
      }

      // 2. Groceries (-1,250)
      if (selectedFilter != 1) {
        TransactionRowItem(
          title = "Groceries",
          time = "08:45 AM",
          amount = "-$currency 1,250",
          isPositive = false,
          icon = Icons.Default.Fastfood,
          iconBg = Color(0xFFFEE2E2),
          iconTint = Color(0xFFEF4444)
        )
        Spacer(modifier = Modifier.height(10.dp))
      }

      // 3. Uber Ride (-320)
      if (selectedFilter != 1) {
        TransactionRowItem(
          title = "Uber Ride",
          time = "08:12 AM",
          amount = "-$currency 320",
          isPositive = false,
          icon = Icons.Default.DirectionsCar,
          iconBg = Color(0xFF0F172A),
          iconTint = Color.White
        )
        Spacer(modifier = Modifier.height(20.dp))
      }
    }

    // YESTERDAY SECTION
    if (selectedFilter == 0 || selectedFilter == 1 || selectedFilter == 2) {
      Text(
        text = "Yesterday",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF64748B),
        modifier = Modifier.padding(bottom = 10.dp)
      )

      // 1. Salary (+120,000)
      if (selectedFilter != 2) {
        TransactionRowItem(
          title = "Salary",
          time = "Thu, Apr 24",
          amount = "+$currency 120,000",
          isPositive = true,
          icon = Icons.Default.Work,
          iconBg = Color(0xFFE8F8F2),
          iconTint = Color(0xFF02844E)
        )
        Spacer(modifier = Modifier.height(10.dp))
      }

      // 2. Electricity Bill (-3,200)
      if (selectedFilter != 1) {
        TransactionRowItem(
          title = "Electricity Bill",
          time = "Thu, Apr 24",
          amount = "-$currency 3,200",
          isPositive = false,
          icon = Icons.Default.ElectricBolt,
          iconBg = Color(0xFFEFF6FF),
          iconTint = Color(0xFF2563EB)
        )
      }
    }
  }
}

@Composable
fun TransactionRowItem(
  title: String,
  time: String,
  amount: String,
  isPositive: Boolean,
  icon: ImageVector,
  iconBg: Color,
  iconTint: Color
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    color = Color.White,
    shape = RoundedCornerShape(16.dp),
    shadowElevation = 0.5.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(iconBg),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = iconTint,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = time,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFF64748B)
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = amount,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = if (isPositive) Color(0xFF02844E) else Color(0xFFEF4444)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = time,
          fontSize = 11.sp,
          color = Color(0xFF94A3B8)
        )
      }
    }
  }
}

/**
 * Add Transaction Dialog matching Screen 5 from user mockup:
 * - Expense / Income toggle
 * - Large amount "KES 0.00"
 * - 3x3 category grid: Food, Transport, Shopping, Health, Bills, Entertainment, Other, Transfer, More
 * - Add Note (Optional)
 * - Big green "Add Transaction" button
 */
@Composable
fun AddTransactionDialog(
  currency: String = "KES",
  onDismiss: () -> Unit,
  onAdd: (Double, String, String) -> Unit
) {
  var isExpense by remember { mutableStateOf(true) }
  var amountInput by remember { mutableStateOf("0.00") }
  var selectedCategory by remember { mutableStateOf("Food") }
  var noteInput by remember { mutableStateOf("") }

  val categories = listOf(
    Triple("Food", Icons.Default.Restaurant, Color(0xFFF97316)),
    Triple("Transport", Icons.Default.DirectionsCar, Color(0xFF3B82F6)),
    Triple("Shopping", Icons.Default.ShoppingBag, Color(0xFF10B981)),
    Triple("Health", Icons.Default.Warning, Color(0xFFF43F5E)),
    Triple("Bills", Icons.Default.ElectricBolt, Color(0xFF14B8A6)),
    Triple("Entertainment", Icons.Default.Fastfood, Color(0xFF8B5CF6)),
    Triple("Other", Icons.Default.LocalAtm, Color(0xFFF59E0B)),
    Triple("Transfer", Icons.Default.ArrowForward, Color(0xFF334155)),
    Triple("More", Icons.Outlined.Settings, Color(0xFF64748B))
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .background(Color.White),
      color = Color.White
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp)
      ) {
        // Top Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onDismiss) {
            Icon(
              imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
              contentDescription = "Back",
              tint = Color(0xFF0F172A)
            )
          }

          Text(
            text = "Add Transaction",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          )

          IconButton(onClick = onDismiss) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color(0xFF64748B)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Expense / Income toggle
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF1F5F9))
            .padding(4.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(16.dp))
              .background(if (isExpense) Color(0xFF02844E) else Color.Transparent)
              .bouncyClickable { isExpense = true }
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Expense",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = if (isExpense) Color.White else Color(0xFF64748B)
            )
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(16.dp))
              .background(if (!isExpense) Color(0xFF02844E) else Color.Transparent)
              .bouncyClickable { isExpense = false }
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Income",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = if (!isExpense) Color.White else Color(0xFF64748B)
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Big Amount input
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "$currency $amountInput",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = amountInput,
            onValueChange = { amountInput = it },
            label = { Text("Enter Amount") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.7f),
            shape = RoundedCornerShape(12.dp)
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3x3 Category Grid
        Text(
          text = "Select Category",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF475569)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          for (row in 0 until 3) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              for (col in 0 until 3) {
                val index = row * 3 + col
                if (index < categories.size) {
                  val cat = categories[index]
                  val isSelected = (selectedCategory == cat.first)
                  Column(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(16.dp))
                      .background(if (isSelected) cat.third.copy(alpha = 0.15f) else Color(0xFFF8FAFC))
                      .border(
                        1.5.dp,
                        if (isSelected) cat.third else Color(0xFFE2E8F0),
                        RoundedCornerShape(16.dp)
                      )
                      .bouncyClickable { selectedCategory = cat.first }
                      .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Box(
                      modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(cat.third.copy(alpha = 0.2f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = cat.second,
                        contentDescription = cat.first,
                        tint = cat.third,
                        modifier = Modifier.size(20.dp)
                      )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                      text = cat.first,
                      fontSize = 12.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = Color(0xFF0F172A)
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Note optional
        OutlinedTextField(
          value = noteInput,
          onValueChange = { noteInput = it },
          placeholder = { Text("Add Note (Optional)", color = Color(0xFF94A3B8)) },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF8FAFC),
            focusedContainerColor = Color.White
          )
        )

        Spacer(modifier = Modifier.weight(1f))

        // Add Transaction Button
        Button(
          onClick = {
            val amt = amountInput.toDoubleOrNull() ?: 0.0
            onAdd(amt, selectedCategory, noteInput)
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("btn_confirm_add_transaction"),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02844E)),
          shape = RoundedCornerShape(16.dp)
        ) {
          Text(
            text = "Add Transaction",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }
  }
}

/**
 * Dialog to edit monthly budget
 */
@Composable
fun EditBudgetDialog(
  currentBudget: Int,
  currency: String,
  onDismiss: () -> Unit,
  onSave: (Int) -> Unit
) {
  var inputVal by remember { mutableStateOf(currentBudget.toString()) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color.White,
      shadowElevation = 8.dp,
      modifier = Modifier.padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(22.dp)) {
        Text(
          text = "Edit Monthly Budget",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Set your target monthly spending ceiling for $currency.",
          fontSize = 13.sp,
          color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = inputVal,
          onValueChange = { inputVal = it },
          label = { Text("Budget Amount ($currency)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF64748B)),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Cancel")
          }
          Spacer(modifier = Modifier.width(10.dp))
          Button(
            onClick = {
              val parsed = inputVal.toIntOrNull() ?: currentBudget
              onSave(parsed)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02844E)),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Save")
          }
        }
      }
    }
  }
}

/**
 * Rebalance Alert Dialog
 */
@Composable
fun RebalanceBudgetDialog(
  currency: String,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color.White,
      shadowElevation = 8.dp,
      modifier = Modifier.padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFE11D48),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Rebalance Categories",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Food & Dining is over budget by $currency 1,500. We can shift $currency 1,500 from your Shopping limit to keep your overall budget balanced.",
          fontSize = 13.sp,
          color = Color(0xFF475569),
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF64748B)),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Dismiss")
          }
          Spacer(modifier = Modifier.width(10.dp))
          Button(
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02844E)),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Rebalance Now")
          }
        }
      }
    }
  }
}

@Composable
fun BudgetSettingsDialog(onDismiss: () -> Unit) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color.White,
      shadowElevation = 8.dp,
      modifier = Modifier.padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Budget Preferences",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "• Cycle: Monthly (1st - End of Month)\n• Auto-Rollover: Enabled\n• Smart Overspend Alerts: Push & Email\n• Currency Sync: Automatic",
          fontSize = 13.sp,
          color = Color(0xFF475569),
          lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02844E)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Done")
        }
      }
    }
  }
}
