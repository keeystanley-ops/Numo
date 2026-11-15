package com.example.voice

import com.example.data.NumoFinancialEngine
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

enum class PendingActionKind {
  TRANSACTION,
  PAY_BILL,
  CONTRIBUTE_GOAL
}

data class PendingFinancialAction(
  val id: String = UUID.randomUUID().toString(),
  val kind: PendingActionKind,
  val amount: Double,
  val description: String,
  val account: String,
  val category: String,
  val isIncome: Boolean = false,
  val statusLabel: String = "Awaiting Approval",
  val isResolved: Boolean = false,
  val wasConfirmed: Boolean = false
)

data class HeyNumoMessage(
  val id: String = UUID.randomUUID().toString(),
  val isUser: Boolean,
  val text: String,
  val pendingAction: PendingFinancialAction? = null
)

data class AssistantReply(
  val spokenText: String,
  val displayText: String,
  val pendingAction: PendingFinancialAction? = null
)

/**
 * Financial Intelligence, Action Confirmation, and Conversation Memory Engine
 * connected directly to `NumoFinancialEngine` (Money Management + Investments).
 */
class NumoFinancialAssistantEngine(
  private val customAccounts: List<Pair<String, String>> = emptyList()
) {
  private var lastTopicCategory: String? = null
  private var lastQueriedRemaining: Double? = null
  var activePendingAction: PendingFinancialAction? = null
    private set

  private fun fmt(amount: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale.US).apply {
      maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
    }
    return "${NumoFinancialEngine.currency} ${nf.format(amount)}"
  }

  fun processUserQuery(rawQuery: String): AssistantReply {
    val q = rawQuery.trim()
    val lower = q.lowercase(Locale.US)
    HeyNumoSettings.recordRecentPrompt(q)

    // 1. Check if user is verbally confirming or cancelling an active pending action
    val pending = activePendingAction
    if (pending != null && !pending.isResolved) {
      if (lower in listOf("confirm", "yes", "yes confirm", "approve", "proceed", "do it")) {
        return executePendingAction(pending)
      }
      if (lower in listOf("cancel", "no", "stop", "decline", "never mind")) {
        activePendingAction = pending.copy(isResolved = true, wasConfirmed = false, statusLabel = "Cancelled")
        return AssistantReply(
          spokenText = "Okay, I've cancelled that transaction.",
          displayText = "Cancelled — no changes were made to your ledger."
        )
      }
    }

    // 2. Follow-up hypothetical ("What if I spend 450?") using conversation memory
    val whatIfMatch = Regex("(?:what if i spend|if i spend|minus|subtract)\\s+(?:[a-z]{3}\\s*)?(\\d+(?:\\.\\d+)?)")
      .find(lower)
    if (whatIfMatch != null && lastQueriedRemaining != null) {
      val delta = whatIfMatch.groupValues[1].toDoubleOrNull() ?: 0.0
      val projected = lastQueriedRemaining!! - delta
      val catLabel = lastTopicCategory ?: "that budget"
      val text = if (projected >= 0) {
        "If you spend ${fmt(delta)}, you would have ${fmt(projected)} remaining in $catLabel."
      } else {
        "If you spend ${fmt(delta)}, you would exceed $catLabel by ${fmt(-projected)}."
      }
      return AssistantReply(spokenText = text, displayText = text)
    }

    // 3. Action initiation: Record Transaction (requires explicit confirmation)
    if (lower.contains("record") || lower.contains("add expense") || lower.contains("log ") ||
      lower.contains("spent ") || lower.contains("add income") || lower.contains("received ")
    ) {
      val parsed = parseTransactionCommand(q, lower)
      if (parsed != null) {
        activePendingAction = parsed
        val kindWord = if (parsed.isIncome) "income" else "expense"
        val prompt = "I've prepared this $kindWord transaction for your approval. Would you like me to confirm ${fmt(parsed.amount)} for ${parsed.description}?"
        return AssistantReply(
          spokenText = prompt,
          displayText = "I've prepared this transaction for your approval.",
          pendingAction = parsed
        )
      }
    }

    // 4. Action initiation: Pay upcoming bill
    if (lower.startsWith("pay ") || lower.contains("pay bill")) {
      val matchingBill = NumoFinancialEngine.bills.firstOrNull {
        !it.isPaid && lower.contains(it.title.lowercase(Locale.US))
      } ?: NumoFinancialEngine.bills.firstOrNull { !it.isPaid }

      if (matchingBill != null) {
        val action = PendingFinancialAction(
          kind = PendingActionKind.PAY_BILL,
          amount = matchingBill.amount,
          description = matchingBill.title,
          account = "M-PESA",
          category = matchingBill.category,
          isIncome = false
        )
        activePendingAction = action
        val msg = "I've prepared the ${matchingBill.title} bill payment of ${fmt(matchingBill.amount)} for your approval."
        return AssistantReply(spokenText = msg, displayText = msg, pendingAction = action)
      }
    }

    // 5. Action initiation: Contribute to savings goal
    if (lower.contains("save ") && Regex("\\d+").containsMatchIn(lower)) {
      val amt = Regex("(\\d+(?:,\\d{3})*(?:\\.\\d+)?)").find(lower)?.groupValues?.get(1)
        ?.replace(",", "")?.toDoubleOrNull()
      if (amt != null && amt > 0) {
        val goal = NumoFinancialEngine.goals.firstOrNull {
          lower.contains(it.title.lowercase(Locale.US))
        } ?: NumoFinancialEngine.goals.firstOrNull()
        val goalName = goal?.title ?: "Emergency Fund"
        val action = PendingFinancialAction(
          kind = PendingActionKind.CONTRIBUTE_GOAL,
          amount = amt,
          description = goalName,
          account = "M-PESA",
          category = "Savings",
          isIncome = false
        )
        activePendingAction = action
        val msg = "Ready to move ${fmt(amt)} into your $goalName goal. Please confirm to proceed."
        return AssistantReply(spokenText = msg, displayText = msg, pendingAction = action)
      }
    }

    // 6. Financial Queries backed by actual NumoFinancialEngine data
    // 6a. Available Cash / Account Balances
    if (lower.contains("available cash") || lower.contains("balance") || lower.contains("how much cash") || lower.contains("accounts")) {
      val cash = fmt(NumoFinancialEngine.availableCash)
      val acctSummary = if (customAccounts.isNotEmpty()) {
        " Linked accounts: " + customAccounts.joinToString(", ") { "${it.first} (${it.second})" } + "."
      } else {
        ""
      }
      val reply = "Your available cash is $cash.$acctSummary"
      return AssistantReply(spokenText = reply, displayText = reply)
    }

    // 6b. Net Worth
    if (lower.contains("net worth")) {
      val nw = fmt(NumoFinancialEngine.totalNetWorth)
      val inv = fmt(NumoFinancialEngine.totalPortfolioValuation)
      val reply = "Your total net worth is $nw, including $inv in your investment portfolio and ${fmt(NumoFinancialEngine.availableCash)} in available cash."
      return AssistantReply(spokenText = reply, displayText = reply)
    }

    // 6c. Specific Category Budget (Food, Shopping, Transport, Bills, Health, etc.)
    val matchedCat = NumoFinancialEngine.categories.firstOrNull { cat ->
      val catWords = cat.name.lowercase(Locale.US).split(" ", "&").filter { it.length >= 3 }
      catWords.any { lower.contains(it) }
    }
    if (matchedCat != null && (lower.contains("budget") || lower.contains("left") || lower.contains("spent") || lower.contains("food"))) {
      val remaining = matchedCat.limit - matchedCat.spent
      lastTopicCategory = matchedCat.name
      lastQueriedRemaining = remaining
      val reply = if (remaining >= 0) {
        "You have ${fmt(remaining)} left in ${matchedCat.name} (spent ${fmt(matchedCat.spent)} of ${fmt(matchedCat.limit)})."
      } else {
        "Your ${matchedCat.name} budget is over by ${fmt(-remaining)} (spent ${fmt(matchedCat.spent)} of ${fmt(matchedCat.limit)})."
      }
      return AssistantReply(spokenText = reply, displayText = reply)
    }

    // 6d. Overall Budget / Monthly Expenses
    if (lower.contains("monthly expense") || lower.contains("spending") || lower.contains("budget") || lower.contains("reduce")) {
      val spent = NumoFinancialEngine.totalSpent
      val target = NumoFinancialEngine.monthlyBudgetTarget
      val remaining = NumoFinancialEngine.totalRemaining
      lastTopicCategory = "Monthly Budget"
      lastQueriedRemaining = remaining
      val topCat = NumoFinancialEngine.categories.maxByOrNull { it.spent }
      val advice = if (lower.contains("reduce") && topCat != null) {
        " Your highest spending category is ${topCat.name} at ${fmt(topCat.spent)} (${topCat.percentage}% of its limit). Trimming 15% there would save ${fmt(topCat.spent * 0.15)}."
      } else {
        ""
      }
      val reply = "You've spent ${fmt(spent)} out of your ${fmt(target)} monthly budget, leaving ${fmt(remaining)}.$advice"
      return AssistantReply(spokenText = reply, displayText = reply)
    }

    // 6e. Investments
    if (lower.contains("invest") || lower.contains("portfolio") || lower.contains("stock") ||
      lower.contains("mmf") || lower.contains("sacco") || lower.contains("t-bill")
    ) {
      val valTotal = fmt(NumoFinancialEngine.totalPortfolioValuation)
      val cap = fmt(NumoFinancialEngine.totalInvestedCapital)
      val ret = fmt(NumoFinancialEngine.totalPortfolioReturn)
      val pct = String.format(Locale.US, "%.1f", NumoFinancialEngine.totalPortfolioReturnPercent)
      val holdingsList = NumoFinancialEngine.investmentHoldings.take(3)
        .joinToString(", ") { "${it.name} (${fmt(it.currentValuation)})" }
      val reply = "Your investment portfolio is valued at $valTotal on $cap invested — a return of $ret (+$pct%). Top holdings: $holdingsList."
      return AssistantReply(spokenText = reply, displayText = reply)
    }

    // 6f. Upcoming Bills
    if (lower.contains("bill") || lower.contains("due")) {
      val unpaid = NumoFinancialEngine.bills.filter { !it.isPaid }
      val reply = if (unpaid.isEmpty()) {
        "All your upcoming bills are paid!"
      } else {
        val totalDue = unpaid.sumOf { it.amount }
        val details = unpaid.joinToString(", ") { "${it.title} (${fmt(it.amount)}, ${it.dueDateText})" }
        "You have ${unpaid.size} upcoming bills totaling ${fmt(totalDue)}: $details."
      }
      return AssistantReply(spokenText = reply, displayText = reply)
    }

    // 6g. Savings Goals & Emergency Fund
    if (lower.contains("save") || lower.contains("saving") || lower.contains("goal") || lower.contains("emergency")) {
      val totalSaved = NumoFinancialEngine.goals.sumOf { it.savedAmount }
      val goalsSummary = NumoFinancialEngine.goals.joinToString(", ") {
        "${it.title}: ${fmt(it.savedAmount)} / ${fmt(it.targetAmount)} (${it.percentage}%)"
      }
      val canSaveMonthly = NumoFinancialEngine.totalRemaining.coerceAtLeast(0.0)
      val reply = if (lower.contains("can i save")) {
        "You currently have ${fmt(canSaveMonthly)} remaining in your monthly budget and ${fmt(totalSaved)} saved across your goals ($goalsSummary)."
      } else {
        "You have saved ${fmt(totalSaved)} across your goals: $goalsSummary."
      }
      return AssistantReply(spokenText = reply, displayText = reply)
    }

    // 6h. Recent Transactions
    if (lower.contains("transaction") || lower.contains("recent")) {
      val latest = NumoFinancialEngine.transactions.take(3).joinToString("; ") {
        val sign = if (it.isIncome) "+" else "-"
        "${it.title} ($sign${fmt(it.amount)})"
      }
      val reply = "Your latest transactions: $latest."
      return AssistantReply(spokenText = reply, displayText = reply)
    }

    // Fallback grounded summary
    val fallback = "Here's your live snapshot: Available cash is ${fmt(NumoFinancialEngine.availableCash)}, monthly spending is ${fmt(NumoFinancialEngine.totalSpent)} of ${fmt(NumoFinancialEngine.monthlyBudgetTarget)}, and your net worth is ${fmt(NumoFinancialEngine.totalNetWorth)}. You can also ask me to record a transaction, check investments, or review upcoming bills."
    return AssistantReply(spokenText = fallback, displayText = fallback)
  }

  fun executePendingAction(action: PendingFinancialAction): AssistantReply {
    val resolved = action.copy(isResolved = true, wasConfirmed = true, statusLabel = "Confirmed")
    activePendingAction = resolved

    when (action.kind) {
      PendingActionKind.TRANSACTION -> {
        NumoFinancialEngine.addTransaction(
          title = action.description,
          amount = action.amount,
          isIncome = action.isIncome,
          category = action.category,
          account = action.account,
          note = "Added via Hey Numo"
        )
      }
      PendingActionKind.PAY_BILL -> {
        NumoFinancialEngine.payBill(action.description)
      }
      PendingActionKind.CONTRIBUTE_GOAL -> {
        NumoFinancialEngine.contributeToGoal(action.description, action.amount)
      }
    }

    val reply = "Done! Recorded ${fmt(action.amount)} for ${action.description} (${action.category}) from ${action.account}. Your available cash is now ${fmt(NumoFinancialEngine.availableCash)}."
    return AssistantReply(spokenText = reply, displayText = reply)
  }

  fun cancelPendingAction(action: PendingFinancialAction): AssistantReply {
    activePendingAction = action.copy(isResolved = true, wasConfirmed = false, statusLabel = "Cancelled")
    return AssistantReply(
      spokenText = "Transaction cancelled.",
      displayText = "Transaction cancelled — your balances were not modified."
    )
  }

  private fun parseTransactionCommand(original: String, lower: String): PendingFinancialAction? {
    val amountMatch = Regex("(\\d+(?:,\\d{3})*(?:\\.\\d+)?)").find(lower) ?: return null
    val amount = amountMatch.groupValues[1].replace(",", "").toDoubleOrNull() ?: return null
    if (amount <= 0.0) return null

    val isIncome = lower.contains("income") || lower.contains("received") || lower.contains("salary")
    val account = when {
      lower.contains("equity") -> "Equity Bank"
      lower.contains("kcb") -> "KCB Bank"
      lower.contains("cash") -> "Cash"
      else -> "M-PESA"
    }

    val category = when {
      isIncome -> "Income"
      lower.contains("lunch") || lower.contains("dinner") || lower.contains("breakfast") ||
        lower.contains("food") || lower.contains("coffee") || lower.contains("grocer") -> "Food & Dining"
      lower.contains("uber") || lower.contains("matatu") || lower.contains("fuel") ||
        lower.contains("bus") || lower.contains("transport") -> "Transport"
      lower.contains("rent") || lower.contains("kplc") || lower.contains("water") ||
        lower.contains("bill") || lower.contains("airtime") -> "Bills & Utilities"
      lower.contains("shop") || lower.contains("clothes") -> "Shopping"
      else -> "Other"
    }

    val descMatch = Regex("(?:for|on)\\s+([a-zA-Z0-9\\s]+?)(?:\\s+(?:from|via|using|in)\\b|$)")
      .find(original)
    val description = descMatch?.groupValues?.get(1)?.trim()
      ?.replaceFirstChar { it.uppercase() }
      ?.takeIf { it.isNotBlank() }
      ?: if (isIncome) "Income Deposit" else category

    return PendingFinancialAction(
      kind = PendingActionKind.TRANSACTION,
      amount = amount,
      description = description,
      account = account,
      category = category,
      isIncome = isIncome
    )
  }
}
