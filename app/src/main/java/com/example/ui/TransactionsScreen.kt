package com.example.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.LedgerTransaction
import com.example.data.NumoFinancialEngine
import com.example.data.TransactionType
import com.example.ui.components.NumoClayIconBadge
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoScreenBackground
import java.util.Calendar

private val TxIncomeGreen = Color(0xFF1B8A5A)
private val TxExpenseMagenta = Color(0xFFD81B60)
private val TxTransferTeal = Color(0xFF00796B)

@Composable
fun TransactionsScreen(
  selectedFilterTab: Int = 0,
  onFilterTabChange: (Int) -> Unit = {},
  searchQuery: String = "",
  onSearchQueryChange: (String) -> Unit = {},
  sortOption: String = "Newest First",
  onSortOptionChange: (String) -> Unit = {},
  accountFilter: String = "All",
  onAccountFilterChange: (String) -> Unit = {},
  dateFilter: String = "All",
  onDateFilterChange: (String) -> Unit = {},
  onBack: () -> Unit = {},
  onAddTransactionTab: (String) -> Unit = {}
) {
  val allTransactions = NumoFinancialEngine.transactions
  var showFilterSheet by remember { mutableStateOf(false) }
  var selectedTxForDetail by remember { mutableStateOf<LedgerTransaction?>(null) }

  val tabs = listOf("All", "Income", "Expenses", "Transfers")
  val now = System.currentTimeMillis()
  val dayMillis = 86_400_000L

  val filteredList = remember(
    allTransactions.toList(),
    selectedFilterTab,
    searchQuery,
    sortOption,
    accountFilter,
    dateFilter
  ) {
    val q = searchQuery.trim().lowercase()
    val cal = Calendar.getInstance()
    val todayYear = cal.get(Calendar.YEAR)
    val todayDay = cal.get(Calendar.DAY_OF_YEAR)
    val todayMonth = cal.get(Calendar.MONTH)

    val filtered = allTransactions.filter { tx ->
      val typeMatches = when (selectedFilterTab) {
        1 -> tx.type == TransactionType.INCOME
        2 -> tx.type == TransactionType.EXPENSE
        3 -> tx.type == TransactionType.TRANSFER
        else -> true
      }
      if (!typeMatches) return@filter false

      if (accountFilter != "All") {
        val accMatch = tx.account.equals(accountFilter, ignoreCase = true) ||
          (tx.toAccount?.equals(accountFilter, ignoreCase = true) == true)
        if (!accMatch) return@filter false
      }

      if (dateFilter != "All") {
        cal.timeInMillis = tx.dateMillis
        val dateOk = when (dateFilter) {
          "Today" -> cal.get(Calendar.YEAR) == todayYear && cal.get(Calendar.DAY_OF_YEAR) == todayDay
          "This Week" -> (now - tx.dateMillis) <= 7L * dayMillis
          "This Month" -> cal.get(Calendar.YEAR) == todayYear && cal.get(Calendar.MONTH) == todayMonth
          else -> true
        }
        if (!dateOk) return@filter false
      }

      if (q.isNotEmpty()) {
        val haystack = "${tx.title} ${tx.category} ${tx.account} ${tx.toAccount.orEmpty()} ${tx.note} ${tx.reference}".lowercase()
        if (!haystack.contains(q)) return@filter false
      }
      true
    }

    when (sortOption) {
      "Oldest First" -> filtered.sortedBy { it.dateMillis }
      "Highest Amount" -> filtered.sortedByDescending { it.amount }
      "Lowest Amount" -> filtered.sortedBy { it.amount }
      else -> filtered.sortedByDescending { it.dateMillis }
    }
  }

  val activeFilterCount = (if (accountFilter != "All") 1 else 0) +
    (if (dateFilter != "All") 1 else 0) +
    (if (sortOption != "Newest First") 1 else 0)

  NumoScreenBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .padding(horizontal = 20.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      // Top Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NumoDS.TextPrimary)
        }
        Text(
          text = "Transactions",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
          Box {
            IconButton(onClick = { showFilterSheet = true }, modifier = Modifier.size(38.dp)) {
              Icon(Icons.Default.Tune, contentDescription = "Filter & Sort", tint = NumoDS.TextPrimary)
            }
            if (activeFilterCount > 0) {
              Box(
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .size(16.dp)
                  .clip(CircleShape)
                  .background(TxExpenseMagenta),
                contentAlignment = Alignment.Center
              ) {
                Text(activeFilterCount.toString(), fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
              }
            }
          }
          Box(
            modifier = Modifier
              .size(34.dp)
              .shadow(3.dp, CircleShape)
              .clip(CircleShape)
              .background(TxTransferTeal)
              .clickable {
                val contextTab = when (selectedFilterTab) {
                  1 -> "income"
                  3 -> "transfer"
                  else -> "expense"
                }
                onAddTransactionTab(contextTab)
              },
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction", tint = Color.White, modifier = Modifier.size(18.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Real Search Field
      NumoNeumorphicCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Search, contentDescription = null, tint = NumoDS.TextSecondary, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          BasicTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp, color = NumoDS.TextPrimary),
            decorationBox = { inner ->
              if (searchQuery.isEmpty()) {
                Text("Search transactions...", fontSize = 14.sp, color = NumoDS.TextMuted)
              }
              inner()
            },
            modifier = Modifier.weight(1f)
          )
          if (searchQuery.isNotEmpty()) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Clear Search",
              tint = NumoDS.TextSecondary,
              modifier = Modifier
                .size(18.dp)
                .clickable { onSearchQueryChange("") }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Filter Tabs: All / Income / Expenses / Transfers
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        tabs.forEachIndexed { idx, label ->
          val sel = selectedFilterTab == idx
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(18.dp))
              .background(if (sel) Color(0xFF2B2118) else NumoDS.CardSurface)
              .clickable { onFilterTabChange(idx) }
              .padding(horizontal = 16.dp, vertical = 8.dp)
          ) {
            Text(
              text = label,
              fontSize = 13.sp,
              fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
              color = if (sel) Color.White else NumoDS.TextPrimary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Transactions List
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        if (filteredList.isEmpty()) {
          NumoNeumorphicCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(Icons.Default.SearchOff, contentDescription = null, tint = NumoDS.TextSecondary, modifier = Modifier.size(36.dp))
              Spacer(modifier = Modifier.height(8.dp))
              Text("No transactions found", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
              Text("Try clearing your search or filters.", fontSize = 12.sp, color = NumoDS.TextSecondary)
              Spacer(modifier = Modifier.height(10.dp))
              TextButton(onClick = {
                onSearchQueryChange("")
                onFilterTabChange(0)
                onAccountFilterChange("All")
                onDateFilterChange("All")
                onSortOptionChange("Newest First")
              }) {
                Text("Reset Filters", color = TxTransferTeal, fontWeight = FontWeight.Bold)
              }
            }
          }
        } else {
          filteredList.forEach { tx ->
            TransactionHistoryRowCard(tx = tx, onClick = { selectedTxForDetail = tx })
          }
        }
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // Filter & Sort Modal
  if (showFilterSheet) {
    FilterSortDialog(
      currentSort = sortOption,
      currentAccount = accountFilter,
      currentDate = dateFilter,
      onDismiss = { showFilterSheet = false },
      onReset = {
        onSortOptionChange("Newest First")
        onAccountFilterChange("All")
        onDateFilterChange("All")
        showFilterSheet = false
      },
      onApply = { s, a, d ->
        onSortOptionChange(s)
        onAccountFilterChange(a)
        onDateFilterChange(d)
        showFilterSheet = false
      }
    )
  }

  // Transaction Detail / Edit / Delete Modal
  selectedTxForDetail?.let { tx ->
    TransactionDetailModal(
      tx = tx,
      onDismiss = { selectedTxForDetail = null },
      onDeleted = { selectedTxForDetail = null },
      onUpdated = { selectedTxForDetail = null }
    )
  }
}

@Composable
private fun TransactionHistoryRowCard(
  tx: LedgerTransaction,
  onClick: () -> Unit
) {
  val (topColor, bottomColor, icon) = when (tx.type) {
    TransactionType.INCOME -> Triple(Color(0xFF34D399), TxIncomeGreen, Icons.Default.ArrowUpward)
    TransactionType.TRANSFER -> Triple(Color(0xFF2DD4BF), TxTransferTeal, Icons.Default.SwapHoriz)
    TransactionType.EXPENSE -> Triple(Color(0xFFF43F5E), TxExpenseMagenta, Icons.Default.ShoppingBag)
  }
  val amountText = when (tx.type) {
    TransactionType.INCOME -> "+${NumoFinancialEngine.formatKes(tx.amount, false)}"
    TransactionType.TRANSFER -> NumoFinancialEngine.formatKes(tx.amount, false)
    TransactionType.EXPENSE -> "-${NumoFinancialEngine.formatKes(tx.amount, false)}"
  }
  val amountColor = when (tx.type) {
    TransactionType.INCOME -> TxIncomeGreen
    TransactionType.TRANSFER -> TxTransferTeal
    TransactionType.EXPENSE -> TxExpenseMagenta
  }
  val subtitle = if (tx.type == TransactionType.TRANSFER && !tx.toAccount.isNullOrBlank()) {
    "${tx.timestamp} • ${tx.account} → ${tx.toAccount}"
  } else {
    "${tx.timestamp} • ${tx.account}"
  }

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
        icon = icon,
        topColor = topColor,
        bottomColor = bottomColor,
        size = 42.dp,
        iconSize = 20.dp
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = tx.title,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = subtitle,
          fontSize = 11.sp,
          color = NumoDS.TextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = amountText,
        fontSize = 14.sp,
        fontWeight = FontWeight.ExtraBold,
        color = amountColor
      )
    }
  }
}

