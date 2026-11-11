package com.example.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

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
  val isBusiness: Boolean = false
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
  var isPaid: Boolean = false
)

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

  // Live Transactions List
  val transactions = mutableStateListOf(
    LedgerTransaction(
      title = "M-PESA Top Up",
      amount = 2000.0,
      isIncome = true,
      category = "Income",
      account = "M-PESA",
      timestamp = "Today, 09:24 AM"
    ),
    LedgerTransaction(
      title = "Groceries",
      amount = 1250.0,
      isIncome = false,
      category = "Food & Dining",
      account = "M-PESA",
      timestamp = "Today, 08:45 AM"
    ),
    LedgerTransaction(
      title = "Uber Ride",
      amount = 320.0,
      isIncome = false,
      category = "Transport",
      account = "M-PESA",
      timestamp = "Today, 08:12 AM"
    ),
    LedgerTransaction(
      title = "Salary",
      amount = 120000.0,
      isIncome = true,
      category = "Income",
      account = "Bank Account",
      timestamp = "Yesterday, 09:00 AM"
    ),
    LedgerTransaction(
      title = "Electricity Bill",
      amount = 3200.0,
      isIncome = false,
      category = "Bills & Utilities",
      account = "M-PESA",
      timestamp = "Yesterday, 02:15 PM"
    )
  )

  // Live Bills
  val bills = mutableStateListOf(
    FinancialBill(
      title = "Rent Payment",
      amount = 35000.0,
      dueDateText = "In 2 days",
      accountNumber = "99881122",
      category = "Rent"
    ),
    FinancialBill(
      title = "KPLC Electricity",
      amount = 2500.0,
      dueDateText = "In 4 days",
      accountNumber = "12345678",
      category = "Bills & Utilities"
    ),
    FinancialBill(
      title = "Internet",
      amount = 1999.0,
      dueDateText = "In 5 days",
      accountNumber = "44556677",
      category = "Bills & Utilities"
    )
  )

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

  /**
   * Records a new transaction following double-entry principles
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

    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val currentTime = "Today, " + timeFormat.format(Date())

    val newTx = LedgerTransaction(
      title = title.ifBlank { if (isIncome) "Income Received" else "Expense" },
      amount = amount,
      isIncome = isIncome,
      category = category,
      account = account,
      timestamp = currentTime,
      note = note,
      isBusiness = isBusiness
    )

    transactions.add(0, newTx)

    if (isIncome) {
      availableCash += amount
      totalNetWorth += amount
    } else {
      availableCash = (availableCash - amount).coerceAtLeast(0.0)
      totalNetWorth = (totalNetWorth - amount).coerceAtLeast(0.0)

      // Update category limit spent
      val catIndex = categories.indexOfFirst { it.name.equals(category, ignoreCase = true) }
      if (catIndex != -1) {
        val cat = categories[catIndex]
        val updated = cat.copy(spent = cat.spent + amount)
        categories[catIndex] = updated

        // Check overspend
        if (updated.spent > updated.limit) {
          isOverspendAlertActive = true
          overspendAmount = updated.spent - updated.limit
        }
      } else {
        // Add to other
        val otherIdx = categories.indexOfFirst { it.name == "Other" }
        if (otherIdx != -1) {
          val cat = categories[otherIdx]
          categories[otherIdx] = cat.copy(spent = cat.spent + amount)
        }
      }
    }
  }

  /**
   * Pays a bill, marking it paid and recording an expense transaction
   */
  fun payBill(billId: String) {
    val billIndex = bills.indexOfFirst { it.id == billId }
    if (billIndex != -1) {
      val bill = bills[billIndex]
      if (!bill.isPaid) {
        bill.isPaid = true
        addTransaction(
          title = bill.title,
          amount = bill.amount,
          isIncome = false,
          category = bill.category,
          account = "M-PESA",
          note = "Account Ref: ${bill.accountNumber}"
        )
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
