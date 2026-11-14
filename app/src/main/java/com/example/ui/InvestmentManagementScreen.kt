package com.example.ui

import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.InvestmentHolding
import com.example.data.NumoFinancialEngine
import com.example.ui.animation.TopScrollProgressBar
import com.example.ui.animation.animateIntCount
import com.example.ui.animation.bouncyClickable
import com.example.ui.animation.formatAmount
import com.example.ui.animation.staggeredEntrance

/**
 * Investment Management Environment (PLAN.md Environment 2)
 * Features:
 * - Top Dual-Environment Pill Switcher [ Money Management | Investments ]
 * - Portfolio Valuation Hero Card with Invested Capital, Return %, and Freshness badge
 * - Projected 30-Day Dividend & Yield Strip
 * - Live Holdings Directory (Safaricom, Sanlam MMF, Stima SACCO, T-Bills)
 * - "+ Add Holding" dialog directly updating NumoFinancialEngine
 */
@Composable
fun InvestmentManagementScreen(
  currency: String = "KES",
  onSwitchToMoney: () -> Unit
) {
  val scrollState = rememberScrollState()
  var showAddHoldingDialog by remember { mutableStateOf(false) }

  val totalValuation = NumoFinancialEngine.totalPortfolioValuation.toInt()
  val investedCapital = NumoFinancialEngine.totalInvestedCapital.toInt()
  val totalReturn = (totalValuation - investedCapital).coerceAtLeast(0)
  val returnPercent = if (investedCapital > 0) ((totalReturn.toDouble() / investedCapital) * 100) else 0.0

  val animatedValuation = animateIntCount(totalValuation, durationMs = 800)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAF9))
  ) {
    TopScrollProgressBar(scrollState = scrollState)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      // 1. Dual Environment Switcher Pill
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center
      ) {
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE2E8F0))
            .padding(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(Color.Transparent)
              .bouncyClickable { onSwitchToMoney() }
              .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Money",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF64748B)
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(Color(0xFF02844E))
              .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Investments",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Top Header with Add Holding button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Portfolio Overview",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Multi-Asset Wealth Engine",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B)
          )
        }

        IconButton(
          onClick = { showAddHoldingDialog = true },
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0xFFE8F8F2))
            .bouncyClickable { showAddHoldingDialog = true }
            .testTag("btn_add_holding")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Holding",
            tint = Color(0xFF02844E),
            modifier = Modifier.size(22.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 3. Hero Card: Total Portfolio Valuation
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .staggeredEntrance(delayMs = 40)
          .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF02844E).copy(alpha = 0.35f))
          .clip(RoundedCornerShape(24.dp))
          .background(
            Brush.linearGradient(
              colors = listOf(
                Color(0xFF015B35),
                Color(0xFF02844E),
                Color(0xFF059669)
              )
            )
          )
          .padding(22.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Total Portfolio Value",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color.White.copy(alpha = 0.85f)
            )

            // Freshness Badge
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.20f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "● Live Market Data",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF86EFAC)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "$currency ${formatAmount(animatedValuation)}.00",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                contentDescription = null,
                tint = Color(0xFF86EFAC),
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "+$currency ${formatAmount(totalReturn)} (${String.format("%.1f", returnPercent)}%)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF86EFAC)
              )
            }

            Text(
              text = "Invested: $currency ${formatAmount(investedCapital)}",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = Color.White.copy(alpha = 0.85f)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 4. Projected 30-Day Dividend & Yield Strip
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .staggeredEntrance(delayMs = 80),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFEFF6FF),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(Color(0xFFDBEAFE)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = null,
              tint = Color(0xFF1D4ED8),
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column {
            Text(
              text = "Projected 30-Day Yields",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1E3A8A)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "+$currency 4,250 expected from MMF & SACCO dividends",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = Color(0xFF3B82F6)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(22.dp))

      // 5. Holdings Section Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Holdings (${NumoFinancialEngine.investmentHoldings.size})",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )

        Text(
          text = "Allocation",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFF02844E)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 6. Dynamic Holdings List
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        NumoFinancialEngine.investmentHoldings.forEachIndexed { index, holding ->
          HoldingCardItem(
            holding = holding,
            currency = currency,
            delayMs = 100 + index * 40
          )
        }
      }

      Spacer(modifier = Modifier.height(60.dp))
    }
  }

  // Add Holding Dialog
  if (showAddHoldingDialog) {
    AddHoldingDialog(
      currency = currency,
      onDismiss = { showAddHoldingDialog = false },
      onSave = { name, type, invested, yieldPct ->
        NumoFinancialEngine.addInvestmentHolding(name, type, invested, yieldPct)
        showAddHoldingDialog = false
      }
    )
  }
}

