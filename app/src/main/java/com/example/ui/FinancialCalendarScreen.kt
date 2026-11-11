package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.animation.animateIntCount
import com.example.ui.animation.bouncyClickable
import com.example.ui.animation.formatAmount
import com.example.ui.animation.staggeredEntrance

/**
 * Financial Calendar Screen (Screen 9) & Bill Payment Screen (Screen 10)
 * matching the user reference mockup.
 */
@Composable
fun FinancialCalendarScreen(
  currency: String = "KES",
  onBack: () -> Unit = {}
) {
  var selectedBillForPayment by remember { mutableStateOf<String?>(null) }

  AnimatedContent(
    targetState = selectedBillForPayment,
    transitionSpec = {
      if (targetState != null) {
        (slideInHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn(tween(250)))
          .togetherWith(slideOutHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { -it / 3 } + fadeOut(tween(200)))
      } else {
        (slideInHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { -it / 3 } + fadeIn(tween(250)))
          .togetherWith(slideOutHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeOut(tween(200)))
      }
    },
    label = "billPaymentTransition"
  ) { billName ->
    if (billName != null) {
      // SCREEN 10: BILL PAYMENT
      BillPaymentDetailScreen(
        billName = billName,
        currency = currency,
        onBack = { selectedBillForPayment = null }
      )
    } else {
      // SCREEN 9: FINANCIAL CALENDAR
      FinancialCalendarMain(
        currency = currency,
        onBack = onBack,
        onOpenBill = { bill -> selectedBillForPayment = bill }
      )
    }
  }
}

@Composable
fun FinancialCalendarMain(
  currency: String,
  onBack: () -> Unit,
  onOpenBill: (String) -> Unit
) {
  var selectedDay by remember { mutableIntStateOf(24) }
  val scrollState = rememberScrollState()

  // Track paid bills
  var paidBills by remember { mutableStateOf(setOf<String>()) }

  val days = listOf(
    Pair("Mon", 21),
    Pair("Tue", 22),
    Pair("Wed", 23),
    Pair("Thu", 24),
    Pair("Fri", 25),
    Pair("Sat", 26),
    Pair("Sun", 27)
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAF9))
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.White)
            .bouncyClickable { onBack() }
            .testTag("btn_cal_back")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Back",
            tint = Color(0xFF0F172A)
          )
        }

        Text(
          text = "Financial Calendar",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = {}) {
            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color(0xFF334155))
          }
          IconButton(onClick = {}) {
            Icon(Icons.Default.CalendarMonth, contentDescription = "Month", tint = Color(0xFF334155))
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // DATE STRIP (Mon 21 .. Sun 27 with Thu 24 highlighted)
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .staggeredEntrance(delayMs = 40),
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 0.5.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp, horizontal = 8.dp),
          horizontalArrangement = Arrangement.SpaceAround,
          verticalAlignment = Alignment.CenterVertically
        ) {
          days.forEach { (dayLabel, dateNum) ->
            val isSelected = (selectedDay == dateNum)
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier
                .bouncyClickable { selectedDay = dateNum }
                .padding(horizontal = 4.dp)
            ) {
              Text(
                text = dayLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(if (isSelected) Color(0xFF02844E) else Color.Transparent),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "$dateNum",
                  fontSize = 14.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else Color(0xFF0F172A)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // UPCOMING SECTION
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Upcoming",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
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

      // 1. Rent Payment: KES 35,000, In 2 days
      CalendarBillItem(
        title = "Rent Payment",
        amount = "$currency 35,000",
        dueText = "In 2 days",
        icon = Icons.Default.Home,
        iconBg = Color(0xFFEFF6FF),
        iconTint = Color(0xFF2563EB),
        isPaid = paidBills.contains("Rent Payment"),
        onMarkAsPaid = {
          paidBills = paidBills + "Rent Payment"
        },
        onClick = { onOpenBill("Rent Payment") },
        delayMs = 80
      )

      Spacer(modifier = Modifier.height(12.dp))

      // 2. KPLC Electricity: KES 2,500, In 4 days (Screen 10 target)
      CalendarBillItem(
        title = "KPLC Electricity",
        amount = "$currency 2,500",
        dueText = "In 4 days",
        icon = Icons.Default.ElectricBolt,
        iconBg = Color(0xFFEFF6FF),
        iconTint = Color(0xFF02844E),
        isPaid = paidBills.contains("KPLC Electricity"),
        onMarkAsPaid = {
          paidBills = paidBills + "KPLC Electricity"
        },
        onClick = { onOpenBill("KPLC Electricity") },
        delayMs = 120
      )

      Spacer(modifier = Modifier.height(12.dp))

      // 3. Internet: KES 1,999, In 5 days
      CalendarBillItem(
        title = "Internet",
        amount = "$currency 1,999",
        dueText = "In 5 days",
        icon = Icons.Default.Wifi,
        iconBg = Color(0xFFEFF6FF),
        iconTint = Color(0xFF0284C7),
        isPaid = paidBills.contains("Internet"),
        onMarkAsPaid = {
          paidBills = paidBills + "Internet"
        },
        onClick = { onOpenBill("Internet") },
        delayMs = 160
      )

      Spacer(modifier = Modifier.height(60.dp))
    }
  }
}

