package com.example.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

enum class TransactionType(val label: String) {
  EXPENSE("Expense"),
  INCOME("Income"),
  TRANSFER("Transfer")
}

enum class RecurrenceFrequency(val label: String) {
  ONE_TIME("One-time"),
  WEEKLY("Weekly"),
  BI_WEEKLY("Bi-weekly"),
  MONTHLY("Monthly"),
  QUARTERLY("Quarterly"),
  ANNUAL("Annual")
}

data class AccountBalanceItem(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val provider: String,
  val balance: Double,
  val accentHex: Long = 0xFF00796B
)

data class BillPaymentRecord(
  val id: String = UUID.randomUUID().toString(),
  val billId: String,
  val billTitle: String,
  val amountPaid: Double,
  val fee: Double = 0.0,
  val account: String,
  val paidAtMillis: Long = System.currentTimeMillis(),
  val dateText: String,
  val receiptUri: String? = null,
  val note: String = ""
)

data class RecurringSchedule(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val type: TransactionType,
  val amount: Double,
  val fee: Double = 0.0,
  val category: String,
  val account: String,
  val toAccount: String? = null,
  val frequency: RecurrenceFrequency,
  val startDateMillis: Long,
  val isActive: Boolean = true
)

/**
 * Transaction entity for the immutable ledger
 */
data class LedgerTransaction(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val amount: Double,
  val isIncome: Boolean,
  val category: String,
  val account: String,
  val timestamp: String,
  val note: String = "",
  val isBusiness: Boolean = false,
  val type: TransactionType = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
  val toAccount: String? = null,
  val fee: Double = 0.0,
  val dateMillis: Long = System.currentTimeMillis(),
  val receiptUri: String? = null,
  val reference: String = "",
  val grossAmount: Double = amount,
  val deductionAmount: Double = 0.0
)

/**
 * Category Budget tracking item
 */
data class CategoryBudgetItem(
  val name: String,
  var limit: Double,
  var spent: Double,
  val iconName: String,
  val colorHex: Long
) {
  val percentage: Int
    get() = if (limit > 0) ((spent / limit) * 100).toInt() else 0
}

/**
 * Bill item in the financial calendar
 */
data class FinancialBill(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val amount: Double,
  val dueDateText: String,
  val accountNumber: String,
  val category: String,
  var isPaid: Boolean = false,
  val dueDateMillis: Long = System.currentTimeMillis() + 3L * 86_400_000L,
  val recurrence: RecurrenceFrequency = RecurrenceFrequency.MONTHLY,
  val reminderEnabled: Boolean = true,
  val reminderLeadDays: Int = 3,
  val defaultAccount: String = "M-PESA",
  val autoPayPreference: Boolean = false,
  val paidAmount: Double = if (isPaid) amount else 0.0
) {
  val remainingAmount: Double
    get() = if (isPaid) 0.0 else (amount - paidAmount).coerceAtLeast(0.0)
}

/**
 * Financial Goal item
 */
data class FinancialGoal(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  var savedAmount: Double,
  val targetAmount: Double,
  var monthlyContribution: Double = 10000.0,
  val iconType: String = "Shield"
) {
  val percentage: Int
    get() = if (targetAmount > 0) ((savedAmount / targetAmount) * 100).coerceIn(0.0, 100.0).toInt() else 0
}

/**
 * Investment Holding item (from PLAN.md Environment 2)
 */
data class InvestmentHolding(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val assetType: String, // Stock, MMF, SACCO, T-Bill
  val currentValuation: Double,
  val investedCapital: Double,
  val annualYieldPercent: Double,
  val currency: String = "KES"
) {
  val returnAmount: Double
    get() = currentValuation - investedCapital
  val returnPercentage: Double
    get() = if (investedCapital > 0) (returnAmount / investedCapital) * 100 else 0.0
}

/**
 * Central Numo Financial Engine singleton coordinating live balances,
 * transactions, category limits, bills, goals, and dual-environment state.
 */
object NumoFinancialEngine {
  // Primary Currency
  var currency by mutableStateOf("KES")

  // Available Cash and Net Worth
  var availableCash by mutableDoubleStateOf(48750.0)
  var totalNetWorth by mutableDoubleStateOf(533750.0)

  // Monthly Budget Target
  var monthlyBudgetTarget by mutableDoubleStateOf(120000.0)

  // Balance Visibility Toggle
  var isBalanceVisible by mutableStateOf(true)

  // Active Environment: 0 = Money Management, 1 = Investment Management
  var activeEnvironment by mutableIntStateOf(0)

  // Overspending alert banner active state
  var isOverspendAlertActive by mutableStateOf(true)
  var overspendAmount by mutableDoubleStateOf(1500.0)

  // Live Category Budgets
  val categories = mutableStateListOf(
    CategoryBudgetItem("Food & Dining", 30000.0, 24980.0, "Restaurant", 0xFF02844E),
    CategoryBudgetItem("Transport", 20000.0, 14120.0, "Car", 0xFF2563EB),
    CategoryBudgetItem("Shopping", 25000.0, 12460.0, "Bag", 0xFFF59E0B),
    CategoryBudgetItem("Health & Fitness", 10000.0, 7110.0, "Heart", 0xFFEC4899),
    CategoryBudgetItem("Entertainment", 8000.0, 6260.0, "Game", 0xFF8B5CF6),
    CategoryBudgetItem("Bills & Utilities", 15000.0, 3200.0, "Bolt", 0xFF14B8A6),
    CategoryBudgetItem("Other", 12000.0, 290.0, "More", 0xFF64748B)
  )

