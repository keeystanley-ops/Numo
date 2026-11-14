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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Visibility
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.NumoClayHeroCard
import com.example.ui.components.NumoClayIconBadge
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoScreenBackground

@Composable
fun HomeScreen(
  userName: String = "Ayesha",
  currency: String = "KES",
  onAddTransaction: () -> Unit = {},
  onPayBill: () -> Unit = {},
  onOpenTransactions: () -> Unit = {},
  onOpenBudget: () -> Unit = {},
  onOpenProfile: () -> Unit = {},
  onOpenInsights: () -> Unit = {}
) {
  val firstName = userName.trim().substringBefore(" ").ifBlank { "Ayesha" }

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
            text = "Good Morning,",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary,
            lineHeight = 24.sp
          )
          Text(
            text = "$firstName 👋",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary,
            lineHeight = 26.sp
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
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
              text = "Available Cash,",
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              color = Color.White.copy(alpha = 0.9f)
            )
            Icon(
              imageVector = Icons.Outlined.Visibility,
              contentDescription = "Toggle Balance",
              tint = Color.White.copy(alpha = 0.9f),
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "$currency 38,000",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = (-0.5).sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Total: $currency 50,000  •  Reserved: $currency 12,000",
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
          onClick = onAddTransaction,
          modifier = Modifier.weight(1f)
        )
        HomeQuickActionTile(
          icon = Icons.Default.SwapHoriz,
          label = "Transfer",
          topColor = NumoDS.Cyan,
          bottomColor = Color(0xFF0D9488),
          onClick = onAddTransaction,
          modifier = Modifier.weight(1f)
        )
        HomeQuickActionTile(
          icon = Icons.Default.AccountBalanceWallet,
          label = "Receive\nIncome",
          topColor = Color(0xFFFB923C),
          bottomColor = Color(0xFFDC2626),
          onClick = onAddTransaction,
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
        Text(
          text = "This Month >",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = NumoDS.TextSecondary,
          modifier = Modifier.clickable { onOpenTransactions() }
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      NumoNeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        onClick = onOpenTransactions
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically
            ) {
              NumoClayIconBadge(
                icon = Icons.Default.ArrowDownward,
                topColor = Color(0xFF4ADE80),
                bottomColor = NumoDS.SuccessGreen,
                size = 30.dp,
                iconSize = 16.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Income",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  color = NumoDS.TextSecondary
                )
                Text(
                  text = "$currency 75,000",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = NumoDS.SuccessGreen
                )
              }
            }

            Column(
              modifier = Modifier.weight(1f),
              horizontalAlignment = Alignment.End
            ) {
              Text(
                text = "Expenses",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = NumoDS.TextSecondary
              )
              Text(
                text = "$currency 51,400",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NumoDS.TransportBlue
              )
            }
          }

          HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp),
            color = NumoDS.TrackBg
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically
            ) {
              NumoClayIconBadge(
                icon = Icons.Default.ArrowUpward,
                topColor = Color(0xFFF43F5E),
                bottomColor = NumoDS.ExpenseRed,
                size = 30.dp,
                iconSize = 16.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "$currency 52,000",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NumoDS.ExpenseRed
              )
            }

            Column(
              modifier = Modifier.weight(1f),
              horizontalAlignment = Alignment.End
            ) {
              Text(
                text = "Net",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = NumoDS.TextSecondary
              )
              Text(
                text = "$currency 25,000",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NumoDS.TransportBlue
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 5. Budget Health Section
      Text(
        text = "Budget Health",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = NumoDS.TextPrimary
      )

      Spacer(modifier = Modifier.height(10.dp))

      NumoNeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        onClick = onOpenBudget
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier.size(90.dp),
            contentAlignment = Alignment.Center
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val stroke = 12.dp.toPx()
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
                    NumoDS.Turquoise,
                    NumoDS.DuneOchre,
                    NumoDS.HeroTealMid
                  )
                ),
                startAngle = 140f,
                sweepAngle = 280f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
              )
            }
            Text(
              text = "78%",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = NumoDS.TextPrimary
            )
          }

          Spacer(modifier = Modifier.width(20.dp))

          Column {
            Text(
              text = "On Track",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = NumoDS.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$currency 6,000 left",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
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