@Composable
fun CalendarBillItem(
  title: String,
  amount: String,
  dueText: String,
  icon: ImageVector,
  iconBg: Color,
  iconTint: Color,
  isPaid: Boolean,
  onMarkAsPaid: () -> Unit,
  onClick: () -> Unit,
  delayMs: Int
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .staggeredEntrance(delayMs = delayMs)
      .clip(RoundedCornerShape(18.dp))
      .bouncyClickable { onClick() },
    color = Color.White,
    shape = RoundedCornerShape(18.dp),
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
          text = amount,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )
        Text(
          text = dueText,
          fontSize = 11.sp,
          color = Color(0xFF64748B)
        )
      }

      // [Mark as Paid] Button pill
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .background(if (isPaid) Color(0xFFE8F8F2) else Color.White)
          .border(
            1.dp,
            if (isPaid) Color(0xFF02844E) else Color(0xFFCBD5E1),
            RoundedCornerShape(12.dp)
          )
          .bouncyClickable { onMarkAsPaid() }
          .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (isPaid) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Paid",
              tint = Color(0xFF02844E),
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
          }
          Text(
            text = if (isPaid) "Paid" else "Mark as Paid",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isPaid) Color(0xFF02844E) else Color(0xFF334155)
          )
        }
      }
    }
  }
}

/**
 * Screen 10: Bill Payment Screen matching user mockup
 * - Header: "<- Bill Payment"
 * - Large circular badge with lightning icon
 * - "KPLC Electricity"
 * - "KES 2,500"
 * - "Due in 4 days"
 * - Big green "Pay Bill" button
 * - Bill Details: Account Number 12345678, Due Date Apr 27, 2025
 */
@Composable
fun BillPaymentDetailScreen(
  billName: String,
  currency: String,
  onBack: () -> Unit
) {
  var isPaidSuccess by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAF9))
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.White)
            .bouncyClickable { onBack() }
            .testTag("btn_bill_pay_back")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Back",
            tint = Color(0xFF0F172A)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
          text = "Bill Payment",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )
      }

      Spacer(modifier = Modifier.height(36.dp))

      // Center Hero
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Color(0xFFEFF6FF)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.ElectricBolt,
            contentDescription = billName,
            tint = Color(0xFF0F172A),
            modifier = Modifier.size(36.dp)
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = billName,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "$currency 2,500",
          fontSize = 28.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = if (isPaidSuccess) "Payment Completed" else "Due in 4 days",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = if (isPaidSuccess) Color(0xFF02844E) else Color(0xFF64748B)
        )
      }

      Spacer(modifier = Modifier.height(32.dp))

      // Big "Pay Bill" button
      Button(
        onClick = { isPaidSuccess = true },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_pay_bill_submit"),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isPaidSuccess) Color(0xFF059669) else Color(0xFF02844E)
        ),
        shape = RoundedCornerShape(16.dp)
      ) {
        if (isPaidSuccess) {
          Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Bill Paid Successfully!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        } else {
          Text("Pay Bill", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(36.dp))

      // BILL DETAILS CARD
      Text(
        text = "Bill Details",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0F172A)
      )

      Spacer(modifier = Modifier.height(14.dp))

      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 0.5.dp
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Account Number",
              fontSize = 14.sp,
              color = Color(0xFF64748B)
            )
            Text(
              text = "12345678",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0F172A)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Due Date",
              fontSize = 14.sp,
              color = Color(0xFF64748B)
            )
            Text(
              text = "Apr 27, 2025",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0F172A)
            )
          }
        }
      }
    }
  }
}