@Composable
fun HoldingCardItem(
  holding: InvestmentHolding,
  currency: String,
  delayMs: Int
) {
  val icon = when (holding.assetType) {
    "NSE Stocks" -> Icons.Default.ShowChart
    "MMF" -> Icons.Default.MonetizationOn
    "SACCO" -> Icons.Default.AccountBalance
    else -> Icons.Default.VerifiedUser
  }

  val iconBg = when (holding.assetType) {
    "NSE Stocks" -> Color(0xFFEFF6FF)
    "MMF" -> Color(0xFFFEF3C7)
    "SACCO" -> Color(0xFFF3E8FF)
    else -> Color(0xFFE8FBF4)
  }

  val iconTint = when (holding.assetType) {
    "NSE Stocks" -> Color(0xFF2563EB)
    "MMF" -> Color(0xFFD97706)
    "SACCO" -> Color(0xFF7C3AED)
    else -> Color(0xFF02844E)
  }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .staggeredEntrance(delayMs = delayMs),
    color = Color.White,
    shape = RoundedCornerShape(18.dp),
    shadowElevation = 1.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
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
          contentDescription = holding.name,
          tint = iconTint,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = holding.name,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = holding.assetType,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Yield: ${holding.annualYieldPercent}%",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF02844E)
          )
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "$currency ${formatAmount(holding.currentValuation.toInt())}",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "+${String.format("%.1f", holding.returnPercentage)}%",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF02844E)
        )
      }
    }
  }
}

@Composable
fun AddHoldingDialog(
  currency: String,
  onDismiss: () -> Unit,
  onSave: (String, String, Double, Double) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var selectedType by remember { mutableStateOf("NSE Stocks") }
  var investedAmount by remember { mutableStateOf("") }
  var annualYield by remember { mutableStateOf("10.0") }

  val assetTypes = listOf("NSE Stocks", "MMF", "SACCO", "Treasury Bill")

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = Color.White,
      modifier = Modifier.padding(16.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text(
          text = "Add Investment Holding",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Holding Name (e.g. Britam MMF)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Asset Class",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          assetTypes.take(2).forEach { type ->
            val isSelected = (selectedType == type)
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) Color(0xFF02844E) else Color(0xFFF1F5F9))
                .clickable { selectedType = type }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = type,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color(0xFF334155)
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          assetTypes.drop(2).forEach { type ->
            val isSelected = (selectedType == type)
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) Color(0xFF02844E) else Color(0xFFF1F5F9))
                .clickable { selectedType = type }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = type,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color(0xFF334155)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = investedAmount,
          onValueChange = { investedAmount = it },
          label = { Text("Invested Capital ($currency)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = annualYield,
          onValueChange = { annualYield = it },
          label = { Text("Expected Yield (%)") },
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
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFFF1F5F9),
              contentColor = Color(0xFF64748B)
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Cancel")
          }

          Spacer(modifier = Modifier.width(8.dp))

          Button(
            onClick = {
              val amt = investedAmount.toDoubleOrNull() ?: 0.0
              val yld = annualYield.toDoubleOrNull() ?: 10.0
              if (name.isNotBlank() && amt > 0) {
                onSave(name, selectedType, amt, yld)
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02844E)),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Add Holding")
          }
        }
      }
    }
  }
}
