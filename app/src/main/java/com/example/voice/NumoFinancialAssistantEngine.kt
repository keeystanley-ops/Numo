package com.example.voice

import com.example.data.NumoFinancialEngine
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

enum class PendingActionKind {
  TRANSACTION,
  PAY_BILL,
  CONTRIBUTE_GOAL,
  CREATE_BUDGET,
  UPDATE_TRANSACTION,
  UPDATE_ASSET
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
  val pendingAction: PendingFinancialAction? = null,
  val isStreaming: Boolean = false
)

data class AssistantReply(
  val spokenText: String,
  val displayText: String,
  val pendingAction: PendingFinancialAction? = null
)

/**
 * Ledger Action Executor & Confirmation State Manager
 * connected directly to `NumoFinancialEngine` (Money Management + Investments).
 */
class NumoFinancialAssistantEngine(
  private val customAccounts: List<Pair<String, String>> = emptyList()
) {
  var activePendingAction: PendingFinancialAction? = null
    private set

  fun setPendingAction(action: PendingFinancialAction?) {
    activePendingAction = action
  }

  private fun fmt(amount: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale.US).apply {
      maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
    }
    return "${NumoFinancialEngine.currency} ${nf.format(amount)}"
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
      PendingActionKind.CREATE_BUDGET -> {
        val catIdx = NumoFinancialEngine.categories.indexOfFirst {
          it.name.contains(action.category, ignoreCase = true)
        }
        if (catIdx >= 0) {
          val old = NumoFinancialEngine.categories[catIdx]
          NumoFinancialEngine.categories[catIdx] = old.copy(limit = action.amount)
        }
      }
      PendingActionKind.UPDATE_TRANSACTION -> {
        val txIdx = NumoFinancialEngine.transactions.indexOfFirst {
          action.description.contains(it.title, ignoreCase = true)
        }
        if (txIdx >= 0) {
          val old = NumoFinancialEngine.transactions[txIdx]
          NumoFinancialEngine.transactions[txIdx] = old.copy(
            category = action.category.ifBlank { old.category },
            amount = if (action.amount > 0.0) action.amount else old.amount
          )
        }
      }
      PendingActionKind.UPDATE_ASSET -> {
        val hIdx = NumoFinancialEngine.investmentHoldings.indexOfFirst {
          action.description.contains(it.name, ignoreCase = true)
        }
        if (hIdx >= 0) {
          val old = NumoFinancialEngine.investmentHoldings[hIdx]
          NumoFinancialEngine.investmentHoldings[hIdx] = old.copy(currentValuation = action.amount)
        }
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
}
