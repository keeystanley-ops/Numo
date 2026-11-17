package com.example.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.NumoFinancialEngine
import com.example.ui.components.NumoClayHeroCard
import com.example.ui.components.NumoClayIconBadge
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoScreenBackground

@Composable
fun HomeScreen(
  userName: String = "Ayesha",
  currency: String = NumoFinancialEngine.currency,
  onAddTransaction: () -> Unit = {},
  onAddTransactionTab: (String) -> Unit = { onAddTransaction() },
  onPayBill: () -> Unit = {},
  onOpenTransactions: () -> Unit = {},
  onOpenTransactionsWithType: (String) -> Unit = { onOpenTransactions() },
  onOpenBudget: () -> Unit = {},
  onOpenProfile: () -> Unit = {},
  onOpenInsights: () -> Unit = {},
  onOpenHeyNumo: () -> Unit = {}
) {
  val rawName = userName.trim().substringBefore(" ")
  val firstName = if (
    rawName.isBlank() ||
    rawName.equals("null", ignoreCase = true) ||
    rawName.equals("undefined", ignoreCase = true)
  ) null else rawName
  val isVisible = NumoFinancialEngine.isBalanceVisible
  val totalBalance = NumoFinancialEngine.accounts.sumOf { it.balance }
  val totalReserved = NumoFinancialEngine.bills.filter { !it.isPaid }.sumOf { it.remainingAmount }
  val availableCash = (totalBalance - totalReserved).coerceAtLeast(0.0)
  val monthlyIncome = NumoFinancialEngine.monthlyIncomeTotal
  val monthlyExpenses = NumoFinancialEngine.monthlyExpenseTotal
  val netCashFlow = NumoFinancialEngine.netCashFlow
  val budgetPercent = NumoFinancialEngine.budgetConsumptionPercent
  val netWorth = NumoFinancialEngine.totalNetWorth

  fun maskAmount(formatted: String): String = if (isVisible) formatted else "$currency ••••••"

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
        Column {
          Text(
            text = if (firstName != null) "Good Morning," else "Good Morning 👋",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary,
            lineHeight = 24.sp
          )
          if (firstName != null) {
            Text(
              text = "$firstName 👋",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = NumoDS.TextPrimary,
              lineHeight = 26.sp
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier
              .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = NumoDS.Turquoise.copy(alpha = 0.25f),
                spotColor = NumoDS.Turquoise.copy(alpha = 0.25f)
              )
              .clip(RoundedCornerShape(20.dp))
              .background(
                Brush.horizontalGradient(
                  listOf(NumoDS.BtnTealTop, NumoDS.BtnTealBottom)
                )
              )
              .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
              .clickable { onOpenHeyNumo() }
              .padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Mic,
              contentDescription = "Hey Numo Voice Assistant",
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Hey Numo",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }

          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(NumoDS.CardSurfaceLight)
              .border(1.dp, NumoDS.CardBorderLight, CircleShape)
              .clickable { onOpenInsights() },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.Notifications,
              contentDescription = "Notifications",
              tint = NumoDS.TextPrimary,
              modifier = Modifier.size(21.dp)
            )
            Box(
              modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-6).dp, y = 6.dp)
                .size(13.dp)
                .clip(CircleShape)
                .background(NumoDS.ExpenseRed),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "3",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }

          Spacer(modifier = Modifier.width(10.dp))

          Image(
            painter = painterResource(id = R.drawable.img_user_avatar),
            contentDescription = "Profile Avatar",
            modifier = Modifier
              .size(42.dp)
              .shadow(4.dp, CircleShape)
              .clip(CircleShape)
              .border(2.dp, Color.White, CircleShape)
              .clickable { onOpenProfile() },
            contentScale = ContentScale.Crop
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Hero Available Cash Card
      NumoClayHeroCard {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Available Cash",
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              color = Color.White.copy(alpha = 0.9f)
            )
            Icon(
              imageVector = if (isVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
              contentDescription = "Toggle Balance Visibility",
              tint = Color.White.copy(alpha = 0.9f),
              modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .clickable { NumoFinancialEngine.isBalanceVisible = !isVisible }
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = maskAmount(NumoFinancialEngine.formatKes(availableCash)),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = (-0.5).sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Total: ${maskAmount(NumoFinancialEngine.formatKes(totalBalance, false))}  •  Reserved: ${maskAmount(NumoFinancialEngine.formatKes(totalReserved, false))}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.88f)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 3. 4 Quick Action Claymorphic Tiles
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        HomeQuickActionTile(
          icon = Icons.Default.Add,
          label = "Add\nTransaction",
          topColor = Color(0xFF4ADE80),
          bottomColor = Color(0xFF15803D),
          onClick = { onAddTransactionTab("expense") },
          modifier = Modifier.weight(1f)
        )
        HomeQuickActionTile(
          icon = Icons.Default.SwapHoriz,
          label = "Transfer",
          topColor = NumoDS.Cyan,
          bottomColor = Color(0xFF0D9488),
          onClick = { onAddTransactionTab("transfer") },
          modifier = Modifier.weight(1f)
        )
        HomeQuickActionTile(
          icon = Icons.Default.AccountBalanceWallet,
          label = "Receive\nIncome",
          topColor = Color(0xFFFB923C),
          bottomColor = Color(0xFFDC2626),
          onClick = { onAddTransactionTab("income") },
          modifier = Modifier.weight(1f)
        )
        HomeQuickActionTile(
          icon = Icons.Default.ReceiptLong,
          label = "Pay Bill",
          topColor = Color(0xFF38BDF8),
          bottomColor = NumoDS.TransportBlue,
          onClick = onPayBill,
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 4. Monthly Cash Flow Section
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Monthly Cash Flow",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onOpenTransactionsWithType("all") }
            .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
          Text(
            text = "View All",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = NumoDS.TextSecondary
          )
          Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = NumoDS.TextSecondary,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      NumoNeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        onClick = { onOpenTransactionsWithType("all") }
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onOpenTransactionsWithType("income") }
                .padding(4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              NumoClayIconBadge(
                icon = Icons.Default.ArrowUpward,
                topColor = Color(0xFF4ADE80),
                bottomColor = NumoDS.SuccessGreen,
                size = 30.dp,
                iconSize = 16.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = maskAmount(NumoFinancialEngine.formatKes(monthlyIncome, false)),
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = NumoDS.SuccessGreen,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "Income",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  color = NumoDS.TextSecondary
                )
              }
            }

            Row(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onOpenTransactionsWithType("expense") }
                .padding(4.dp),
              horizontalArrangement = Arrangement.End,
              verticalAlignment = Alignment.CenterVertically
            ) {
              NumoClayIconBadge(
                icon = Icons.Default.ArrowDownward,
                topColor = Color(0xFFF43F5E),
                bottomColor = NumoDS.ExpenseRed,
                size = 30.dp,
                iconSize = 16.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = maskAmount(NumoFinancialEngine.formatKes(monthlyExpenses, false)),
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = NumoDS.ExpenseRed,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "Expenses",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  color = NumoDS.TextSecondary
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Proportional Comparison Bar
          val totalFlow = (monthlyIncome + monthlyExpenses).coerceAtLeast(1.0)
          val incomeFraction = (monthlyIncome / totalFlow).toFloat().coerceIn(0.05f, 0.95f)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp))
              .background(NumoDS.ExpenseRed.copy(alpha = 0.78f))
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth(incomeFraction)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(NumoDS.SuccessGreen)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          val netPrefix = if (netCashFlow < 0) "-" else ""
          val netAbs = kotlin.math.abs(netCashFlow)
          Text(
            text = "Net: $netPrefix${maskAmount(NumoFinancialEngine.formatKes(netAbs, false))}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (netCashFlow >= 0) NumoDS.TextPrimary else NumoDS.ExpenseRed,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 5. Budget Health & Net Worth Side-by-Side Cards
      val budgetStatus = when {
        budgetPercent >= 100 -> "Over Budget"
        budgetPercent >= 80 -> "Nearing Limit"
        else -> "On Track"
      }
      val budgetRingColor = when {
        budgetPercent >= 100 -> NumoDS.ExpenseRed
        budgetPercent >= 80 -> NumoDS.DuneOchre
        else -> NumoDS.SuccessGreen
      }
      val sweepFraction = (budgetPercent.coerceIn(0, 100) / 100f) * 360f

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        NumoNeumorphicCard(
          modifier = Modifier.weight(1f),
          cornerRadius = 20.dp,
          onClick = onOpenBudget
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Budget Health",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = NumoDS.TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
              modifier = Modifier.size(74.dp),
              contentAlignment = Alignment.Center
            ) {
              Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 9.dp.toPx()
                drawArc(
                  color = NumoDS.TrackBg,
                  startAngle = 0f,
                  sweepAngle = 360f,
                  useCenter = false,
                  style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                drawArc(
                  brush = Brush.sweepGradient(
                    colors = listOf(
                      NumoDS.HeroTealMid,
                      budgetRingColor,
                      NumoDS.DuneOchre,
                      NumoDS.HeroTealMid
                    )
                  ),
                  startAngle = -90f,
                  sweepAngle = sweepFraction,
                  useCenter = false,
                  style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
              }
              Text(
                text = "$budgetPercent%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NumoDS.TextPrimary
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = budgetStatus,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = budgetRingColor
            )
            val remaining = NumoFinancialEngine.totalRemaining
            Text(
              text = if (budgetPercent >= 100) "Review limits" else "${maskAmount(NumoFinancialEngine.formatKes(remaining, false))} left",
              fontSize = 11.sp,
              color = NumoDS.TextSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        NumoNeumorphicCard(
          modifier = Modifier.weight(1f),
          cornerRadius = 20.dp,
          onClick = onOpenInsights
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Net Worth",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = NumoDS.TextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))
            NumoClayIconBadge(
              icon = Icons.Default.AccountBalanceWallet,
              topColor = Color(0xFF4ADE80),
              bottomColor = NumoDS.HeroTealMid,
              size = 54.dp,
              iconSize = 26.dp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = maskAmount(NumoFinancialEngine.formatKes(netWorth, false)),
              fontSize = 17.sp,
              fontWeight = FontWeight.ExtraBold,
              color = NumoDS.TextPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Cash + Investments",
              fontSize = 11.sp,
              color = NumoDS.TextSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun HomeQuickActionTile(
  icon: ImageVector,
  label: String,
  topColor: Color,
  bottomColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  NumoNeumorphicCard(
    modifier = modifier.height(92.dp),
    cornerRadius = 16.dp,
    onClick = onClick
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 4.dp, vertical = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      NumoClayIconBadge(
        icon = icon,
        topColor = topColor,
        bottomColor = bottomColor,
        size = 38.dp,
        iconSize = 20.dp
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = NumoDS.TextPrimary,
        textAlign = TextAlign.Center,
        lineHeight = 12.sp
      )
    }
  }
}