  // Total spent calculated dynamically
  val totalSpent: Double
    get() = categories.sumOf { it.spent }

  val totalRemaining: Double
    get() = (monthlyBudgetTarget - totalSpent).coerceAtLeast(0.0)

  // Live Accounts (Sum = 48,750.00 KES matching initial availableCash)
  val accounts = mutableStateListOf(
    AccountBalanceItem(id = "acc_mpesa", name = "M-PESA", provider = "Safaricom M-Pesa", balance = 8500.0, accentHex = 0xFF1B8A5A),
    AccountBalanceItem(id = "acc_kcb", name = "KCB", provider = "KCB Bank Kenya", balance = 24500.0, accentHex = 0xFF00796B),
    AccountBalanceItem(id = "acc_equity", name = "Equity", provider = "Equity Bank", balance = 12250.0, accentHex = 0xFFD81B60),
    AccountBalanceItem(id = "acc_cash", name = "Cash", provider = "Cash Wallet", balance = 3500.0, accentHex = 0xFFD97706)
  )

  private val nowMillis: Long = System.currentTimeMillis()
  private val dayMillis: Long = 86_400_000L

  // Live Transactions List
  val transactions = mutableStateListOf(
    LedgerTransaction(
      title = "Salary",
      amount = 75000.0,
      isIncome = true,
      category = "Salary",
      account = "KCB",
      timestamp = "Today, 09:00 AM",
      note = "Monthly Net Salary",
      type = TransactionType.INCOME,
      dateMillis = nowMillis,
      grossAmount = 75000.0
    ),
    LedgerTransaction(
      title = "Lunch at Java House",
      amount = 2500.0,
      isIncome = false,
      category = "Food & Dining",
      account = "M-PESA",
      timestamp = "Today, 12:30 PM",
      note = "Lunch with colleagues",
      type = TransactionType.EXPENSE,
      dateMillis = nowMillis
    ),
    LedgerTransaction(
      title = "Matatu & Uber",
      amount = 1250.0,
      isIncome = false,
      category = "Transport",
      account = "Cash",
      timestamp = "Yesterday, 06:15 PM",
      type = TransactionType.EXPENSE,
      dateMillis = nowMillis - dayMillis
    ),
    LedgerTransaction(
      title = "Transfer to Savings",
      amount = 5000.0,
      isIncome = false,
      category = "Transfer",
      account = "KCB",
      toAccount = "M-PESA",
      fee = 50.0,
      timestamp = "2 days ago, 02:15 PM",
      note = "Rent Share",
      type = TransactionType.TRANSFER,
      dateMillis = nowMillis - 2 * dayMillis,
      reference = "Rent Share"
    ),
    LedgerTransaction(
      title = "Electricity Bill",
      amount = 3200.0,
      isIncome = false,
      category = "Bills & Utilities",
      account = "M-PESA",
      timestamp = "3 days ago, 04:40 PM",
      type = TransactionType.EXPENSE,
      dateMillis = nowMillis - 3 * dayMillis
    ),
    LedgerTransaction(
      title = "Naivas Groceries",
      amount = 45050.0,
      isIncome = false,
      category = "Shopping",
      account = "Equity",
      timestamp = "5 days ago, 11:20 AM",
      type = TransactionType.EXPENSE,
      dateMillis = nowMillis - 5 * dayMillis
    )
  )

  // Live Bills
  val bills = mutableStateListOf(
    FinancialBill(
      id = "bill_rent",
      title = "Rent",
      amount = 15000.0,
      dueDateText = "Due in 3 days",
      accountNumber = "99881122",
      category = "Housing",
      dueDateMillis = nowMillis + 3 * dayMillis,
      recurrence = RecurrenceFrequency.MONTHLY,
      defaultAccount = "M-PESA"
    ),
    FinancialBill(
      id = "bill_kplc",
      title = "KPLC",
      amount = 3500.0,
      dueDateText = "Due in 5 days",
      accountNumber = "12345678",
      category = "Bills & Utilities",
      dueDateMillis = nowMillis + 5 * dayMillis,
      recurrence = RecurrenceFrequency.MONTHLY,
      defaultAccount = "M-PESA"
    ),
    FinancialBill(
      id = "bill_water",
      title = "Water",
      amount = 1200.0,
      dueDateText = "Due in 7 days",
      accountNumber = "44556677",
      category = "Bills & Utilities",
      dueDateMillis = nowMillis + 7 * dayMillis,
      recurrence = RecurrenceFrequency.MONTHLY,
      defaultAccount = "M-PESA"
    )
  )

  val billPayments = mutableStateListOf(
    BillPaymentRecord(
      billId = "bill_rent",
      billTitle = "Rent",
      amountPaid = 15000.0,
      fee = 0.0,
      account = "M-PESA",
      paidAtMillis = nowMillis - 30 * dayMillis,
      dateText = formatLocalDate(nowMillis - 30 * dayMillis),
      note = "Previous month rent"
    )
  )

  val recurringSchedules = mutableStateListOf<RecurringSchedule>()

