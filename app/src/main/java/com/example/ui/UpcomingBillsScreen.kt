package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.FinancialBill
import com.example.data.NumoFinancialEngine
import com.example.data.RecurrenceFrequency
import com.example.ui.components.NumoClayIconBadge
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoScreenBackground
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val BillMagenta = Color(0xFFD81B60)
private val BillEmerald = Color(0xFF1B8A5A)
private val BillTeal = Color(0xFF00796B)
private val BillAmber = Color(0xFFD97706)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingBillsScreen(
  onBack: () -> Unit = {}
) {
  val bills = NumoFinancialEngine.bills
  val nowMillis = System.currentTimeMillis()
  val dayMillis = 86_400_000L

  var calendarExpanded by remember { mutableStateOf(false) }
  var displayedMonthOffset by remember { mutableIntStateOf(0) }
  var selectedDayOfMonth by remember { mutableStateOf<Int?>(null) }

  var selectedBillForDetail by remember { mutableStateOf<FinancialBill?>(null) }
  var billToPay by remember { mutableStateOf<FinancialBill?>(null) }
  var showAddBillModal by remember { mutableStateOf(false) }
  var editingBill by remember { mutableStateOf<FinancialBill?>(null) }
  var showAllBillsDirectory by remember { mutableStateOf(false) }

  val monthCal = remember(displayedMonthOffset) {
    Calendar.getInstance().apply {
      set(Calendar.DAY_OF_MONTH, 1)
      add(Calendar.MONTH, displayedMonthOffset)
    }
  }
  val monthTitle = remember(monthCal) {
    SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(monthCal.time)
  }
  val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
  val firstDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sun

  fun billsForDay(day: Int): List<FinancialBill> {
    val targetYear = monthCal.get(Calendar.YEAR)
    val targetMonth = monthCal.get(Calendar.MONTH)
    val c = Calendar.getInstance()
    return bills.filter { b ->
      c.timeInMillis = b.dueDateMillis
      c.get(Calendar.YEAR) == targetYear &&
        c.get(Calendar.MONTH) == targetMonth &&
        c.get(Calendar.DAY_OF_MONTH) == day
    }
  }

  fun dotColorForBill(bill: FinancialBill): Color {
    if (bill.isPaid) return BillEmerald
    val diffDays = (bill.dueDateMillis - nowMillis) / dayMillis.toDouble()
    return when {
      diffDays < 0 -> BillMagenta
      diffDays <= 3.0 -> BillAmber
      else -> BillTeal
    }
  }

  val filteredBills = remember(bills.toList(), selectedDayOfMonth, displayedMonthOffset) {
    val base = if (selectedDayOfMonth != null) {
      billsForDay(selectedDayOfMonth!!)
    } else {
      bills.filter { !it.isPaid }.sortedBy { it.dueDateMillis }
    }
    base
  }

  val nextBill = filteredBills.firstOrNull()
  val otherBills = if (filteredBills.size > 1) filteredBills.drop(1) else emptyList()

  NumoScreenBackground {
    Box(modifier = Modifier.fillMaxSize()) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp)
      ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NumoDS.TextPrimary)
          }
          Text(
            text = "Upcoming Bills",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )
          Box(
            modifier = Modifier
              .size(38.dp)
              .shadow(3.dp, RoundedCornerShape(12.dp))
              .clip(RoundedCornerShape(12.dp))
              .background(if (calendarExpanded) BillTeal else Color(0xFFD6F3EE))
              .clickable { calendarExpanded = !calendarExpanded },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = "Toggle Calendar",
              tint = if (calendarExpanded) Color.White else BillTeal,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Month Header + Strip or Full Monthly Calendar
        NumoNeumorphicCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              IconButton(onClick = {
                displayedMonthOffset--
                selectedDayOfMonth = null
              }, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = NumoDS.TextPrimary)
              }
              Text(
                text = monthTitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = NumoDS.TextPrimary
              )
              IconButton(onClick = {
                displayedMonthOffset++
                selectedDayOfMonth = null
              }, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = NumoDS.TextPrimary)
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Weekday labels
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { wd ->
                Text(
                  text = wd,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium,
                  color = NumoDS.TextSecondary,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.weight(1f)
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (!calendarExpanded) {
              // Compact 7-day horizontal strip around today
              val todayCal = Calendar.getInstance()
              val startDay = if (displayedMonthOffset == 0) (todayCal.get(Calendar.DAY_OF_MONTH) - 3).coerceIn(1, (daysInMonth - 6).coerceAtLeast(1)) else 1
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (i in 0 until 7) {
                  val d = (startDay + i).coerceAtMost(daysInMonth)
                  val dayBills = billsForDay(d)
                  val isSel = selectedDayOfMonth == d
                  CalendarDayCell(
                    day = d,
                    selected = isSel,
                    dotColors = dayBills.map { dotColorForBill(it) },
                    modifier = Modifier.weight(1f),
                    onClick = { selectedDayOfMonth = if (isSel) null else d }
                  )
                }
              }
            } else {
              // Full monthly grid
              val totalCells = firstDayOfWeek + daysInMonth
              val rows = (totalCells + 6) / 7
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (r in 0 until rows) {
                  Row(modifier = Modifier.fillMaxWidth()) {
                    for (c in 0 until 7) {
                      val cellIdx = r * 7 + c
                      val d = cellIdx - firstDayOfWeek + 1
                      if (d in 1..daysInMonth) {
                        val dayBills = billsForDay(d)
                        val isSel = selectedDayOfMonth == d
                        CalendarDayCell(
                          day = d,
                          selected = isSel,
                          dotColors = dayBills.map { dotColorForBill(it) },
                          modifier = Modifier.weight(1f),
                          onClick = { selectedDayOfMonth = if (isSel) null else d }
                        )
                      } else {
                        Spacer(modifier = Modifier.weight(1f))
                      }
                    }
                  }
                }
              }
            }

            if (selectedDayOfMonth != null) {
              Spacer(modifier = Modifier.height(6.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Filtered to day $selectedDayOfMonth",
                  fontSize = 11.sp,
                  color = BillTeal,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "Clear Filter",
                  fontSize = 11.sp,
                  color = BillMagenta,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.clickable { selectedDayOfMonth = null }
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Next Bill Section
        Text(
          text = if (selectedDayOfMonth != null) "Bills on Day $selectedDayOfMonth" else "Next Bill",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (nextBill == null) {
          NumoNeumorphicCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = BillEmerald, modifier = Modifier.size(36.dp))
              Spacer(modifier = Modifier.height(8.dp))
              Text("All caught up!", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
              Text("No pending bills match this view.", fontSize = 12.sp, color = NumoDS.TextSecondary)
            }
          }
        } else {
          NextBillHighlightCard(
            bill = nextBill,
            onOpenDetail = { selectedBillForDetail = nextBill },
            onPayNow = { billToPay = nextBill }
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Other Bills Header with working See All >
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Other Bills",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable { showAllBillsDirectory = true }
              .padding(horizontal = 4.dp, vertical = 2.dp)
          ) {
            Text("See All", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NumoDS.TextSecondary)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = NumoDS.TextSecondary, modifier = Modifier.size(16.dp))
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (otherBills.isEmpty() && nextBill != null) {
          Text(
            text = "Tap 'See All' to view paid history or add a new recurring bill.",
            fontSize = 12.sp,
            color = NumoDS.TextSecondary
          )
        } else {
          otherBills.forEach { bill ->
            OtherBillRowCard(
              bill = bill,
              onClick = { selectedBillForDetail = bill }
            )
            Spacer(modifier = Modifier.height(10.dp))
          }
        }

        Spacer(modifier = Modifier.height(90.dp))
      }

      // Prominent Circular "+" Add Bill Button
      FloatingActionButton(
        onClick = {
          editingBill = null
          showAddBillModal = true
        },
        shape = CircleShape,
        containerColor = BillMagenta,
        contentColor = Color.White,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 22.dp, bottom = 24.dp)
          .size(56.dp)
          .shadow(8.dp, CircleShape)
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Bill", modifier = Modifier.size(28.dp))
      }
    }
  }

  // Bill Details Modal (Matching screenshot #6)
  selectedBillForDetail?.let { detailBill ->
    val liveBill = bills.firstOrNull { it.id == detailBill.id } ?: detailBill
    BillDetailsDialog(
      bill = liveBill,
      onDismiss = { selectedBillForDetail = null },
      onPayNow = {
        selectedBillForDetail = null
        billToPay = liveBill
      },
      onEdit = {
        selectedBillForDetail = null
        editingBill = liveBill
        showAddBillModal = true
      },
      onDelete = {
        NumoFinancialEngine.deleteBill(liveBill.id)
        selectedBillForDetail = null
      }
    )
  }

  // Pay Bill Modal (Supports partial & split payments + fee + receipt)
  billToPay?.let { targetBill ->
    val liveBill = bills.firstOrNull { it.id == targetBill.id } ?: targetBill
    PayBillModal(
      bill = liveBill,
      onDismiss = { billToPay = null },
      onPaid = { billToPay = null }
    )
  }

  // Add / Edit Bill Modal
  if (showAddBillModal) {
    AddOrEditBillDialog(
      existing = editingBill,
      onDismiss = { showAddBillModal = false },
      onSaved = { showAddBillModal = false }
    )
  }

  // Full Bill Directory Modal (Upcoming / Overdue / Paid History)
  if (showAllBillsDirectory) {
    AllBillsDirectoryDialog(
      onDismiss = { showAllBillsDirectory = false },
      onSelectBill = { b ->
        showAllBillsDirectory = false
        selectedBillForDetail = b
      }
    )
  }
}

