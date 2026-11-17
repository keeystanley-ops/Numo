package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.NumoFinancialEngine
import com.example.data.RecurrenceFrequency
import com.example.ui.components.NumoCalculatorModal
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoScreenBackground
import kotlin.math.abs

private data class TxCategoryChoice(
  val label: String,
  val engineName: String,
  val icon: ImageVector,
  val topColor: Color,
  val bottomColor: Color
)

private val ExpenseAccent = Color(0xFFD81B60)
private val IncomeAccent = Color(0xFF1B8A5A)
private val TransferAccent = Color(0xFF00796B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
  currency: String = NumoFinancialEngine.currency,
  initialTab: String = "expense",
  onTabChanged: (String) -> Unit = {},
  onBack: () -> Unit = {},
  onSaved: () -> Unit = onBack
) {
  val context = LocalContext.current
  val accounts = NumoFinancialEngine.accounts

  var typeIndex by remember {
    mutableIntStateOf(
      when (initialTab.lowercase()) {
        "income" -> 1
        "transfer" -> 2
        else -> 0
      }
    )
  }

  LaunchedEffect(initialTab) {
    typeIndex = when (initialTab.lowercase()) {
      "income" -> 1
      "transfer" -> 2
      else -> 0
    }
  }

  val activeAccent by animateColorAsState(
    targetValue = when (typeIndex) {
      1 -> IncomeAccent
      2 -> TransferAccent
      else -> ExpenseAccent
    },
    animationSpec = tween(250),
    label = "txAccent"
  )

  // Shared Form State (preserved when switching tabs)
  var amountText by remember { mutableStateOf("") }
  var descriptionText by remember { mutableStateOf("") }
  var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
  var showDatePicker by remember { mutableStateOf(false) }
  var showCalculator by remember { mutableStateOf(false) }
  var isSubmitting by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Expense State
  val primaryExpenseCategories = remember {
    listOf(
      TxCategoryChoice("Food", "Food & Dining", Icons.Default.Restaurant, Color(0xFFF43F5E), Color(0xFFBE123C)),
      TxCategoryChoice("Transport", "Transport", Icons.Default.DirectionsCar, Color(0xFF38BDF8), Color(0xFF0284C7)),
      TxCategoryChoice("Shopping", "Shopping", Icons.Default.ShoppingCart, Color(0xFFFBBF24), Color(0xFFD97706)),
      TxCategoryChoice("Health", "Health & Fitness", Icons.Default.Favorite, Color(0xFFFB7185), Color(0xFFE11D48))
    )
  }
  val extraExpenseCategories = remember {
    listOf(
      TxCategoryChoice("Utilities", "Bills & Utilities", Icons.Default.Bolt, Color(0xFF2DD4BF), Color(0xFF0F766E)),
      TxCategoryChoice("Fun", "Entertainment", Icons.Default.SportsEsports, Color(0xFFA78BFA), Color(0xFF6D28D9)),
      TxCategoryChoice("Other", "Other", Icons.Default.Category, Color(0xFF94A3B8), Color(0xFF475569))
    )
  }
  var showMoreExpenseCats by remember { mutableStateOf(false) }
  var selectedExpenseCategory by remember { mutableStateOf(primaryExpenseCategories.first()) }
  var selectedExpenseAccount by remember { mutableStateOf(accounts.firstOrNull()?.name ?: "M-PESA") }
  var showExpenseAccountMenu by remember { mutableStateOf(false) }

  // Income State
  val incomeCategories = remember {
    listOf(
      TxCategoryChoice("Salary", "Salary", Icons.Default.Payments, Color(0xFF34D399), Color(0xFF059669)),
      TxCategoryChoice("Freelance", "Freelance", Icons.Default.LaptopMac, Color(0xFF60A5FA), Color(0xFF2563EB)),
      TxCategoryChoice("Invest", "Investments", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF2DD4BF), Color(0xFF0F766E)),
      TxCategoryChoice("Business", "Business", Icons.Default.Storefront, Color(0xFFFBBF24), Color(0xFFD97706)),
      TxCategoryChoice("Other", "Other", Icons.Default.AddCircleOutline, Color(0xFF94A3B8), Color(0xFF475569))
    )
  }
  var selectedIncomeCategory by remember { mutableStateOf(incomeCategories.first()) }
  var selectedIncomeAccount by remember { mutableStateOf(accounts.getOrNull(1)?.name ?: "KCB") }
  var showIncomeAccountMenu by remember { mutableStateOf(false) }

  // Tax / Deductions State
  var taxSectionExpanded by remember { mutableStateOf(true) }
  var applyTaxDeductions by remember { mutableStateOf(false) }
  var deductionIsPercent by remember { mutableStateOf(true) }
  var taxPercentValue by remember { mutableStateOf("18") }
  var taxFlatValue by remember { mutableStateOf("0") }
  var extraDeductions by remember { mutableStateOf(listOf<Pair<String, String>>()) }

  // Split Deposit Across Accounts
  var splitDepositEnabled by remember { mutableStateOf(false) }
  var splitIsPercent by remember { mutableStateOf(true) }
  val splitValues = remember {
    mutableStateMapOf<String, String>().apply {
      accounts.forEachIndexed { idx, acc ->
        put(acc.name, if (idx == 0) "50" else if (idx == 1) "50" else "0")
      }
    }
  }

  // Recurring Income
  var recurringIncomeEnabled by remember { mutableStateOf(false) }
  var incomeFrequency by remember { mutableStateOf(RecurrenceFrequency.MONTHLY) }

  // Document / Receipt Attachment
  var attachedReceiptUri by remember { mutableStateOf<String?>(null) }
  val receiptLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri: Uri? ->
    if (uri != null) {
      runCatching {
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      attachedReceiptUri = uri.toString()
    }
  }

  // Transfer State
  var fromAccount by remember { mutableStateOf(accounts.getOrNull(1)?.name ?: "KCB") }
  var toAccount by remember { mutableStateOf(accounts.firstOrNull()?.name ?: "M-PESA") }
  var showFromPicker by remember { mutableStateOf(false) }
  var showToPicker by remember { mutableStateOf(false) }
  var transferFeeText by remember { mutableStateOf("") }
  var transferReference by remember { mutableStateOf("") }
  var recurringTransferEnabled by remember { mutableStateOf(false) }
  var transferFrequency by remember { mutableStateOf(RecurrenceFrequency.MONTHLY) }
  var showTransferReviewModal by remember { mutableStateOf(false) }

  val grossAmount = amountText.toDoubleOrNull() ?: 0.0
  val primaryDeduction = if (!applyTaxDeductions) {
    0.0
  } else if (deductionIsPercent) {
    val pct = (taxPercentValue.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0)
    grossAmount * (pct / 100.0)
  } else {
    (taxFlatValue.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
  }
  val extraDeductionsSum = if (!applyTaxDeductions) 0.0 else extraDeductions.sumOf {
    val v = it.second.toDoubleOrNull() ?: 0.0
    if (deductionIsPercent) grossAmount * (v.coerceIn(0.0, 100.0) / 100.0) else v.coerceAtLeast(0.0)
  }
  val totalDeductions = (primaryDeduction + extraDeductionsSum).coerceAtMost(grossAmount)
  val netIncome = (grossAmount - totalDeductions).coerceAtLeast(0.0)

  val transferFee = transferFeeText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
  val totalSourceDebit = grossAmount + transferFee

  NumoScreenBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      // Top Navigation Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = NumoDS.TextPrimary
            )
          }
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Add Transaction",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )
        }
        TextButton(onClick = onBack) {
          Text(
            text = "Cancel",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = NumoDS.TextSecondary
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 3-Mode Segmented Pill Switcher
      NumoNeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 26.dp,
        backgroundColor = Color(0xFFF6ECDD)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          val modes = listOf("Expense" to "expense", "Income" to "income", "Transfer" to "transfer")
          modes.forEachIndexed { idx, (label, key) ->
            val selected = typeIndex == idx
            val pillColor = when {
              !selected -> Color.Transparent
              idx == 0 -> ExpenseAccent
              idx == 1 -> IncomeAccent
              else -> TransferAccent
            }
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(pillColor)
                .clickable {
                  typeIndex = idx
                  errorMessage = null
                  onTabChanged(key)
                }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected) Color.White else NumoDS.TextPrimary
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Transfer From / To Cards (Shown above amount in Transfer Mode matching screenshot)
      if (typeIndex == 2) {
        val fromBal = NumoFinancialEngine.getAccountBalance(fromAccount)
        val toBal = NumoFinancialEngine.getAccountBalance(toAccount)
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          TransferAccountMiniCard(
            label = "From Account",
            accountName = fromAccount,
            balanceText = NumoFinancialEngine.formatKes(fromBal, false),
            accent = TransferAccent,
            modifier = Modifier.weight(1f),
            onClick = { showFromPicker = true }
          )

          Box(
            modifier = Modifier
              .size(38.dp)
              .shadow(4.dp, CircleShape)
              .clip(CircleShape)
              .background(Color.White)
              .clickable {
                val tmp = fromAccount
                fromAccount = toAccount
                toAccount = tmp
                errorMessage = null
              },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.SwapHoriz,
              contentDescription = "Swap Accounts",
              tint = TransferAccent,
              modifier = Modifier.size(20.dp)
            )
          }

          TransferAccountMiniCard(
            label = "To Account",
            accountName = toAccount,
            balanceText = NumoFinancialEngine.formatKes(toBal, false),
            accent = TransferAccent,
            modifier = Modifier.weight(1f),
            onClick = { showToPicker = true }
          )
        }

        if (fromAccount.equals(toAccount, ignoreCase = true)) {
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Source and destination accounts cannot be the same.",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ExpenseAccent
          )
        }
        Spacer(modifier = Modifier.height(14.dp))
      }

      // Amount Card with Direct Input & Working Calculator Button
      val amountLabel = when (typeIndex) {
        1 -> "Income Amount"
        2 -> "Transfer Amount"
        else -> "Amount"
      }
      Text(
        text = amountLabel,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = NumoDS.TextSecondary
      )
      Spacer(modifier = Modifier.height(6.dp))
      NumoNeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        backgroundColor = Color(0xFFFDF8F0)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Text(
              text = currency,
              fontSize = 24.sp,
              fontWeight = FontWeight.ExtraBold,
              color = activeAccent
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
              value = amountText,
              onValueChange = { input ->
                val filtered = input.filter { it.isDigit() || it == '.' }
                if (filtered.count { it == '.' } <= 1) {
                  amountText = filtered
                  errorMessage = null
                }
              },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              cursorBrush = SolidColor(activeAccent),
              textStyle = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NumoDS.TextPrimary
              ),
              decorationBox = { innerTextField ->
                if (amountText.isEmpty()) {
                  Text(
                    text = "0",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NumoDS.TextMuted
                  )
                }
                innerTextField()
              },
              modifier = Modifier.fillMaxWidth()
            )
          }

          Box(
            modifier = Modifier
              .size(38.dp)
              .shadow(3.dp, RoundedCornerShape(10.dp))
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFFF59E0B))
              .clickable { showCalculator = true },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Calculate,
              contentDescription = "Open Calculator",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // ==================== MODE 0: EXPENSE ====================
      if (typeIndex == 0) {
        Text(
          text = "Category",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          primaryExpenseCategories.forEach { cat ->
            CategoryBubbleItem(
              choice = cat,
              selected = selectedExpenseCategory.label == cat.label,
              accentColor = activeAccent,
              onClick = { selectedExpenseCategory = cat }
            )
          }
          // "+ More" toggle
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { showMoreExpenseCats = !showMoreExpenseCats }
          ) {
            Box(
              modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (showMoreExpenseCats) activeAccent.copy(alpha = 0.14f) else NumoDS.CardSurface)
                .border(1.dp, Color.White, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (showMoreExpenseCats) Icons.Default.ExpandLess else Icons.Default.Add,
                contentDescription = "More Categories",
                tint = if (showMoreExpenseCats) activeAccent else NumoDS.TextPrimary
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (showMoreExpenseCats) "Less" else "More",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = NumoDS.TextSecondary
            )
          }
        }

        AnimatedVisibility(visible = showMoreExpenseCats) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 10.dp)
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            extraExpenseCategories.forEach { cat ->
              CategoryBubbleItem(
                choice = cat,
                selected = selectedExpenseCategory.label == cat.label,
                accentColor = activeAccent,
                onClick = { selectedExpenseCategory = cat }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Account Selector with Balances
        Text(
          text = "Account",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = NumoDS.TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box {
          val curBal = NumoFinancialEngine.getAccountBalance(selectedExpenseAccount)
          NumoNeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp,
            onClick = { showExpenseAccountMenu = true }
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = activeAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(selectedExpenseAccount, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
              }
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = NumoFinancialEngine.formatKes(curBal),
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = NumoDS.TextSecondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NumoDS.TextSecondary)
              }
            }
          }
          DropdownMenu(
            expanded = showExpenseAccountMenu,
            onDismissRequest = { showExpenseAccountMenu = false }
          ) {
            accounts.forEach { acc ->
              DropdownMenuItem(
                text = {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(acc.name, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(NumoFinancialEngine.formatKes(acc.balance), color = NumoDS.TextSecondary)
                  }
                },
                onClick = {
                  selectedExpenseAccount = acc.name
                  showExpenseAccountMenu = false
                  errorMessage = null
                }
              )
            }
          }
        }
      }

      // ==================== MODE 1: INCOME ====================
      if (typeIndex == 1) {
        Text(
          text = "Income Category",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          incomeCategories.forEach { cat ->
            CategoryBubbleItem(
              choice = cat,
              selected = selectedIncomeCategory.label == cat.label,
              accentColor = activeAccent,
              onClick = { selectedIncomeCategory = cat }
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Collapsible Tax / Deductions Card
        NumoNeumorphicCard(
          modifier = Modifier.fillMaxWidth(),
          cornerRadius = 18.dp
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { taxSectionExpanded = !taxSectionExpanded },
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (taxSectionExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.ChevronRight,
                  contentDescription = null,
                  tint = NumoDS.TextPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Tax / Deductions",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = NumoDS.TextPrimary
                )
              }
              Switch(
                checked = applyTaxDeductions,
                onCheckedChange = { applyTaxDeductions = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = activeAccent)
              )
            }

            AnimatedVisibility(visible = taxSectionExpanded && applyTaxDeductions) {
              Column(modifier = Modifier.padding(top = 10.dp)) {
                Text("Deduction Type", fontSize = 12.sp, color = NumoDS.TextSecondary, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NumoDS.CardSurface)
                    .padding(3.dp)
                ) {
                  listOf(true to "Percentage", false to "Fixed Amount").forEach { (isPct, label) ->
                    val sel = deductionIsPercent == isPct
                    Box(
                      modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (sel) Color(0xFF4E342E) else Color.Transparent)
                        .clickable { deductionIsPercent = isPct }
                        .padding(vertical = 8.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (sel) Color.White else NumoDS.TextPrimary
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  OutlinedTextField(
                    value = if (deductionIsPercent) taxPercentValue else taxFlatValue,
                    onValueChange = { v ->
                      val clean = v.filter { it.isDigit() || it == '.' }
                      if (deductionIsPercent) taxPercentValue = clean else taxFlatValue = clean
                    },
                    label = { Text(if (deductionIsPercent) "Percentage (%)" else "Flat ($currency)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  if (deductionIsPercent) {
                    IconButton(onClick = {
                      val cur = (taxPercentValue.toDoubleOrNull() ?: 0.0) - 1.0
                      taxPercentValue = cur.coerceAtLeast(0.0).toInt().toString()
                    }) {
                      Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", tint = NumoDS.TextPrimary)
                    }
                    IconButton(onClick = {
                      val cur = (taxPercentValue.toDoubleOrNull() ?: 0.0) + 1.0
                      taxPercentValue = cur.coerceAtMost(100.0).toInt().toString()
                    }) {
                      Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = activeAccent)
                    }
                  }
                }

                // Additional deduction entries
                extraDeductions.forEachIndexed { idx, (label, valStr) ->
                  Spacer(modifier = Modifier.height(8.dp))
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                      value = label,
                      onValueChange = { newL ->
                        extraDeductions = extraDeductions.toMutableList().also { it[idx] = newL to valStr }
                      },
                      label = { Text("Label (e.g. NSSF)") },
                      singleLine = true,
                      modifier = Modifier.weight(1f),
                      shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                      value = valStr,
                      onValueChange = { newV ->
                        extraDeductions = extraDeductions.toMutableList().also {
                          it[idx] = label to newV.filter { c -> c.isDigit() || c == '.' }
                        }
                      },
                      label = { Text(if (deductionIsPercent) "%" else currency) },
                      singleLine = true,
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                      modifier = Modifier.width(90.dp),
                      shape = RoundedCornerShape(10.dp)
                    )
                    IconButton(onClick = {
                      extraDeductions = extraDeductions.toMutableList().also { it.removeAt(idx) }
                    }) {
                      Icon(Icons.Default.Close, contentDescription = "Remove", tint = ExpenseAccent)
                    }
                  }
                }

                TextButton(
                  onClick = { extraDeductions = extraDeductions + ("Other Deduction" to "0") },
                  contentPadding = PaddingValues(0.dp)
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, tint = activeAccent, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Add another deduction", fontSize = 12.sp, color = activeAccent, fontWeight = FontWeight.SemiBold)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = NumoDS.TrackBg)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("Gross Income", fontSize = 12.sp, color = NumoDS.TextSecondary)
                  Text(NumoFinancialEngine.formatKes(grossAmount, false), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("Deductions", fontSize = 12.sp, color = NumoDS.TextSecondary)
                  Text("-${NumoFinancialEngine.formatKes(totalDeductions, false)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ExpenseAccent)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("Net Credited", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
                  Text(NumoFinancialEngine.formatKes(netIncome, false), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = activeAccent)
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Destination Account OR Split Deposit Across Accounts
        NumoNeumorphicCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Split Deposit Across Accounts", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
              Switch(
                checked = splitDepositEnabled,
                onCheckedChange = { splitDepositEnabled = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = activeAccent)
              )
            }

            if (!splitDepositEnabled) {
              Spacer(modifier = Modifier.height(6.dp))
              Box {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NumoDS.CardSurface)
                    .clickable { showIncomeAccountMenu = true }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("Deposit to: $selectedIncomeAccount", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NumoDS.TextPrimary)
                  Text(NumoFinancialEngine.formatKes(NumoFinancialEngine.getAccountBalance(selectedIncomeAccount), false), fontSize = 12.sp, color = NumoDS.TextSecondary)
                }
                DropdownMenu(expanded = showIncomeAccountMenu, onDismissRequest = { showIncomeAccountMenu = false }) {
                  accounts.forEach { acc ->
                    DropdownMenuItem(
                      text = { Text("${acc.name} (${NumoFinancialEngine.formatKes(acc.balance, false)})") },
                      onClick = {
                        selectedIncomeAccount = acc.name
                        showIncomeAccountMenu = false
                      }
                    )
                  }
                }
              }
            } else {
              Spacer(modifier = Modifier.height(8.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                  selected = splitIsPercent,
                  onClick = { splitIsPercent = true },
                  label = { Text("Percentage (%)") }
                )
                FilterChip(
                  selected = !splitIsPercent,
                  onClick = { splitIsPercent = false },
                  label = { Text("Fixed ($currency)") }
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              accounts.forEach { acc ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(acc.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                  OutlinedTextField(
                    value = splitValues[acc.name] ?: "0",
                    onValueChange = { v ->
                      splitValues[acc.name] = v.filter { it.isDigit() || it == '.' }
                      errorMessage = null
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    suffix = { Text(if (splitIsPercent) "%" else currency, fontSize = 11.sp) },
                    modifier = Modifier.width(130.dp),
                    shape = RoundedCornerShape(10.dp)
                  )
                }
              }
              val sumVal = accounts.sumOf { (splitValues[it.name]?.toDoubleOrNull() ?: 0.0) }
              val targetSum = if (splitIsPercent) 100.0 else netIncome
              val validSplit = abs(sumVal - targetSum) < 0.5
              Text(
                text = if (splitIsPercent) "Total: ${sumVal.toInt()}% / 100%" else "Total: ${NumoFinancialEngine.formatKes(sumVal, false)} / ${NumoFinancialEngine.formatKes(netIncome, false)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (validSplit) activeAccent else ExpenseAccent
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Recurring Income & Document Attachment
        NumoNeumorphicCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Recurring Income", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
              Switch(
                checked = recurringIncomeEnabled,
                onCheckedChange = { recurringIncomeEnabled = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = activeAccent)
              )
            }
            AnimatedVisibility(visible = recurringIncomeEnabled) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 6.dp)
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                listOf(RecurrenceFrequency.WEEKLY, RecurrenceFrequency.BI_WEEKLY, RecurrenceFrequency.MONTHLY).forEach { freq ->
                  FilterChip(
                    selected = incomeFrequency == freq,
                    onClick = { incomeFrequency = freq },
                    label = { Text(freq.label) }
                  )
                }
              }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = NumoDS.TrackBg)

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.AttachFile, contentDescription = null, tint = activeAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (attachedReceiptUri != null) "Receipt attached" else "Attach Document / Receipt",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium,
                  color = NumoDS.TextPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
              if (attachedReceiptUri != null) {
                TextButton(onClick = { attachedReceiptUri = null }) {
                  Text("Remove", color = ExpenseAccent, fontSize = 12.sp)
                }
              } else {
                TextButton(onClick = { receiptLauncher.launch(arrayOf("image/*", "application/pdf")) }) {
                  Text("Browse", color = activeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }

      // ==================== MODE 2: TRANSFER ====================
      if (typeIndex == 2) {
        Text("Transfer Fee (Optional)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NumoDS.TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        NumoNeumorphicCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(currency, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextSecondary)
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
              value = transferFeeText,
              onValueChange = { v ->
                val clean = v.filter { it.isDigit() || it == '.' }
                if (clean.count { it == '.' } <= 1) transferFeeText = clean
              },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary),
              decorationBox = { inner ->
                if (transferFeeText.isEmpty()) Text("0", fontSize = 16.sp, color = NumoDS.TextMuted)
                inner()
              },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Reference (Optional)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NumoDS.TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        NumoNeumorphicCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
          BasicTextField(
            value = transferReference,
            onValueChange = { transferReference = it },
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = NumoDS.TextPrimary),
            decorationBox = { inner ->
              if (transferReference.isEmpty()) Text("e.g. Rent Share", fontSize = 14.sp, color = NumoDS.TextMuted)
              inner()
            },
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 14.dp)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val fromBalance = NumoFinancialEngine.getAccountBalance(fromAccount)
        val exceedsSource = totalSourceDebit > fromBalance
        NumoNeumorphicCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Total Source Debit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NumoDS.TextPrimary)
              Text(
                text = NumoFinancialEngine.formatKes(totalSourceDebit, false),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (exceedsSource) ExpenseAccent else TransferAccent
              )
            }
            if (exceedsSource) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Exceeds $fromAccount balance (${NumoFinancialEngine.formatKes(fromBalance, false)})",
                fontSize = 11.sp,
                color = ExpenseAccent
              )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = NumoDS.TrackBg)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Autorenew, contentDescription = null, tint = TransferAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Schedule Recurring Transfer", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NumoDS.TextPrimary)
              }
              Switch(
                checked = recurringTransferEnabled,
                onCheckedChange = { recurringTransferEnabled = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TransferAccent)
              )
            }
            AnimatedVisibility(visible = recurringTransferEnabled) {
              Row(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                listOf(RecurrenceFrequency.WEEKLY, RecurrenceFrequency.MONTHLY).forEach { freq ->
                  FilterChip(
                    selected = transferFrequency == freq,
                    onClick = { transferFrequency = freq },
                    label = { Text(freq.label) }
                  )
                }
              }
            }
          }
        }
      }

      // Shared Date & Description Fields (for Expense & Income)
      if (typeIndex != 2) {
        Spacer(modifier = Modifier.height(14.dp))
        Text("Date", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NumoDS.TextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        NumoNeumorphicCard(
          modifier = Modifier.fillMaxWidth(),
          cornerRadius = 16.dp,
          onClick = { showDatePicker = true }
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.CalendarToday, contentDescription = null, tint = activeAccent, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = NumoFinancialEngine.formatLocalDate(selectedDateMillis),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = NumoDS.TextPrimary
              )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NumoDS.TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text("Description", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NumoDS.TextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        NumoNeumorphicCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
          BasicTextField(
            value = descriptionText,
            onValueChange = { descriptionText = it },
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp, color = NumoDS.TextPrimary),
            decorationBox = { inner ->
              if (descriptionText.isEmpty()) {
                Text(
                  text = if (typeIndex == 1) "e.g. Monthly Salary" else "e.g. Lunch at Java House",
                  fontSize = 14.sp,
                  color = NumoDS.TextMuted
                )
              }
              inner()
            },
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp)
          )
        }
      }

      if (errorMessage != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = ExpenseAccent.copy(alpha = 0.1f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = errorMessage!!,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ExpenseAccent,
            modifier = Modifier.padding(12.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Primary Action CTA
      val ctaLabel = when (typeIndex) {
        1 -> "Save Income"
        2 -> "Review Transfer"
        else -> "Save Transaction"
      }
      Button(
        onClick = {
          if (isSubmitting) return@Button
          errorMessage = null
          when (typeIndex) {
            0 -> {
              if (grossAmount <= 0.0) {
                errorMessage = "Enter a valid amount greater than zero."
                return@Button
              }
              isSubmitting = true
              val res = NumoFinancialEngine.recordExpense(
                title = descriptionText.ifBlank { selectedExpenseCategory.label },
                amount = grossAmount,
                category = selectedExpenseCategory.engineName,
                account = selectedExpenseAccount,
                dateMillis = selectedDateMillis,
                note = descriptionText,
                receiptUri = attachedReceiptUri
              )
              isSubmitting = false
              res.onSuccess { onSaved() }.onFailure { errorMessage = it.message }
            }
            1 -> {
              if (grossAmount <= 0.0) {
                errorMessage = "Enter a valid gross income amount."
                return@Button
              }
              if (netIncome <= 0.0) {
                errorMessage = "Net credited income must be greater than zero."
                return@Button
              }
              val splits: List<Pair<String, Double>> = if (!splitDepositEnabled) {
                listOf(selectedIncomeAccount to netIncome)
              } else {
                val rawSplits = accounts.mapNotNull { acc ->
                  val v = splitValues[acc.name]?.toDoubleOrNull() ?: 0.0
                  if (v > 0.0) acc.name to v else null
                }
                if (rawSplits.isEmpty()) {
                  errorMessage = "Allocate at least one account in split deposit."
                  return@Button
                }
                val sum = rawSplits.sumOf { it.second }
                if (splitIsPercent) {
                  if (abs(sum - 100.0) > 0.5) {
                    errorMessage = "Split percentages must sum to 100%."
                    return@Button
                  }
                  rawSplits.map { (name, pct) -> name to (netIncome * (pct / 100.0)) }
                } else {
                  if (abs(sum - netIncome) > 0.5) {
                    errorMessage = "Fixed splits must sum to Net Credited (${NumoFinancialEngine.formatKes(netIncome, false)})."
                    return@Button
                  }
                  rawSplits
                }
              }
              isSubmitting = true
              val res = NumoFinancialEngine.recordIncomeWithSplits(
                title = descriptionText.ifBlank { selectedIncomeCategory.label },
                grossAmount = grossAmount,
                deductionAmount = totalDeductions,
                netAmount = netIncome,
                category = selectedIncomeCategory.engineName,
                splits = splits,
                dateMillis = selectedDateMillis,
                note = descriptionText,
                receiptUri = attachedReceiptUri,
                recurringFrequency = if (recurringIncomeEnabled) incomeFrequency else null
              )
              isSubmitting = false
              res.onSuccess { onSaved() }.onFailure { errorMessage = it.message }
            }
            2 -> {
              if (grossAmount <= 0.0) {
                errorMessage = "Enter a transfer amount greater than zero."
                return@Button
              }
              if (fromAccount.equals(toAccount, ignoreCase = true)) {
                errorMessage = "Source and destination accounts must be different."
                return@Button
              }
              val fromBal = NumoFinancialEngine.getAccountBalance(fromAccount)
              if (totalSourceDebit > fromBal) {
                errorMessage = "Insufficient balance in $fromAccount."
                return@Button
              }
              showTransferReviewModal = true
            }
          }
        },
        enabled = !isSubmitting,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = activeAccent),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .shadow(6.dp, RoundedCornerShape(24.dp))
      ) {
        Text(
          text = ctaLabel,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }

  // Shared Calculator Modal
  if (showCalculator) {
    NumoCalculatorModal(
      initialAmount = grossAmount,
      currency = currency,
      accentColor = activeAccent,
      onDismiss = { showCalculator = false },
      onApply = { evaluated ->
        amountText = if (abs(evaluated - evaluated.toLong()) < 0.001) {
          evaluated.toLong().toString()
        } else {
          String.format("%.2f", evaluated)
        }
        errorMessage = null
      }
    )
  }

  // Date Picker Dialog
  if (showDatePicker) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(onClick = {
          datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
          showDatePicker = false
        }) { Text("OK", color = activeAccent, fontWeight = FontWeight.Bold) }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }

  // Transfer Account Pickers
  if (showFromPicker) {
    AccountPickerDialog(
      title = "Select Source Account",
      selected = fromAccount,
      onDismiss = { showFromPicker = false },
      onSelect = {
        fromAccount = it
        showFromPicker = false
        errorMessage = null
      }
    )
  }
  if (showToPicker) {
    AccountPickerDialog(
      title = "Select Destination Account",
      selected = toAccount,
      onDismiss = { showToPicker = false },
      onSelect = {
        toAccount = it
        showToPicker = false
        errorMessage = null
      }
    )
  }

  // Review Transfer Confirmation Modal
  if (showTransferReviewModal) {
    val fromBal = NumoFinancialEngine.getAccountBalance(fromAccount)
    val toBal = NumoFinancialEngine.getAccountBalance(toAccount)
    val newFromBal = (fromBal - totalSourceDebit).coerceAtLeast(0.0)
    val newToBal = toBal + grossAmount

    Dialog(onDismissRequest = { showTransferReviewModal = false }) {
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = NumoDS.CardSurface,
        shadowElevation = 12.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text("Review Transfer", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
          Spacer(modifier = Modifier.height(12.dp))
          ReviewRow("From", fromAccount)
          ReviewRow("To", toAccount)
          ReviewRow("Transfer Amount", NumoFinancialEngine.formatKes(grossAmount))
          ReviewRow("Fee", NumoFinancialEngine.formatKes(transferFee))
          ReviewRow("Total Source Debit", NumoFinancialEngine.formatKes(totalSourceDebit), highlight = TransferAccent)
          if (transferReference.isNotBlank()) ReviewRow("Reference", transferReference)
          HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = NumoDS.TrackBg)
          ReviewRow("New $fromAccount Balance", NumoFinancialEngine.formatKes(newFromBal))
          ReviewRow("New $toAccount Balance", NumoFinancialEngine.formatKes(newToBal), highlight = IncomeAccent)

          Spacer(modifier = Modifier.height(18.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
              onClick = { showTransferReviewModal = false },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(16.dp)
            ) {
              Text("Edit")
            }
            Button(
              onClick = {
                if (isSubmitting) return@Button
                isSubmitting = true
                val res = NumoFinancialEngine.executeTransfer(
                  fromAccount = fromAccount,
                  toAccount = toAccount,
                  amount = grossAmount,
                  fee = transferFee,
                  reference = transferReference,
                  dateMillis = selectedDateMillis,
                  recurringFrequency = if (recurringTransferEnabled) transferFrequency else null
                )
                isSubmitting = false
                showTransferReviewModal = false
                res.onSuccess { onSaved() }.onFailure { errorMessage = it.message }
              },
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(containerColor = TransferAccent),
              shape = RoundedCornerShape(16.dp)
            ) {
              Text("Confirm Transfer", color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CategoryBubbleItem(
  choice: TxCategoryChoice,
  selected: Boolean,
  accentColor: Color,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .width(64.dp)
      .clickable(onClick = onClick)
  ) {
    Box(
      modifier = Modifier
        .size(54.dp)
        .shadow(if (selected) 8.dp else 3.dp, CircleShape)
        .clip(CircleShape)
        .background(Brush.verticalGradient(listOf(choice.topColor, choice.bottomColor)))
        .then(if (selected) Modifier.border(3.dp, accentColor, CircleShape) else Modifier),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = choice.icon,
        contentDescription = choice.label,
        tint = Color.White,
        modifier = Modifier.size(24.dp)
      )
    }
    Spacer(modifier = Modifier.height(5.dp))
    Text(
      text = choice.label,
      fontSize = 11.sp,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
      color = if (selected) accentColor else NumoDS.TextPrimary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
private fun TransferAccountMiniCard(
  label: String,
  accountName: String,
  balanceText: String,
  accent: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  NumoNeumorphicCard(
    modifier = modifier,
    cornerRadius = 16.dp,
    onClick = onClick
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(label, fontSize = 10.sp, color = NumoDS.TextSecondary, fontWeight = FontWeight.Medium)
      Spacer(modifier = Modifier.height(4.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(accountName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary, maxLines = 1)
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(balanceText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NumoDS.TextSecondary, maxLines = 1)
    }
  }
}

@Composable
private fun AccountPickerDialog(
  title: String,
  selected: String,
  onDismiss: () -> Unit,
  onSelect: (String) -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = NumoDS.CardSurface,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        NumoFinancialEngine.accounts.forEach { acc ->
          val isSel = acc.name.equals(selected, ignoreCase = true)
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isSel) TransferAccent.copy(alpha = 0.12f) else Color.White,
            border = if (isSel) BorderStroke(1.5.dp, TransferAccent) else null,
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .clickable { onSelect(acc.name) }
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(acc.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
                Text(acc.provider, fontSize = 11.sp, color = NumoDS.TextSecondary)
              }
              Text(
                NumoFinancialEngine.formatKes(acc.balance, false),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TransferAccent
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ReviewRow(label: String, value: String, highlight: Color = NumoDS.TextPrimary) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(label, fontSize = 13.sp, color = NumoDS.TextSecondary)
    Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = highlight)
  }
}