@Composable
private fun FilterSortDialog(
  currentSort: String,
  currentAccount: String,
  currentDate: String,
  onDismiss: () -> Unit,
  onReset: () -> Unit,
  onApply: (String, String, String) -> Unit
) {
  var sort by remember { mutableStateOf(currentSort) }
  var account by remember { mutableStateOf(currentAccount) }
  var date by remember { mutableStateOf(currentDate) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(shape = RoundedCornerShape(24.dp), color = NumoDS.CardSurface, modifier = Modifier.fillMaxWidth()) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(18.dp)
      ) {
        Text("Sort & Filter", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NumoDS.TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))

        Text("Sort By", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NumoDS.TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("Newest First", "Oldest First", "Highest Amount", "Lowest Amount").forEach { opt ->
            FilterChip(selected = sort == opt, onClick = { sort = opt }, label = { Text(opt, fontSize = 11.sp) })
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text("Account", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NumoDS.TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          val accOptions = listOf("All") + NumoFinancialEngine.accounts.map { it.name }
          accOptions.forEach { acc ->
            FilterChip(selected = account == acc, onClick = { account = acc }, label = { Text(acc, fontSize = 11.sp) })
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text("Date Range", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NumoDS.TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("All", "Today", "This Week", "This Month").forEach { df ->
            FilterChip(selected = date == df, onClick = { date = df }, label = { Text(df, fontSize = 11.sp) })
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
            Text("Reset")
          }
          Button(
            onClick = { onApply(sort, account, date) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = TxTransferTeal),
            shape = RoundedCornerShape(14.dp)
          ) {
            Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
private fun TransactionDetailModal(
  tx: LedgerTransaction,
  onDismiss: () -> Unit,
  onDeleted: () -> Unit,
  onUpdated: () -> Unit
) {
  var isEditing by remember { mutableStateOf(false) }
  var showDeleteConfirm by remember { mutableStateOf(false) }

  var editTitle by remember { mutableStateOf(tx.title) }
  var editAmount by remember { mutableStateOf(tx.amount.toInt().toString()) }
  var editCategory by remember { mutableStateOf(tx.category) }
  var editAccount by remember { mutableStateOf(tx.account) }
  var editNote by remember { mutableStateOf(tx.note) }
  var errorText by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(shape = RoundedCornerShape(24.dp), color = NumoDS.CardSurface, modifier = Modifier.fillMaxWidth()) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(20.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isEditing) "Edit Transaction" else "Transaction Details",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )
          IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (!isEditing) {
          Text(
            text = NumoFinancialEngine.formatKes(tx.amount),
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = when (tx.type) {
              TransactionType.INCOME -> TxIncomeGreen
              TransactionType.TRANSFER -> TxTransferTeal
              TransactionType.EXPENSE -> TxExpenseMagenta
            }
          )
          Spacer(modifier = Modifier.height(10.dp))
          DetailLine("Title", tx.title)
          DetailLine("Type", tx.type.label)
          DetailLine("Category", tx.category)
          DetailLine("Account", if (tx.toAccount != null) "${tx.account} → ${tx.toAccount}" else tx.account)
          if (tx.fee > 0) DetailLine("Fee", NumoFinancialEngine.formatKes(tx.fee))
          DetailLine("Date", NumoFinancialEngine.formatLocalDate(tx.dateMillis))
          if (tx.note.isNotBlank()) DetailLine("Note", tx.note)

          Spacer(modifier = Modifier.height(16.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
              onClick = { showDeleteConfirm = true },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(14.dp)
            ) {
              Text("Delete", color = TxExpenseMagenta, fontWeight = FontWeight.Bold)
            }
            Button(
              onClick = { isEditing = true },
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(containerColor = TxTransferTeal),
              shape = RoundedCornerShape(14.dp)
            ) {
              Text("Edit", color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        } else {
          OutlinedTextField(
            value = editTitle,
            onValueChange = { editTitle = it },
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = editAmount,
            onValueChange = { editAmount = it.filter { c -> c.isDigit() || c == '.' }; errorText = null },
            label = { Text("Amount (KES)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = editNote,
            onValueChange = { editNote = it },
            label = { Text("Note") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          if (errorText != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(errorText!!, color = TxExpenseMagenta, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }

          Spacer(modifier = Modifier.height(14.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { isEditing = false }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) {
              Text("Cancel")
            }
            Button(
              onClick = {
                val amt = editAmount.toDoubleOrNull() ?: 0.0
                val res = NumoFinancialEngine.editTransaction(
                  txId = tx.id,
                  newTitle = editTitle,
                  newAmount = amt,
                  newCategory = editCategory,
                  newAccount = editAccount,
                  newNote = editNote,
                  newDateMillis = tx.dateMillis
                )
                res.onSuccess { onUpdated() }.onFailure { errorText = it.message }
              },
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(containerColor = TxIncomeGreen),
              shape = RoundedCornerShape(14.dp)
            ) {
              Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  if (showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirm = false },
      title = { Text("Delete Transaction?") },
      text = { Text("This will reverse its balance and budget impact.") },
      confirmButton = {
        TextButton(onClick = {
          NumoFinancialEngine.deleteTransaction(tx.id)
          showDeleteConfirm = false
          onDeleted()
        }) { Text("Delete", color = TxExpenseMagenta, fontWeight = FontWeight.Bold) }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
      }
    )
  }
}

@Composable
private fun DetailLine(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(label, fontSize = 13.sp, color = NumoDS.TextSecondary)
    Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NumoDS.TextPrimary)
  }
}