@Composable
private fun CalendarDayCell(
  day: Int,
  selected: Boolean,
  dotColors: List<Color>,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .clickable(onClick = onClick)
      .padding(vertical = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(30.dp)
        .clip(CircleShape)
        .background(if (selected) BillTeal else Color.Transparent),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = day.toString(),
        fontSize = 13.sp,
        fontWeight = if (selected || dotColors.isNotEmpty()) FontWeight.Bold else FontWeight.Medium,
        color = if (selected) Color.White else NumoDS.TextPrimary
      )
    }
    Spacer(modifier = Modifier.height(2.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.height(6.dp)) {
      dotColors.take(3).forEach { c ->
        Box(
          modifier = Modifier
            .size(5.dp)
            .clip(CircleShape)
            .background(c)
        )
      }
    }
  }
}

@Composable
private fun NextBillHighlightCard(
  bill: FinancialBill,
  onOpenDetail: () -> Unit,
  onPayNow: () -> Unit
) {
  NumoNeumorphicCard(
    modifier = Modifier.fillMaxWidth(),
    cornerRadius = 20.dp,
    onClick = onOpenDetail
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        NumoClayIconBadge(
          icon = iconForBillCategory(bill.category, bill.title),
          topColor = Color(0xFF38BDF8),
          bottomColor = BillTeal,
          size = 44.dp,
          iconSize = 22.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(bill.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
          Text(
            text = NumoFinancialEngine.formatDueRelative(bill.dueDateMillis, bill.isPaid),
            fontSize = 12.sp,
            color = NumoDS.TextSecondary
          )
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = NumoFinancialEngine.formatKes(bill.remainingAmount, false),
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = NumoDS.TextPrimary
          )
          if (bill.paidAmount > 0 && !bill.isPaid) {
            Text(
              text = "Paid ${NumoFinancialEngine.formatKes(bill.paidAmount, false)} of ${NumoFinancialEngine.formatKes(bill.amount, false)}",
              fontSize = 11.sp,
              color = BillEmerald
            )
          }
        }
        Button(
          onClick = onPayNow,
          shape = RoundedCornerShape(20.dp),
          colors = ButtonDefaults.buttonColors(containerColor = BillEmerald)
        ) {
          Text("Pay Now", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    }
  }
}

@Composable
private fun OtherBillRowCard(
  bill: FinancialBill,
  onClick: () -> Unit
) {
  NumoNeumorphicCard(
    modifier = Modifier.fillMaxWidth(),
    cornerRadius = 18.dp,
    onClick = onClick
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      NumoClayIconBadge(
        icon = iconForBillCategory(bill.category, bill.title),
        topColor = Color(0xFFFBBF24),
        bottomColor = Color(0xFFD97706),
        size = 42.dp,
        iconSize = 20.dp
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(bill.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
        Text(
          text = "Due ${NumoFinancialEngine.formatLocalDate(bill.dueDateMillis)}",
          fontSize = 11.sp,
          color = NumoDS.TextSecondary
        )
      }
      Text(
        text = NumoFinancialEngine.formatKes(bill.remainingAmount, false),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = NumoDS.TextPrimary
      )
    }
  }
}

@Composable
private fun BillDetailsDialog(
  bill: FinancialBill,
  onDismiss: () -> Unit,
  onPayNow: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  var showOverflow by remember { mutableStateOf(false) }
  var showDeleteConfirm by remember { mutableStateOf(false) }
  val payments = NumoFinancialEngine.billPayments.filter { it.billId == bill.id || it.billTitle.equals(bill.title, ignoreCase = true) }

  val catIdx = NumoFinancialEngine.resolveBudgetCategoryIndex(bill.category)
  val budgetItem = if (catIdx != -1) NumoFinancialEngine.categories[catIdx] else null
  val budgetRemaining = budgetItem?.let { (it.limit - it.spent).coerceAtLeast(0.0) } ?: 0.0
  val budgetPct = budgetItem?.percentage?.coerceIn(0, 100) ?: 0

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(26.dp),
      color = NumoDS.CardSurface,
      shadowElevation = 14.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onDismiss, modifier = Modifier.size(34.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close")
          }
          Text("Bill Details", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
          Box {
            IconButton(onClick = { showOverflow = true }, modifier = Modifier.size(34.dp)) {
              Icon(Icons.Default.MoreHoriz, contentDescription = "Options")
            }
            DropdownMenu(expanded = showOverflow, onDismissRequest = { showOverflow = false }) {
              DropdownMenuItem(
                text = { Text("Edit Bill") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                onClick = {
                  showOverflow = false
                  onEdit()
                }
              )
              DropdownMenuItem(
                text = { Text("Delete Bill", color = BillMagenta) },
                leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = BillMagenta) },
                onClick = {
                  showOverflow = false
                  showDeleteConfirm = true
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        NumoClayIconBadge(
          icon = iconForBillCategory(bill.category, bill.title),
          topColor = Color(0xFF7DD3FC),
          bottomColor = BillTeal,
          size = 64.dp,
          iconSize = 30.dp
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text(bill.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
        Text(bill.category, fontSize = 13.sp, color = NumoDS.TextSecondary)

        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = if (bill.isPaid) BillEmerald.copy(alpha = 0.14f) else BillMagenta.copy(alpha = 0.12f)
        ) {
          Text(
            text = NumoFinancialEngine.formatDueRelative(bill.dueDateMillis, bill.isPaid),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (bill.isPaid) BillEmerald else BillMagenta,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = NumoFinancialEngine.formatKes(bill.remainingAmount.takeIf { !bill.isPaid } ?: bill.amount, false),
          fontSize = 28.sp,
          fontWeight = FontWeight.ExtraBold,
          color = NumoDS.TextPrimary
        )
        Text(
          text = "Status: ${if (bill.isPaid) "Paid" else if (bill.paidAmount > 0) "Partially Paid" else "Unpaid"}",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = if (bill.isPaid) BillEmerald else BillMagenta
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Budget Impact Card
        NumoNeumorphicCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Budget Impact", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = budgetItem?.name ?: bill.category,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = NumoDS.TextSecondary
            )
            Text(
              text = "${NumoFinancialEngine.formatKes(budgetRemaining, false)} remaining",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = BillEmerald
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              LinearProgressIndicator(
                progress = { (budgetPct / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                  .weight(1f)
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp)),
                color = if (budgetPct >= 90) BillMagenta else BillEmerald,
                trackColor = NumoDS.TrackBg
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("$budgetPct%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Payment History Card
        NumoNeumorphicCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Payment History", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            if (payments.isEmpty()) {
              Text("No payments recorded yet.", fontSize = 12.sp, color = NumoDS.TextSecondary)
            } else {
              payments.take(4).forEach { rec ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BillEmerald, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(rec.dateText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
                    Text(rec.account, fontSize = 11.sp, color = NumoDS.TextSecondary)
                  }
                  Text(
                    text = NumoFinancialEngine.formatKes(rec.amountPaid, false),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NumoDS.TextPrimary
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!bill.isPaid) {
          Button(
            onClick = onPayNow,
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BillMagenta),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
          ) {
            Text("Pay Bill Now", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          }
        }
      }
    }
  }

  if (showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirm = false },
      title = { Text("Delete ${bill.title}?") },
      text = { Text("This removes the scheduled bill without deleting past payment transactions.") },
      confirmButton = {
        TextButton(onClick = {
          showDeleteConfirm = false
          onDelete()
        }) { Text("Delete", color = BillMagenta, fontWeight = FontWeight.Bold) }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
      }
    )
  }
}

@Composable
private fun PayBillModal(
  bill: FinancialBill,
  onDismiss: () -> Unit,
  onPaid: () -> Unit
) {
  val context = LocalContext.current
  val accounts = NumoFinancialEngine.accounts
  var amountText by remember { mutableStateOf(bill.remainingAmount.toInt().toString()) }
  var feeText by remember { mutableStateOf("0") }
  var splitMode by remember { mutableStateOf(false) }
  var selectedAccount by remember { mutableStateOf(bill.defaultAccount) }
  val splitAmounts = remember {
    mutableStateMapOf<String, String>().apply {
      accounts.forEachIndexed { idx, acc ->
        put(acc.name, if (idx == 0) bill.remainingAmount.toInt().toString() else "0")
      }
    }
  }
  var receiptUri by remember { mutableStateOf<String?>(null) }
  var errorText by remember { mutableStateOf<String?>(null) }

  val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
    if (uri != null) {
      runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
      receiptUri = uri.toString()
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(shape = RoundedCornerShape(24.dp), color = NumoDS.CardSurface, modifier = Modifier.fillMaxWidth()) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(20.dp)
      ) {
        Text("Pay ${bill.title}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
        Text("Remaining: ${NumoFinancialEngine.formatKes(bill.remainingAmount, false)}", fontSize = 12.sp, color = NumoDS.TextSecondary)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Split across accounts", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          Switch(checked = splitMode, onCheckedChange = { splitMode = it })
        }

        if (!splitMode) {
          OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' }; errorText = null },
            label = { Text("Payment Amount (Partial or Full)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text("Pay From", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NumoDS.TextSecondary)
          Spacer(modifier = Modifier.height(4.dp))
          Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            accounts.forEach { acc ->
              FilterChip(
                selected = selectedAccount.equals(acc.name, ignoreCase = true),
                onClick = { selectedAccount = acc.name; errorText = null },
                label = { Text("${acc.name} (${NumoFinancialEngine.formatKes(acc.balance, false)})") }
              )
            }
          }
        } else {
          accounts.forEach { acc ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(acc.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(NumoFinancialEngine.formatKes(acc.balance, false), fontSize = 11.sp, color = NumoDS.TextSecondary)
              }
              OutlinedTextField(
                value = splitAmounts[acc.name] ?: "0",
                onValueChange = { splitAmounts[acc.name] = it.filter { c -> c.isDigit() || c == '.' }; errorText = null },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.width(120.dp),
                shape = RoundedCornerShape(10.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = feeText,
          onValueChange = { feeText = it.filter { c -> c.isDigit() || c == '.' } },
          label = { Text("Transaction Fee (Optional)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(if (receiptUri != null) "Receipt attached" else "Attach receipt (optional)", fontSize = 12.sp, color = NumoDS.TextSecondary)
          TextButton(onClick = { launcher.launch(arrayOf("image/*", "application/pdf")) }) {
            Text(if (receiptUri != null) "Change" else "Attach", color = BillTeal, fontWeight = FontWeight.Bold)
          }
        }

        if (errorText != null) {
          Text(errorText!!, color = BillMagenta, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
            Text("Cancel")
          }
          Button(
            onClick = {
              val fee = feeText.toDoubleOrNull() ?: 0.0
              val splits = if (!splitMode) {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                listOf(selectedAccount to amt)
              } else {
                accounts.mapNotNull { acc ->
                  val amt = splitAmounts[acc.name]?.toDoubleOrNull() ?: 0.0
                  if (amt > 0.0) acc.name to amt else null
                }
              }
              val res = NumoFinancialEngine.payBillAdvanced(
                billId = bill.id,
                splits = splits,
                fee = fee,
                receiptUri = receiptUri
              )
              res.onSuccess { onPaid() }.onFailure { errorText = it.message }
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = BillEmerald),
            shape = RoundedCornerShape(14.dp)
          ) {
            Text("Confirm Pay", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddOrEditBillDialog(
  existing: FinancialBill?,
  onDismiss: () -> Unit,
  onSaved: () -> Unit
) {
  val presets = listOf(
    Triple("Rent", "Housing", 15000.0),
    Triple("KPLC Tokens", "Bills & Utilities", 2500.0),
    Triple("Water", "Bills & Utilities", 1200.0),
    Triple("Safaricom Home Fibre", "Bills & Utilities", 3000.0),
    Triple("Zuku", "Bills & Utilities", 2799.0),
    Triple("NHIF / SHIF", "Health & Fitness", 500.0)
  )

  var title by remember { mutableStateOf(existing?.title ?: "") }
  var category by remember { mutableStateOf(existing?.category ?: "Bills & Utilities") }
  var amountText by remember { mutableStateOf(existing?.amount?.toInt()?.toString() ?: "") }
  var accountNumber by remember { mutableStateOf(existing?.accountNumber ?: "") }
  var dueDateMillis by remember { mutableStateOf(existing?.dueDateMillis ?: (System.currentTimeMillis() + 3L * 86_400_000L)) }
  var recurrence by remember { mutableStateOf(existing?.recurrence ?: RecurrenceFrequency.MONTHLY) }
  var reminderEnabled by remember { mutableStateOf(existing?.reminderEnabled ?: true) }
  var reminderLeadDays by remember { mutableIntStateOf(existing?.reminderLeadDays ?: 3) }
  var defaultAccount by remember { mutableStateOf(existing?.defaultAccount ?: "M-PESA") }
  var autoPayPref by remember { mutableStateOf(existing?.autoPayPreference ?: false) }
  var showDatePicker by remember { mutableStateOf(false) }
  var errorText by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(shape = RoundedCornerShape(24.dp), color = NumoDS.CardSurface, modifier = Modifier.fillMaxWidth()) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(18.dp)
      ) {
        Text(
          text = if (existing == null) "Add New Bill" else "Edit Bill",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (existing == null) {
          Text("Quick Presets", fontSize = 11.sp, color = NumoDS.TextSecondary, fontWeight = FontWeight.SemiBold)
          Spacer(modifier = Modifier.height(4.dp))
          Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            presets.forEach { (pName, pCat, pAmt) ->
              AssistChip(
                onClick = {
                  title = pName
                  category = pCat
                  if (amountText.isBlank()) amountText = pAmt.toInt().toString()
                },
                label = { Text(pName, fontSize = 11.sp) }
              )
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
          value = title,
          onValueChange = { title = it; errorText = null },
          label = { Text("Bill Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = amountText,
          onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' }; errorText = null },
          label = { Text("Amount (KES)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = accountNumber,
          onValueChange = { accountNumber = it },
          label = { Text("Paybill / Account Ref (Optional)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { showDatePicker = true }
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Due Date", fontSize = 13.sp, color = NumoDS.TextSecondary)
          Text(NumoFinancialEngine.formatLocalDate(dueDateMillis), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Recurrence", fontSize = 12.sp, color = NumoDS.TextSecondary, fontWeight = FontWeight.SemiBold)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf(RecurrenceFrequency.ONE_TIME, RecurrenceFrequency.MONTHLY, RecurrenceFrequency.QUARTERLY, RecurrenceFrequency.ANNUAL).forEach { freq ->
            FilterChip(
              selected = recurrence == freq,
              onClick = { recurrence = freq },
              label = { Text(freq.label, fontSize = 11.sp) }
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Due Reminders ($reminderLeadDays d before)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
        }
        if (reminderEnabled) {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(0 to "Same day", 1 to "1 day", 3 to "3 days", 7 to "7 days").forEach { (d, lbl) ->
              FilterChip(selected = reminderLeadDays == d, onClick = { reminderLeadDays = d }, label = { Text(lbl, fontSize = 11.sp) })
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Auto-Pay Preference", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("Saves preference; bank auto-debit requires external mandate.", fontSize = 10.sp, color = NumoDS.TextSecondary)
          }
          Switch(checked = autoPayPref, onCheckedChange = { autoPayPref = it })
        }

        if (errorText != null) {
          Text(errorText!!, color = BillMagenta, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
            Text("Cancel")
          }
          Button(
            onClick = {
              val amt = amountText.toDoubleOrNull() ?: 0.0
              if (title.isBlank()) {
                errorText = "Enter a bill name."
                return@Button
              }
              if (amt <= 0.0) {
                errorText = "Enter a valid amount."
                return@Button
              }
              if (existing == null) {
                NumoFinancialEngine.addBill(
                  title = title,
                  amount = amt,
                  category = category,
                  dueDateMillis = dueDateMillis,
                  accountNumber = accountNumber,
                  recurrence = recurrence,
                  reminderEnabled = reminderEnabled,
                  reminderLeadDays = reminderLeadDays,
                  defaultAccount = defaultAccount,
                  autoPayPreference = autoPayPref
                )
              } else {
                NumoFinancialEngine.updateBill(
                  billId = existing.id,
                  title = title,
                  amount = amt,
                  category = category,
                  dueDateMillis = dueDateMillis,
                  accountNumber = accountNumber,
                  recurrence = recurrence,
                  reminderEnabled = reminderEnabled,
                  reminderLeadDays = reminderLeadDays,
                  defaultAccount = defaultAccount,
                  autoPayPreference = autoPayPref
                )
              }
              onSaved()
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = BillMagenta),
            shape = RoundedCornerShape(14.dp)
          ) {
            Text("Save Bill", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }

  if (showDatePicker) {
    val state = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis)
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          state.selectedDateMillis?.let { dueDateMillis = it }
          showDatePicker = false
        }) { Text("OK") }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
      }
    ) { DatePicker(state = state) }
  }
}

@Composable
private fun AllBillsDirectoryDialog(
  onDismiss: () -> Unit,
  onSelectBill: (FinancialBill) -> Unit
) {
  var tab by remember { mutableIntStateOf(0) } // 0: Upcoming, 1: Overdue, 2: Paid History
  val now = System.currentTimeMillis()
  val items = when (tab) {
    0 -> NumoFinancialEngine.bills.filter { !it.isPaid && it.dueDateMillis >= now - 86_400_000L }
    1 -> NumoFinancialEngine.bills.filter { !it.isPaid && it.dueDateMillis < now - 86_400_000L }
    else -> NumoFinancialEngine.bills.filter { it.isPaid }
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(shape = RoundedCornerShape(24.dp), color = NumoDS.CardSurface, modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("All Bills", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
          IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("Upcoming", "Overdue", "Paid History").forEachIndexed { i, lbl ->
            FilterChip(selected = tab == i, onClick = { tab = i }, label = { Text(lbl, fontSize = 11.sp) })
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Column(modifier = Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState())) {
          if (items.isEmpty()) {
            Text("No bills in this category.", fontSize = 13.sp, color = NumoDS.TextSecondary, modifier = Modifier.padding(vertical = 20.dp))
          } else {
            items.forEach { bill ->
              OtherBillRowCard(bill = bill, onClick = { onSelectBill(bill) })
              Spacer(modifier = Modifier.height(8.dp))
            }
          }
        }
      }
    }
  }
}

private fun iconForBillCategory(category: String, title: String): ImageVector {
  val combined = "$category $title".lowercase()
  return when {
    combined.contains("rent") || combined.contains("housing") -> Icons.Default.Home
    combined.contains("kplc") || combined.contains("electric") || combined.contains("power") -> Icons.Default.Bolt
    combined.contains("water") -> Icons.Default.WaterDrop
    combined.contains("internet") || combined.contains("fibre") || combined.contains("zuku") -> Icons.Default.Wifi
    else -> Icons.Default.ReceiptLong
  }
}