  val monthlyIncomeTotal: Double
    get() {
      val cal = Calendar.getInstance()
      val curYear = cal.get(Calendar.YEAR)
      val curMonth = cal.get(Calendar.MONTH)
      return transactions.filter { tx ->
        if (tx.type != TransactionType.INCOME) return@filter false
        cal.timeInMillis = tx.dateMillis
        cal.get(Calendar.YEAR) == curYear && cal.get(Calendar.MONTH) == curMonth
      }.sumOf { it.amount }
    }

  val monthlyExpenseTotal: Double
    get() {
      val cal = Calendar.getInstance()
      val curYear = cal.get(Calendar.YEAR)
      val curMonth = cal.get(Calendar.MONTH)
      return transactions.filter { tx ->
        if (tx.type != TransactionType.EXPENSE) return@filter false
        cal.timeInMillis = tx.dateMillis
        cal.get(Calendar.YEAR) == curYear && cal.get(Calendar.MONTH) == curMonth
      }.sumOf { it.amount }
    }

  val netCashFlow: Double
    get() = monthlyIncomeTotal - monthlyExpenseTotal

  val budgetConsumptionPercent: Int
    get() = if (monthlyBudgetTarget > 0) ((totalSpent / monthlyBudgetTarget) * 100).roundToInt().coerceAtLeast(0) else 0

  // Live Financial Goals
  val goals = mutableStateListOf(
    FinancialGoal(
      title = "Emergency Fund",
      savedAmount = 180000.0,
      targetAmount = 250000.0,
      monthlyContribution = 10000.0,
      iconType = "Shield"
    ),
    FinancialGoal(
      title = "Dream Vacation",
      savedAmount = 0.0,
      targetAmount = 200000.0,
      monthlyContribution = 5000.0,
      iconType = "Flight"
    ),
    FinancialGoal(
      title = "New Laptop",
      savedAmount = 60000.0,
      targetAmount = 100000.0,
      monthlyContribution = 15000.0,
      iconType = "Laptop"
    )
  )

  // Live Investment Holdings (PLAN.md Environment 2)
  val investmentHoldings = mutableStateListOf(
    InvestmentHolding(
      name = "Safaricom PLC (NSE: SCOM)",
      assetType = "NSE Stocks",
      currentValuation = 175000.0,
      investedCapital = 148000.0,
      annualYieldPercent = 6.2
    ),
    InvestmentHolding(
      name = "Sanlam Money Market Fund",
      assetType = "MMF",
      currentValuation = 180000.0,
      investedCapital = 160000.0,
      annualYieldPercent = 12.8
    ),
    InvestmentHolding(
      name = "Stima SACCO Member Shares",
      assetType = "SACCO",
      currentValuation = 80000.0,
      investedCapital = 70000.0,
      annualYieldPercent = 10.5
    ),
    InvestmentHolding(
      name = "91-Day Kenya Treasury Bill",
      assetType = "Treasury Bill",
      currentValuation = 50000.0,
      investedCapital = 48000.0,
      annualYieldPercent = 15.2
    )
  )

  val totalPortfolioValuation: Double
    get() = investmentHoldings.sumOf { it.currentValuation }

  val totalInvestedCapital: Double
    get() = investmentHoldings.sumOf { it.investedCapital }

  val totalPortfolioReturn: Double
    get() = totalPortfolioValuation - totalInvestedCapital

  val totalPortfolioReturnPercent: Double
    get() = if (totalInvestedCapital > 0) (totalPortfolioReturn / totalInvestedCapital) * 100 else 0.0

  fun formatKes(amount: Double, includeDecimals: Boolean = true): String {
    val nf = NumberFormat.getNumberInstance(Locale.US).apply {
      minimumFractionDigits = if (includeDecimals) 2 else 0
      maximumFractionDigits = if (includeDecimals) 2 else 0
    }
    return "$currency ${nf.format(amount)}"
  }

  fun formatLocalDate(millis: Long): String {
    val fmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return fmt.format(Date(millis))
  }

