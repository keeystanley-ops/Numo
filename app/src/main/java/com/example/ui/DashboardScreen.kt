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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

val NumoDarkBg = Color(0xFF070A0F)
val NumoCardBg = Color(0xFF0F1626)
val NumoCardBorder = Color(0xFF1E293B)
val NumoAccentGreen = Color(0xFF00D084)
val NumoGold = Color(0xFFF59E0B)

data class TransactionItem(
  val id: String,
  val title: String,
  val category: String,
  val amount: String,
  val isExpense: Boolean,
  val date: String,
  val isBusiness: Boolean
)

@Composable
fun DashboardScreen(
  userEmail: String = "stalonngacha@gmail.com",
  provider: String = "Google",
  onSignOut: () -> Unit
) {
  var selectedTab by remember { mutableStateOf(0) } // 0: Money Management, 1: Investments

  val transactions = listOf(
    TransactionItem("1", "Naivas Supermarket", "Groceries", "- KES 6,450", true, "Today, 2:15 PM", false),
    TransactionItem("2", "Client Retainer", "Consulting", "+ KES 95,000", false, "Yesterday", true),
    TransactionItem("3", "KPLC Prepaid Power", "Utilities", "- KES 2,500", true, "Nov 8", false),
    TransactionItem("4", "Sanlam MMF Yield", "Interest", "+ KES 3,820", false, "Nov 7", false),
    TransactionItem("5", "Safaricom M-PESA Transfer", "Transfer", "- KES 15,000", true, "Nov 5", false),
  )

  Surface(
    modifier = Modifier
      .fillMaxSize()
      .background(NumoDarkBg)
      .statusBarsPadding()
      .navigationBarsPadding(),
    color = NumoDarkBg
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Top Bar
      item {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
              painter = painterResource(id = R.drawable.ic_numo_leaf),
              contentDescription = "Numo Brand",
              modifier = Modifier.size(36.dp),
              contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Welcome back,",
                  fontSize = 12.sp,
                  color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = "Authenticated",
                  tint = NumoAccentGreen,
                  modifier = Modifier.size(13.dp)
                )
              }
              Text(
                text = userEmail.substringBefore("@"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }

          // Sign Out Button
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(NumoCardBg)
              .border(1.dp, NumoCardBorder, RoundedCornerShape(20.dp))
              .clickable { onSignOut() }
              .padding(horizontal = 12.dp, vertical = 8.dp)
              .testTag("sign_out_button")
          ) {
            Icon(
              imageVector = Icons.Default.ExitToApp,
              contentDescription = "Sign Out",
              tint = Color(0xFFEF4444),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Sign Out",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFFEF4444)
            )
          }
        }
      }

      // 2. Net Worth Card
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
              brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0E2A1F), Color(0xFF091410), NumoCardBg)
              )
            )
            .border(1.2.dp, Color(0xFF00D084).copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            .padding(22.dp)
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "TOTAL NET WORTH",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = NumoAccentGreen
              )
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0xFF00D084).copy(alpha = 0.15f))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "KES (Kenya)",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = NumoAccentGreen
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "KES 485,250.00",
              fontSize = 32.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.SansSerif,
              color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = NumoAccentGreen,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "+12.4% (KES 53,800)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = NumoAccentGreen
              )
              Text(
                text = " vs last month",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8)
              )
            }
          }
        }
      }

      // 3. Environment Switcher Tabs (Money Management vs Investment Management)
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NumoCardBg)
            .padding(4.dp)
        ) {
          // Tab 0: Money
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(if (selectedTab == 0) Color(0xFF059669) else Color.Transparent)
              .clickable { selectedTab = 0 }
              .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Money Management",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = if (selectedTab == 0) Color.White else Color(0xFF94A3B8)
            )
          }

          // Tab 1: Investments
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(if (selectedTab == 1) NumoGold else Color.Transparent)
              .clickable { selectedTab = 1 }
              .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Investments",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = if (selectedTab == 1) Color.Black else Color(0xFF94A3B8)
            )
          }
        }
      }

      // 4. Quick Accounts Strip
      item {
        Column {
          Text(
            text = if (selectedTab == 0) "ACCOUNTS & CASH FLOW" else "PORTFOLIO HOLDINGS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            letterSpacing = 0.5.sp
          )
          Spacer(modifier = Modifier.height(10.dp))
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            if (selectedTab == 0) {
              item {
                AccountPill("M-PESA", "KES 48,200", Color(0xFF00D084))
              }
              item {
                AccountPill("Equity Bank", "KES 185,000", Color(0xFF60A5FA))
              }
              item {
                AccountPill("Cash Wallet", "KES 24,500", Color(0xFFFBBF24))
              }
              item {
                AccountPill("KCB Savings", "KES 80,000", Color(0xFFA78BFA))
              }
            } else {
              item {
                AccountPill("Sanlam MMF", "KES 125,400", NumoGold)
              }
              item {
                AccountPill("NSE: Safaricom", "KES 64,800", Color(0xFF34D399))
              }
              item {
                AccountPill("Treasury Bond FXD", "KES 150,000", Color(0xFF60A5FA))
              }
              item {
                AccountPill("Stima SACCO", "KES 92,000", Color(0xFFF472B6))
              }
            }
          }
        }
      }

      // 5. Monthly Budget / Target Progress
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = NumoCardBg),
          border = androidx.compose.foundation.BorderStroke(1.dp, NumoCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (selectedTab == 0) "November Spending Budget" else "Annual Wealth Target",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
              )
              Text(
                text = if (selectedTab == 0) "KES 85,000 / 120,000" else "KES 485k / 1.0M",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NumoAccentGreen
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
              progress = { if (selectedTab == 0) 0.70f else 0.48f },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
              color = NumoAccentGreen,
              trackColor = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (selectedTab == 0) "KES 35,000 remaining for 20 days" else "On track to reach KES 1M milestone",
              fontSize = 12.sp,
              color = Color(0xFF94A3B8)
            )
          }
        }
      }

      // 6. Recent Double-Entry Ledger Transactions
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "RECENT LEDGER ENTRIES",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            letterSpacing = 0.5.sp
          )
          Text(
            text = "See All",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NumoAccentGreen
          )
        }
      }

      items(transactions) { tx ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = NumoCardBg),
          border = androidx.compose.foundation.BorderStroke(1.dp, NumoCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(if (tx.isExpense) Color(0x20EF4444) else Color(0x2000D084)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (tx.isExpense) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                  contentDescription = null,
                  tint = if (tx.isExpense) Color(0xFFEF4444) else Color(0xFF00D084),
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Text(
                  text = tx.title,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = tx.category,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                  )
                  if (tx.isBusiness) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF3B82F6).copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Text(
                        text = "Business",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF60A5FA)
                      )
                    }
                  }
                }
              }
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = tx.amount,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (tx.isExpense) Color.White else NumoAccentGreen
              )
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = tx.date,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(30.dp))
      }
    }
  }
}

@Composable
fun AccountPill(name: String, balance: String, color: Color) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .background(NumoCardBg)
      .border(1.dp, NumoCardBorder, RoundedCornerShape(16.dp))
      .padding(horizontal = 16.dp, vertical = 12.dp)
  ) {
    Column {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = name,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFF94A3B8)
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = balance,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
    }
  }
}