  fun formatLocalTimestamp(millis: Long): String {
    val cal = Calendar.getInstance()
    val todayYear = cal.get(Calendar.YEAR)
    val todayDay = cal.get(Calendar.DAY_OF_YEAR)
    cal.timeInMillis = millis
    val txYear = cal.get(Calendar.YEAR)
    val txDay = cal.get(Calendar.DAY_OF_YEAR)
    val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return when {
      txYear == todayYear && txDay == todayDay -> "Today, ${timeFmt.format(Date(millis))}"
      txYear == todayYear && txDay == todayDay - 1 -> "Yesterday, ${timeFmt.format(Date(millis))}"
      else -> SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(millis))
    }
  }

  fun formatDueRelative(dueMillis: Long, isPaid: Boolean): String {
    if (isPaid) return "Paid"
    val diffDays = ((dueMillis - System.currentTimeMillis()) / dayMillis.toDouble()).roundToInt()
    return when {
      diffDays < 0 -> "Overdue by ${-diffDays}d"
      diffDays == 0 -> "Due today"
      diffDays == 1 -> "Due tomorrow"
      else -> "Due in $diffDays days"
    }
  }

  fun findAccountIndex(accountName: String): Int {
    val norm = accountName.trim()
    val direct = accounts.indexOfFirst { it.name.equals(norm, ignoreCase = true) }
    if (direct != -1) return direct
    return when {
      norm.contains("mpesa", ignoreCase = true) || norm.contains("m-pesa", ignoreCase = true) ->
        accounts.indexOfFirst { it.name.equals("M-PESA", ignoreCase = true) }
      norm.contains("bank", ignoreCase = true) || norm.contains("kcb", ignoreCase = true) ->
        accounts.indexOfFirst { it.name.equals("KCB", ignoreCase = true) }
      norm.contains("equity", ignoreCase = true) ->
        accounts.indexOfFirst { it.name.equals("Equity", ignoreCase = true) }
      norm.contains("cash", ignoreCase = true) ->
        accounts.indexOfFirst { it.name.equals("Cash", ignoreCase = true) }
      else -> -1
    }
  }

  fun getAccountBalance(accountName: String): Double {
    val idx = findAccountIndex(accountName)
    return if (idx != -1) accounts[idx].balance else 0.0
  }

  private fun syncCashAndNetWorth() {
    availableCash = accounts.sumOf { it.balance }.coerceAtLeast(0.0)
    totalNetWorth = (availableCash + totalPortfolioValuation).coerceAtLeast(0.0)
  }

  fun resolveBudgetCategoryIndex(category: String): Int {
    val norm = category.trim()
    val exact = categories.indexOfFirst { it.name.equals(norm, ignoreCase = true) }
    if (exact != -1) return exact
    val mapped = when {
      norm.contains("food", ignoreCase = true) -> "Food & Dining"
      norm.contains("health", ignoreCase = true) -> "Health & Fitness"
      norm.contains("housing", ignoreCase = true) || norm.contains("rent", ignoreCase = true) ||
        norm.contains("bill", ignoreCase = true) || norm.contains("utility", ignoreCase = true) ||
        norm.contains("water", ignoreCase = true) || norm.contains("kplc", ignoreCase = true) -> "Bills & Utilities"
      norm.contains("transport", ignoreCase = true) -> "Transport"
      norm.contains("shop", ignoreCase = true) -> "Shopping"
      norm.contains("entertain", ignoreCase = true) -> "Entertainment"
      else -> "Other"
    }
    return categories.indexOfFirst { it.name.equals(mapped, ignoreCase = true) }
  }

  private fun applyBudgetSpendDelta(category: String, delta: Double) {
    val catIndex = resolveBudgetCategoryIndex(category)
    if (catIndex != -1) {
      val cat = categories[catIndex]
      val updated = cat.copy(spent = (cat.spent + delta).coerceAtLeast(0.0))
      categories[catIndex] = updated
      if (updated.spent > updated.limit) {
        isOverspendAlertActive = true
        overspendAmount = updated.spent - updated.limit
      } else {
        val anyOver = categories.firstOrNull { it.spent > it.limit }
        isOverspendAlertActive = anyOver != null
        overspendAmount = anyOver?.let { it.spent - it.limit } ?: 0.0
      }
    }
  }

  /**
   * Records an Expense atomically with balance validation.
   */
  fun recordExpense(
    title: String,
    amount: Double,
    category: String,
    account: String,
    dateMillis: Long = System.currentTimeMillis(),
    note: String = "",
    receiptUri: String? = null
  ): Result<LedgerTransaction> {
    if (amount <= 0.0) return Result.failure(IllegalArgumentException("Amount must be greater than zero."))
    val accIdx = findAccountIndex(account)
    if (accIdx == -1) return Result.failure(IllegalArgumentException("Please select a valid account."))
    val acc = accounts[accIdx]
    if (amount > acc.balance) {
      return Result.failure(
        IllegalArgumentException("Insufficient balance in ${acc.name} (${formatKes(acc.balance)} available).")
      )
    }

    accounts[accIdx] = acc.copy(balance = acc.balance - amount)
    applyBudgetSpendDelta(category, amount)
    syncCashAndNetWorth()

    val tx = LedgerTransaction(
      title = title.ifBlank { "$category Expense" },
      amount = amount,
      isIncome = false,
      category = category,
      account = acc.name,
      timestamp = formatLocalTimestamp(dateMillis),
      note = note,
      type = TransactionType.EXPENSE,
      dateMillis = dateMillis,
      receiptUri = receiptUri
    )
    transactions.add(0, tx)
    return Result.success(tx)
  }

  /**
   * Records Income (supports tax deductions metadata, multi-account split deposits, receipt & recurring schedule).
   */
  fun recordIncomeWithSplits(
    title: String,
    grossAmount: Double,
    deductionAmount: Double,
    netAmount: Double,
    category: String,
    splits: List<Pair<String, Double>>,
    dateMillis: Long = System.currentTimeMillis(),
    note: String = "",
    receiptUri: String? = null,
    recurringFrequency: RecurrenceFrequency? = null
  ): Result<List<LedgerTransaction>> {
    if (netAmount <= 0.0) return Result.failure(IllegalArgumentException("Net income must be greater than zero."))
    if (splits.isEmpty()) return Result.failure(IllegalArgumentException("Select at least one destination account."))

    val resolvedIndices = splits.map { (accName, amt) ->
      val idx = findAccountIndex(accName)
      if (idx == -1) return Result.failure(IllegalArgumentException("Unknown account: $accName"))
      if (amt <= 0.0) return Result.failure(IllegalArgumentException("Split amount for $accName must be > 0."))
      idx to amt
    }

    val created = mutableListOf<LedgerTransaction>()
    resolvedIndices.forEach { (idx, amt) ->
      val acc = accounts[idx]
      accounts[idx] = acc.copy(balance = acc.balance + amt)
      val splitSuffix = if (resolvedIndices.size > 1) " (Split)" else ""
      val tx = LedgerTransaction(
        title = (title.ifBlank { category }) + splitSuffix,
        amount = amt,
        isIncome = true,
        category = category,
        account = acc.name,
        timestamp = formatLocalTimestamp(dateMillis),
        note = note,
        type = TransactionType.INCOME,
        dateMillis = dateMillis,
        receiptUri = receiptUri,
        grossAmount = if (resolvedIndices.size == 1) grossAmount else amt,
        deductionAmount = if (resolvedIndices.size == 1) deductionAmount else 0.0
      )
      transactions.add(0, tx)
      created.add(tx)
    }

    if (recurringFrequency != null && recurringFrequency != RecurrenceFrequency.ONE_TIME) {
      recurringSchedules.add(
        RecurringSchedule(
          title = title.ifBlank { category },
          type = TransactionType.INCOME,
          amount = netAmount,
          category = category,
          account = splits.first().first,
          frequency = recurringFrequency,
          startDateMillis = dateMillis
        )
      )
    }

    syncCashAndNetWorth()
    return Result.success(created)
  }

  /**
   * Executes an atomic Transfer between two distinct accounts.
   */
  fun executeTransfer(
    fromAccount: String,
    toAccount: String,
    amount: Double,
    fee: Double = 0.0,
    reference: String = "",
    dateMillis: Long = System.currentTimeMillis(),
    recurringFrequency: RecurrenceFrequency? = null
  ): Result<LedgerTransaction> {
    if (amount <= 0.0) return Result.failure(IllegalArgumentException("Transfer amount must be greater than zero."))
    if (fee < 0.0) return Result.failure(IllegalArgumentException("Transfer fee cannot be negative."))
    val fromIdx = findAccountIndex(fromAccount)
    val toIdx = findAccountIndex(toAccount)
    if (fromIdx == -1 || toIdx == -1) return Result.failure(IllegalArgumentException("Select valid source and destination accounts."))
    if (fromIdx == toIdx) return Result.failure(IllegalArgumentException("Source and destination accounts must be different."))

    val fromAcc = accounts[fromIdx]
    val toAcc = accounts[toIdx]
    val totalDebit = amount + fee
    if (totalDebit > fromAcc.balance) {
      return Result.failure(
        IllegalArgumentException("Insufficient balance in ${fromAcc.name}. Needed ${formatKes(totalDebit)}, available ${formatKes(fromAcc.balance)}.")
      )
    }

    accounts[fromIdx] = fromAcc.copy(balance = fromAcc.balance - totalDebit)
    accounts[toIdx] = toAcc.copy(balance = toAcc.balance + amount)
    syncCashAndNetWorth()

    val tx = LedgerTransaction(
      title = if (reference.isNotBlank()) reference else "Transfer to ${toAcc.name}",
      amount = amount,
      isIncome = false,
      category = "Transfer",
      account = fromAcc.name,
      toAccount = toAcc.name,
      fee = fee,
      timestamp = formatLocalTimestamp(dateMillis),
      note = reference,
      type = TransactionType.TRANSFER,
      dateMillis = dateMillis,
      reference = reference
    )
    transactions.add(0, tx)

    if (recurringFrequency != null && recurringFrequency != RecurrenceFrequency.ONE_TIME) {
      recurringSchedules.add(
        RecurringSchedule(
          title = tx.title,
          type = TransactionType.TRANSFER,
          amount = amount,
          fee = fee,
          category = "Transfer",
          account = fromAcc.name,
          toAccount = toAcc.name,
          frequency = recurringFrequency,
          startDateMillis = dateMillis
        )
      )
    }

    return Result.success(tx)
  }

  /**
   * Atomically reverses a transaction's financial effects and removes it.
   */
  fun deleteTransaction(txId: String): Boolean {
    val idx = transactions.indexOfFirst { it.id == txId }
    if (idx == -1) return false
    val tx = transactions[idx]
    reverseTransactionEffects(tx)
    transactions.removeAt(idx)
    syncCashAndNetWorth()
    return true
  }

  private fun reverseTransactionEffects(tx: LedgerTransaction) {
    when (tx.type) {
      TransactionType.INCOME -> {
        val accIdx = findAccountIndex(tx.account)
        if (accIdx != -1) {
          val acc = accounts[accIdx]
          accounts[accIdx] = acc.copy(balance = (acc.balance - tx.amount).coerceAtLeast(0.0))
        }
      }
      TransactionType.EXPENSE -> {
        val accIdx = findAccountIndex(tx.account)
        if (accIdx != -1) {
          val acc = accounts[accIdx]
          accounts[accIdx] = acc.copy(balance = acc.balance + tx.amount)
        }
        applyBudgetSpendDelta(tx.category, -tx.amount)
      }
      TransactionType.TRANSFER -> {
        val fromIdx = findAccountIndex(tx.account)
        val toIdx = tx.toAccount?.let { findAccountIndex(it) } ?: -1
        if (fromIdx != -1) {
          val fromAcc = accounts[fromIdx]
          accounts[fromIdx] = fromAcc.copy(balance = fromAcc.balance + tx.amount + tx.fee)
        }
        if (toIdx != -1) {
          val toAcc = accounts[toIdx]
          accounts[toIdx] = toAcc.copy(balance = (toAcc.balance - tx.amount).coerceAtLeast(0.0))
        }
      }
    }
  }

  /**
   * Atomically edits an existing transaction by reversing old effects and applying new ones.
   */
  fun editTransaction(
    txId: String,
    newTitle: String,
    newAmount: Double,
    newCategory: String,
    newAccount: String,
    newNote: String,
    newDateMillis: Long,
    newToAccount: String? = null,
    newFee: Double = 0.0
  ): Result<LedgerTransaction> {
    if (newAmount <= 0.0) return Result.failure(IllegalArgumentException("Amount must be greater than zero."))
    val idx = transactions.indexOfFirst { it.id == txId }
    if (idx == -1) return Result.failure(IllegalArgumentException("Transaction not found."))
    val oldTx = transactions[idx]

    // Temporarily reverse old effects to validate against restored balances
    reverseTransactionEffects(oldTx)

    val applyResult: Result<LedgerTransaction> = when (oldTx.type) {
      TransactionType.INCOME -> {
        val accIdx = findAccountIndex(newAccount)
        if (accIdx == -1) {
          Result.failure(IllegalArgumentException("Invalid account."))
        } else {
          val acc = accounts[accIdx]
          accounts[accIdx] = acc.copy(balance = acc.balance + newAmount)
          val updated = oldTx.copy(
            title = newTitle.ifBlank { oldTx.title },
            amount = newAmount,
            category = newCategory,
            account = acc.name,
            note = newNote,
            dateMillis = newDateMillis,
            timestamp = formatLocalTimestamp(newDateMillis)
          )
          transactions[idx] = updated
          Result.success(updated)
        }
      }
      TransactionType.EXPENSE -> {
        val accIdx = findAccountIndex(newAccount)
        if (accIdx == -1) {
          Result.failure(IllegalArgumentException("Invalid account."))
        } else {
          val acc = accounts[accIdx]
          if (newAmount > acc.balance) {
            Result.failure(IllegalArgumentException("Insufficient balance in ${acc.name}."))
          } else {
            accounts[accIdx] = acc.copy(balance = acc.balance - newAmount)
            applyBudgetSpendDelta(newCategory, newAmount)
            val updated = oldTx.copy(
              title = newTitle.ifBlank { oldTx.title },
              amount = newAmount,
              category = newCategory,
              account = acc.name,
              note = newNote,
              dateMillis = newDateMillis,
              timestamp = formatLocalTimestamp(newDateMillis)
            )
            transactions[idx] = updated
            Result.success(updated)
          }
        }
      }
      TransactionType.TRANSFER -> {
        val fromIdx = findAccountIndex(newAccount)
        val toIdx = findAccountIndex(newToAccount ?: oldTx.toAccount ?: "")
        if (fromIdx == -1 || toIdx == -1 || fromIdx == toIdx) {
          Result.failure(IllegalArgumentException("Select two distinct valid accounts."))
        } else {
          val fromAcc = accounts[fromIdx]
          val toAcc = accounts[toIdx]
          val totalDebit = newAmount + newFee
          if (totalDebit > fromAcc.balance) {
            Result.failure(IllegalArgumentException("Insufficient balance in ${fromAcc.name}."))
          } else {
            accounts[fromIdx] = fromAcc.copy(balance = fromAcc.balance - totalDebit)
            accounts[toIdx] = toAcc.copy(balance = toAcc.balance + newAmount)
            val updated = oldTx.copy(
              title = newTitle.ifBlank { oldTx.title },
              amount = newAmount,
              account = fromAcc.name,
              toAccount = toAcc.name,
              fee = newFee,
              note = newNote,
              dateMillis = newDateMillis,
              timestamp = formatLocalTimestamp(newDateMillis)
            )
            transactions[idx] = updated
            Result.success(updated)
          }
        }
      }
    }

    if (applyResult.isFailure) {
      // Rollback reversal if re-application failed
      when (oldTx.type) {
        TransactionType.INCOME -> {
          val accIdx = findAccountIndex(oldTx.account)
          if (accIdx != -1) accounts[accIdx] = accounts[accIdx].copy(balance = accounts[accIdx].balance + oldTx.amount)
        }
        TransactionType.EXPENSE -> {
          val accIdx = findAccountIndex(oldTx.account)
          if (accIdx != -1) accounts[accIdx] = accounts[accIdx].copy(balance = (accounts[accIdx].balance - oldTx.amount).coerceAtLeast(0.0))
          applyBudgetSpendDelta(oldTx.category, oldTx.amount)
        }
        TransactionType.TRANSFER -> {
          val fromIdx = findAccountIndex(oldTx.account)
          val toIdx = oldTx.toAccount?.let { findAccountIndex(it) } ?: -1
          if (fromIdx != -1) accounts[fromIdx] = accounts[fromIdx].copy(balance = (accounts[fromIdx].balance - oldTx.amount - oldTx.fee).coerceAtLeast(0.0))
          if (toIdx != -1) accounts[toIdx] = accounts[toIdx].copy(balance = accounts[toIdx].balance + oldTx.amount)
        }
      }
    }
    syncCashAndNetWorth()
    return applyResult
  }

  /**
   * Backwards-compatible transaction recording helper.
   */
  fun addTransaction(
    title: String,
    amount: Double,
    isIncome: Boolean,
    category: String,
    account: String = "M-PESA",
    note: String = "",
    isBusiness: Boolean = false
  ) {
    if (amount <= 0) return
    val accIdx = findAccountIndex(account)
    if (accIdx != -1) {
      val acc = accounts[accIdx]
      if (isIncome) {
        accounts[accIdx] = acc.copy(balance = acc.balance + amount)
      } else {
        accounts[accIdx] = acc.copy(balance = (acc.balance - amount).coerceAtLeast(0.0))
      }
    }
    if (!isIncome) {
      applyBudgetSpendDelta(category, amount)
    }
    syncCashAndNetWorth()

    val currentTime = formatLocalTimestamp(System.currentTimeMillis())
    val newTx = LedgerTransaction(
      title = title.ifBlank { if (isIncome) "Income Received" else "Expense" },
      amount = amount,
      isIncome = isIncome,
      category = category,
      account = if (accIdx != -1) accounts[accIdx].name else account,
      timestamp = currentTime,
      note = note,
      isBusiness = isBusiness,
      type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
    )
    transactions.add(0, newTx)
  }

  fun addBill(
    title: String,
    amount: Double,
    category: String,
    dueDateMillis: Long,
    accountNumber: String = "",
    recurrence: RecurrenceFrequency = RecurrenceFrequency.MONTHLY,
    reminderEnabled: Boolean = true,
    reminderLeadDays: Int = 3,
    defaultAccount: String = "M-PESA",
    autoPayPreference: Boolean = false
  ): FinancialBill {
    val bill = FinancialBill(
      title = title.trim(),
      amount = amount,
      dueDateText = formatDueRelative(dueDateMillis, false),
      accountNumber = accountNumber,
      category = category,
      isPaid = false,
      dueDateMillis = dueDateMillis,
      recurrence = recurrence,
      reminderEnabled = reminderEnabled,
      reminderLeadDays = reminderLeadDays,
      defaultAccount = defaultAccount,
      autoPayPreference = autoPayPreference
    )
    bills.add(0, bill)
    return bill
  }

  fun updateBill(
    billId: String,
    title: String,
    amount: Double,
    category: String,
    dueDateMillis: Long,
    accountNumber: String,
    recurrence: RecurrenceFrequency,
    reminderEnabled: Boolean,
    reminderLeadDays: Int,
    defaultAccount: String,
    autoPayPreference: Boolean
  ): Boolean {
    val idx = bills.indexOfFirst { it.id == billId }
    if (idx == -1) return false
    val existing = bills[idx]
    val fullyPaid = existing.paidAmount >= amount
    bills[idx] = existing.copy(
      title = title.trim(),
      amount = amount,
      category = category,
      dueDateMillis = dueDateMillis,
      dueDateText = formatDueRelative(dueDateMillis, fullyPaid),
      accountNumber = accountNumber,
      recurrence = recurrence,
      reminderEnabled = reminderEnabled,
      reminderLeadDays = reminderLeadDays,
      defaultAccount = defaultAccount,
      autoPayPreference = autoPayPreference,
      isPaid = fullyPaid
    )
    return true
  }

  /**
   * Deletes a bill without removing historical LedgerTransactions or BillPaymentRecords.
   */
  fun deleteBill(billId: String): Boolean {
    val idx = bills.indexOfFirst { it.id == billId }
    if (idx == -1) return false
    bills.removeAt(idx)
    return true
  }

  /**
   * Pays a bill (supports partial & split payments across accounts, fee, receipt & budget updates).
   */
  fun payBillAdvanced(
    billId: String,
    splits: List<Pair<String, Double>>,
    fee: Double = 0.0,
    receiptUri: String? = null,
    note: String = ""
  ): Result<BillPaymentRecord> {
    val billIdx = bills.indexOfFirst { it.id == billId }
    if (billIdx == -1) return Result.failure(IllegalArgumentException("Bill not found."))
    val bill = bills[billIdx]
    if (splits.isEmpty()) return Result.failure(IllegalArgumentException("Select a payment account."))
    val totalPay = splits.sumOf { it.second }
    if (totalPay <= 0.0) return Result.failure(IllegalArgumentException("Payment amount must be greater than zero."))

    val resolvedSplits = splits.mapIndexed { index, (accName, amt) ->
      val accIdx = findAccountIndex(accName)
      if (accIdx == -1) return Result.failure(IllegalArgumentException("Invalid account: $accName"))
      val acc = accounts[accIdx]
      val needed = amt + if (index == 0) fee else 0.0
      if (needed > acc.balance) {
        return Result.failure(IllegalArgumentException("Insufficient balance in ${acc.name} (${formatKes(acc.balance)} available)."))
      }
      accIdx to needed
    }

    resolvedSplits.forEachIndexed { index, (accIdx, debit) ->
      val acc = accounts[accIdx]
      accounts[accIdx] = acc.copy(balance = acc.balance - debit)
      val payAmt = splits[index].second
      val feeApplied = if (index == 0) fee else 0.0
      val tx = LedgerTransaction(
        title = "${bill.title} Bill",
        amount = payAmt + feeApplied,
        isIncome = false,
        category = bill.category,
        account = acc.name,
        fee = feeApplied,
        timestamp = formatLocalTimestamp(System.currentTimeMillis()),
        note = note.ifBlank { "Ref: ${bill.accountNumber.ifBlank { bill.title }}" },
        type = TransactionType.EXPENSE,
        receiptUri = receiptUri
      )
      transactions.add(0, tx)
    }

    applyBudgetSpendDelta(bill.category, totalPay + fee)
    syncCashAndNetWorth()

    val newPaidTotal = bill.paidAmount + totalPay
    val isNowPaid = newPaidTotal >= bill.amount - 0.01
    bills[billIdx] = bill.copy(
      paidAmount = if (isNowPaid) bill.amount else newPaidTotal,
      isPaid = isNowPaid,
      dueDateText = formatDueRelative(bill.dueDateMillis, isNowPaid)
    )

    val accountSummary = splits.joinToString(" + ") { it.first }
    val record = BillPaymentRecord(
      billId = bill.id,
      billTitle = bill.title,
      amountPaid = totalPay,
      fee = fee,
      account = accountSummary,
      paidAtMillis = System.currentTimeMillis(),
      dateText = formatLocalDate(System.currentTimeMillis()),
      receiptUri = receiptUri,
      note = note
    )
    billPayments.add(0, record)
    return Result.success(record)
  }

  /**
   * Backwards-compatible single-tap bill payment.
   */
  fun payBill(billIdOrTitle: String) {
    val billIndex = bills.indexOfFirst { it.id == billIdOrTitle || it.title.equals(billIdOrTitle, ignoreCase = true) }
    if (billIndex != -1) {
      val bill = bills[billIndex]
      if (!bill.isPaid) {
        val account = if (getAccountBalance(bill.defaultAccount) >= bill.remainingAmount) bill.defaultAccount else "M-PESA"
        payBillAdvanced(bill.id, listOf(account to bill.remainingAmount))
      }
    }
  }

  /**
   * Rebalances budget when a category is overspent (Rule from PLAN.md)
   * Shifts funds from Shopping to Food
   */
  fun rebalanceCategory() {
    val foodIdx = categories.indexOfFirst { it.name == "Food & Dining" }
    val shopIdx = categories.indexOfFirst { it.name == "Shopping" }

    if (foodIdx != -1 && shopIdx != -1) {
      val food = categories[foodIdx]
      val shop = categories[shopIdx]

      val shiftAmt = 1500.0
      categories[foodIdx] = food.copy(limit = food.limit + shiftAmt)
      categories[shopIdx] = shop.copy(limit = (shop.limit - shiftAmt).coerceAtLeast(1000.0))
      isOverspendAlertActive = false
    }
  }

  /**
   * Adds a new financial goal
   */
  fun addGoal(title: String, target: Double) {
    if (title.isBlank() || target <= 0) return
    goals.add(
      FinancialGoal(
        title = title,
        savedAmount = 0.0,
        targetAmount = target,
        monthlyContribution = (target / 12.0).coerceAtLeast(1000.0)
      )
    )
  }

  /**
   * Contributes money into a savings goal
   */
  fun contributeToGoal(goalTitle: String, amount: Double) {
    val index = goals.indexOfFirst { it.title.equals(goalTitle, ignoreCase = true) }
    if (index != -1 && amount > 0) {
      val g = goals[index]
      goals[index] = g.copy(savedAmount = (g.savedAmount + amount).coerceAtMost(g.targetAmount))
      availableCash = (availableCash - amount).coerceAtLeast(0.0)
      addTransaction(
        title = "Deposit to ${g.title}",
        amount = amount,
        isIncome = false,
        category = "Savings & Goals",
        account = "Bank Account",
        note = "Goal Contribution"
      )
    }
  }

  /**
   * Adds an investment holding
   */
  fun addInvestmentHolding(
    name: String,
    assetType: String,
    investedAmount: Double,
    annualYield: Double
  ) {
    if (name.isBlank() || investedAmount <= 0) return
    investmentHoldings.add(
      InvestmentHolding(
        name = name,
        assetType = assetType,
        currentValuation = investedAmount,
        investedCapital = investedAmount,
        annualYieldPercent = annualYield
      )
    )
    availableCash = (availableCash - investedAmount).coerceAtLeast(0.0)
    totalNetWorth += investedAmount
    addTransaction(
      title = "Invest in $name",
      amount = investedAmount,
      isIncome = false,
      category = "Investments",
      account = "Bank Account",
      note = "$assetType Portfolio Allocation"
    )
  }

  /**
   * Updates monthly budget target
   */
  fun updateMonthlyBudget(newBudget: Double) {
    if (newBudget > 0) {
      monthlyBudgetTarget = newBudget
    }
  }

  /**
   * Initializes user accounts from onboarding
   */
  fun seedFromOnboarding(
    currencyCode: String,
    accounts: List<Pair<String, String>>
  ) {
    currency = currencyCode
    if (accounts.isNotEmpty()) {
      var totalStarting = 0.0
      accounts.forEach { (name, balanceStr) ->
        val balance = balanceStr.replace(",", "").replace(currencyCode, "").trim().toDoubleOrNull() ?: 0.0
        totalStarting += balance
      }
      if (totalStarting > 0) {
        availableCash = totalStarting
        totalNetWorth = totalStarting + totalPortfolioValuation
      }
    }
  }
}
