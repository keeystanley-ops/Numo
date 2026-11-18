package com.example.voice

import android.util.Log
import com.example.data.NumoFinancialEngine
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.FunctionCallPart
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.InlineDataPart
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.Locale
import java.util.UUID

sealed interface GeminiChatStreamEvent {
  data class Delta(val accumulatedText: String) : GeminiChatStreamEvent
  data class Completed(
    val replyText: String,
    val spokenText: String,
    val pendingAction: PendingFinancialAction?
  ) : GeminiChatStreamEvent
}

/**
 * Multi-turn conversational AI client powered directly by Firebase AI Logic (`Firebase.ai`)
 * with lazy model initialization, role system instructions, function tools bound to
 * `NumoFinancialEngine`, and token streaming.
 */
object GeminiHeyNumoClient {
  private const val TAG = "GeminiHeyNumoClient"
  private const val MAX_TOOL_ROUNDS = 4

  // Lazily obtain FirebaseAI only when inference is actually requested (never at construction time).
  private val firebaseAi by lazy { Firebase.ai }

  private var activeChat: Chat? = null
  private var activeModelId: String = HeyNumoSettings.DEFAULT_GEMINI_MODEL
  private var activeUserKey: String = ""

  private val functionDeclarations by lazy {
    listOf(
      FunctionDeclaration(
        name = "getAccountBalances",
        description = "Get the user's linked bank, mobile money (M-PESA), MMF, SACCO, and cash account balances.",
        parameters = emptyMap()
      ),
      FunctionDeclaration(
        name = "getAvailableCash",
        description = "Get the user's total liquid available cash balance and currency.",
        parameters = emptyMap()
      ),
      FunctionDeclaration(
        name = "getTransactions",
        description = "Get recent transactions, optionally filtered by category or limit.",
        parameters = mapOf(
          "category" to Schema.string("Optional category filter, e.g. Food & Dining, Transport"),
          "limit" to Schema.integer("Maximum number of recent transactions to return (default 5)")
        ),
        optionalParameters = listOf("category", "limit")
      ),
      FunctionDeclaration(
        name = "getBudgetStatus",
        description = "Get monthly budget status overall or for a specific spending category.",
        parameters = mapOf(
          "category" to Schema.string("Optional category name such as Food, Transport, Shopping, Bills")
        ),
        optionalParameters = listOf("category")
      ),
      FunctionDeclaration(
        name = "getUpcomingBills",
        description = "Get upcoming bills, amounts, due dates, paid status, and total unpaid obligations.",
        parameters = emptyMap()
      ),
      FunctionDeclaration(
        name = "getSavingsGoals",
        description = "Get all savings goals, saved amounts, target amounts, and remaining amounts.",
        parameters = emptyMap()
      ),
      FunctionDeclaration(
        name = "getCashFlowSummary",
        description = "Get monthly income, monthly expenses, net cash flow, and recurring schedules.",
        parameters = emptyMap()
      ),
      FunctionDeclaration(
        name = "getInvestmentPortfolio",
        description = "Get investment holdings, total portfolio valuation, invested capital, and return.",
        parameters = emptyMap()
      ),
      FunctionDeclaration(
        name = "getDividendForecast",
        description = "Get projected annual and monthly dividend/yield income from investments.",
        parameters = emptyMap()
      ),
      FunctionDeclaration(
        name = "getNetWorth",
        description = "Get the user's total net worth combining cash, investments, and unpaid bills.",
        parameters = emptyMap()
      ),
      FunctionDeclaration(
        name = "getSpendingAnalytics",
        description = "Get category-by-category spending breakdown and highest spending insights.",
        parameters = emptyMap()
      ),
      FunctionDeclaration(
        name = "createTransaction",
        description = "Propose recording a new expense or income transaction. Requires explicit user confirmation.",
        parameters = mapOf(
          "amount" to Schema.double("Transaction amount"),
          "description" to Schema.string("Short description or merchant/item name, e.g. Lunch"),
          "account" to Schema.string("Account name such as M-PESA, KCB, Equity, or Cash"),
          "category" to Schema.string("Category such as Food & Dining, Transport, Shopping, Bills & Utilities, Income"),
          "isIncome" to Schema.boolean("True if income/deposit, false if expense")
        )
      ),
      FunctionDeclaration(
        name = "payBill",
        description = "Propose paying an upcoming bill from the user's primary wallet. Requires explicit user confirmation.",
        parameters = mapOf(
          "billTitle" to Schema.string("Title of the unpaid bill to pay"),
          "account" to Schema.string("Source account name, e.g. M-PESA")
        ),
        optionalParameters = listOf("account")
      ),
      FunctionDeclaration(
        name = "updateTransaction",
        description = "Propose updating an existing transaction category or amount. Requires explicit user confirmation.",
        parameters = mapOf(
          "title" to Schema.string("Existing transaction title to update"),
          "newCategory" to Schema.string("Updated category"),
          "newAmount" to Schema.double("Optional updated amount")
        ),
        optionalParameters = listOf("newCategory", "newAmount")
      ),
      FunctionDeclaration(
        name = "createBudget",
        description = "Propose setting or updating a monthly budget limit for a category. Requires explicit user confirmation.",
        parameters = mapOf(
          "category" to Schema.string("Budget category name"),
          "monthlyLimit" to Schema.double("Proposed monthly budget limit")
        )
      ),
      FunctionDeclaration(
        name = "updateSavingsGoal",
        description = "Propose contributing funds toward a savings goal. Requires explicit user confirmation.",
        parameters = mapOf(
          "goalTitle" to Schema.string("Savings goal name, e.g. Emergency Fund"),
          "amount" to Schema.double("Contribution amount"),
          "account" to Schema.string("Source account, e.g. M-PESA")
        ),
        optionalParameters = listOf("account")
      ),
      FunctionDeclaration(
        name = "updateAssetValuation",
        description = "Propose updating an investment or asset valuation. Requires explicit user confirmation.",
        parameters = mapOf(
          "assetName" to Schema.string("Asset or holding name"),
          "newValuation" to Schema.double("Updated valuation amount")
        )
      )
    )
  }

  private fun createModel(
    modelId: String,
    userName: String,
    customAccounts: List<Pair<String, String>>
  ): GenerativeModel {
    val safeModel = modelId.ifBlank { HeyNumoSettings.DEFAULT_GEMINI_MODEL }
    return firebaseAi.generativeModel(
      modelName = safeModel,
      generationConfig = generationConfig {
        temperature = 0.65f
      },
      tools = listOf(Tool.functionDeclarations(functionDeclarations)),
      systemInstruction = buildSystemInstruction(userName, customAccounts)
    )
  }

  private fun buildSystemInstruction(
    userName: String,
    customAccounts: List<Pair<String, String>>
  ): Content {
    val firstName = userName.trim().substringBefore(" ").ifBlank { "" }
    val currency = NumoFinancialEngine.currency
    val linkedList = if (customAccounts.isEmpty()) {
      NumoFinancialEngine.accounts.joinToString(", ") { "${it.name} (${currency} ${it.balance.toInt()})" }
    } else {
      customAccounts.joinToString(", ") { "${it.first} (${it.second})" }
    }

    val instructionText = buildString {
      appendLine("You are Numo (\"Hey Numo\"), a warm, calm, intelligent Personal Finance Assistant inside the Numo Personal Finance OS.")
      if (firstName.isNotEmpty()) {
        appendLine("The user's first name is $firstName. Use their name occasionally when natural, not in every reply.")
      }
      appendLine("The user's active currency is $currency. Linked accounts: $linkedList.")
      appendLine()
      appendLine("ROLE & CONVERSATION BEHAVIOR:")
      appendLine("- Sound like a knowledgeable, supportive personal finance partner — concise, human, and insightful.")
      appendLine("- Never use generic robotic filler phrases such as \"Certainly!\", \"As an AI language model...\", \"How may I assist you today?\", or \"I hope this helps!\".")
      appendLine("- Answer the user's question directly in 1–3 clear sentences first before offering brief actionable context.")
      appendLine("- Use multi-turn conversation history naturally for follow-up questions (e.g., \"What if I spend 500 more?\", \"And my other goals?\").")
      appendLine("- Ask a short clarifying question if a request is ambiguous (e.g., which account or category to use).")
      appendLine()
      appendLine("GROUNDING & FINANCIAL TOOL RULES:")
      appendLine("- ALWAYS call the financial read tools (getAvailableCash, getAccountBalances, getBudgetStatus, getTransactions, getUpcomingBills, getSavingsGoals, getCashFlowSummary, getInvestmentPortfolio, getNetWorth, getSpendingAnalytics) before stating numbers — never invent balances, expenses, or portfolio figures.")
      appendLine("- Never guarantee future investment returns; explain risk/yield objectively.")
      appendLine("- When the user asks to record an expense/income, pay a bill, contribute to a savings goal, adjust a budget, or update an asset, ALWAYS call the corresponding action proposal tool (createTransaction, payBill, updateSavingsGoal, createBudget, updateTransaction, updateAssetValuation). This creates an Awaiting Approval confirmation card in the app; never claim a change was saved before the user taps Confirm.")
    }

    return content {
      text(instructionText)
    }
  }

  private fun getOrCreateChat(
    userId: String,
    userName: String,
    customAccounts: List<Pair<String, String>>,
    resetHistory: Boolean
  ): Chat {
    val desiredModel = HeyNumoSettings.selectedModel
    val priorHistory: List<Content> = if (resetHistory || activeUserKey != userId) {
      emptyList()
    } else {
      activeChat?.history?.toList().orEmpty()
    }

    val needsRebuild = resetHistory ||
      activeChat == null ||
      activeUserKey != userId ||
      activeModelId != desiredModel

    if (needsRebuild) {
      val model = createModel(desiredModel, userName, customAccounts)
      activeChat = model.startChat(history = priorHistory)
      activeModelId = desiredModel
      activeUserKey = userId
    }

    return activeChat!!
  }

  fun resetChatSession() {
    activeChat = null
  }

  fun sendChatStream(
    userId: String,
    userName: String,
    message: String,
    customAccounts: List<Pair<String, String>>,
    resetHistory: Boolean
  ): Flow<GeminiChatStreamEvent> {
    val chat = getOrCreateChat(
      userId = userId,
      userName = userName,
      customAccounts = customAccounts,
      resetHistory = resetHistory
    )
    return streamChatTurn(
      chat = chat,
      initialContent = content("user") { text(message) },
      customAccounts = customAccounts
    )
  }

  fun sendAudioChatStream(
    userId: String,
    userName: String,
    audioWavBytes: ByteArray,
    customAccounts: List<Pair<String, String>>,
    resetHistory: Boolean
  ): Flow<GeminiChatStreamEvent> {
    val chat = getOrCreateChat(
      userId = userId,
      userName = userName,
      customAccounts = customAccounts,
      resetHistory = resetHistory
    )
    val audioContent = content("user") {
      part(InlineDataPart(audioWavBytes, "audio/wav"))
      text("Respond naturally and directly to the user's spoken request using their Numo financial data.")
    }
    return streamChatTurn(
      chat = chat,
      initialContent = audioContent,
      customAccounts = customAccounts
    )
  }

  private fun streamChatTurn(
    chat: Chat,
    initialContent: Content,
    customAccounts: List<Pair<String, String>>
  ): Flow<GeminiChatStreamEvent> = flow {
    val accumulatedText = StringBuilder()
    var latestPendingAction: PendingFinancialAction? = null
    var nextInput: Content = initialContent

    for (round in 0 until MAX_TOOL_ROUNDS) {
      val pendingCalls = mutableListOf<FunctionCallPart>()

      chat.sendMessageStream(nextInput).collect { chunk ->
        val chunkText = chunk.text
        if (!chunkText.isNullOrEmpty()) {
          accumulatedText.append(chunkText)
          emit(GeminiChatStreamEvent.Delta(accumulatedText.toString()))
        }
        if (chunk.functionCalls.isNotEmpty()) {
          pendingCalls.addAll(chunk.functionCalls)
        }
      }

      if (pendingCalls.isEmpty()) {
        break
      }

      val functionResponses = mutableListOf<FunctionResponsePart>()
      for (call in pendingCalls) {
        val execution = executeToolCall(call, customAccounts)
        if (execution.pendingAction != null) {
          latestPendingAction = execution.pendingAction
        }
        functionResponses.add(
          FunctionResponsePart(
            name = call.name,
            response = execution.responseJson,
            id = call.id
          )
        )
      }

      nextInput = content("function") {
        functionResponses.forEach { part(it) }
      }
    }

    val finalText = accumulatedText.toString().trim()
    if (finalText.isEmpty() && latestPendingAction == null) {
      throw IllegalStateException("Gemini returned an empty response. Please try again.")
    }

    val resolvedText = finalText.ifEmpty {
      val action = latestPendingAction!!
      "I've prepared ${action.description} for ${NumoFinancialEngine.currency} ${action.amount.toInt()}. Tap Confirm when you're ready to apply it."
    }

    emit(
      GeminiChatStreamEvent.Completed(
        replyText = resolvedText,
        spokenText = resolvedText,
        pendingAction = latestPendingAction
      )
    )
  }

  private data class ToolExecutionOutcome(
    val responseJson: JsonObject,
    val pendingAction: PendingFinancialAction? = null
  )

  private fun executeToolCall(
    call: FunctionCallPart,
    customAccounts: List<Pair<String, String>>
  ): ToolExecutionOutcome {
    val args = call.args
    val currency = NumoFinancialEngine.currency

    return when (call.name) {
      "getAccountBalances" -> ToolExecutionOutcome(
        responseJson = buildJsonObject {
          put("currency", currency)
          put("totalAvailableCash", NumoFinancialEngine.availableCash)
          put(
            "accounts",
            buildJsonArray {
              NumoFinancialEngine.accounts.forEach { acc ->
                add(buildJsonObject {
                  put("name", acc.name)
                  put("provider", acc.provider)
                  put("balance", acc.balance)
                })
              }
              customAccounts.forEach { (name, bal) ->
                add(buildJsonObject {
                  put("name", name)
                  put("provider", "Linked Account")
                  put("balance", bal)
                })
              }
            }
          )
        }
      )

      "getAvailableCash" -> ToolExecutionOutcome(
        responseJson = buildJsonObject {
          put("currency", currency)
          put("availableCash", NumoFinancialEngine.availableCash)
        }
      )

      "getTransactions" -> {
        val catFilter = args["category"].asStringOrNull()?.lowercase(Locale.US).orEmpty()
        val limit = (args["limit"].asIntOrNull() ?: 5).coerceIn(1, 20)
        val filtered = if (catFilter.isNotBlank()) {
          NumoFinancialEngine.transactions.filter {
            it.category.lowercase(Locale.US).contains(catFilter) ||
              it.title.lowercase(Locale.US).contains(catFilter)
          }
        } else {
          NumoFinancialEngine.transactions.toList()
        }.take(limit)

        ToolExecutionOutcome(
          responseJson = buildJsonObject {
            put("currency", currency)
            put(
              "transactions",
              buildJsonArray {
                filtered.forEach { tx ->
                  add(buildJsonObject {
                    put("title", tx.title)
                    put("amount", tx.amount)
                    put("isIncome", tx.isIncome)
                    put("category", tx.category)
                    put("account", tx.account)
                    put("timestamp", tx.timestamp)
                  })
                }
              }
            )
          }
        )
      }

      "getBudgetStatus" -> {
        val catFilter = args["category"].asStringOrNull()?.lowercase(Locale.US).orEmpty()
        val matched = if (catFilter.isNotBlank()) {
          NumoFinancialEngine.categories.filter {
            it.name.lowercase(Locale.US).contains(catFilter)
          }
        } else {
          NumoFinancialEngine.categories.toList()
        }

        ToolExecutionOutcome(
          responseJson = buildJsonObject {
            put("currency", currency)
            put("monthlyBudgetTarget", NumoFinancialEngine.monthlyBudgetTarget)
            put("totalSpent", NumoFinancialEngine.totalSpent)
            put("totalRemaining", NumoFinancialEngine.totalRemaining)
            put("budgetConsumptionPercent", NumoFinancialEngine.budgetConsumptionPercent)
            put(
              "categories",
              buildJsonArray {
                matched.forEach { cat ->
                  add(buildJsonObject {
                    put("name", cat.name)
                    put("limit", cat.limit)
                    put("spent", cat.spent)
                    put("remaining", (cat.limit - cat.spent).coerceAtLeast(0.0))
                    put("percentageUsed", cat.percentage)
                  })
                }
              }
            )
          }
        )
      }

      "getUpcomingBills" -> {
        val unpaid = NumoFinancialEngine.bills.filter { !it.isPaid }
        ToolExecutionOutcome(
          responseJson = buildJsonObject {
            put("currency", currency)
            put("totalUnpaid", unpaid.sumOf { it.remainingAmount })
            put(
              "bills",
              buildJsonArray {
                NumoFinancialEngine.bills.forEach { b ->
                  add(buildJsonObject {
                    put("title", b.title)
                    put("amount", b.amount)
                    put("remainingAmount", b.remainingAmount)
                    put("dueDateText", b.dueDateText)
                    put("category", b.category)
                    put("isPaid", b.isPaid)
                  })
                }
              }
            )
          }
        )
      }

      "getSavingsGoals" -> ToolExecutionOutcome(
        responseJson = buildJsonObject {
          put("currency", currency)
          put("totalSaved", NumoFinancialEngine.goals.sumOf { it.savedAmount })
          put(
            "goals",
            buildJsonArray {
              NumoFinancialEngine.goals.forEach { g ->
                add(buildJsonObject {
                  put("title", g.title)
                  put("savedAmount", g.savedAmount)
                  put("targetAmount", g.targetAmount)
                  put("remainingAmount", (g.targetAmount - g.savedAmount).coerceAtLeast(0.0))
                  put("monthlyContribution", g.monthlyContribution)
                  put("percentage", g.percentage)
                })
              }
            }
          )
        }
      )

      "getCashFlowSummary" -> ToolExecutionOutcome(
        responseJson = buildJsonObject {
          put("currency", currency)
          put("monthlyIncomeTotal", NumoFinancialEngine.monthlyIncomeTotal)
          put("monthlyExpenseTotal", NumoFinancialEngine.monthlyExpenseTotal)
          put("netCashFlow", NumoFinancialEngine.netCashFlow)
          put("recurringSchedulesCount", NumoFinancialEngine.recurringSchedules.size)
        }
      )

      "getInvestmentPortfolio" -> ToolExecutionOutcome(
        responseJson = buildJsonObject {
          put("currency", currency)
          put("totalPortfolioValuation", NumoFinancialEngine.totalPortfolioValuation)
          put("totalInvestedCapital", NumoFinancialEngine.totalInvestedCapital)
          put("totalPortfolioReturn", NumoFinancialEngine.totalPortfolioReturn)
          put("totalPortfolioReturnPercent", NumoFinancialEngine.totalPortfolioReturnPercent)
          put(
            "holdings",
            buildJsonArray {
              NumoFinancialEngine.investmentHoldings.forEach { h ->
                add(buildJsonObject {
                  put("name", h.name)
                  put("assetType", h.assetType)
                  put("currentValuation", h.currentValuation)
                  put("investedCapital", h.investedCapital)
                  put("annualYieldPercent", h.annualYieldPercent)
                })
              }
            }
          )
        }
      )

      "getDividendForecast" -> {
        val annual = NumoFinancialEngine.investmentHoldings.sumOf {
          it.currentValuation * (it.annualYieldPercent / 100.0)
        }
        ToolExecutionOutcome(
          responseJson = buildJsonObject {
            put("currency", currency)
            put("estimatedAnnualYield", annual)
            put("estimatedMonthlyYield", annual / 12.0)
          }
        )
      }

      "getNetWorth" -> {
        val totalUnpaidBills = NumoFinancialEngine.bills.filter { !it.isPaid }.sumOf { it.remainingAmount }
        ToolExecutionOutcome(
          responseJson = buildJsonObject {
            put("currency", currency)
            put("totalNetWorth", NumoFinancialEngine.totalNetWorth)
            put("availableCash", NumoFinancialEngine.availableCash)
            put("totalPortfolioValuation", NumoFinancialEngine.totalPortfolioValuation)
            put("unpaidBillsTotal", totalUnpaidBills)
          }
        )
      }

      "getSpendingAnalytics" -> {
        val sorted = NumoFinancialEngine.categories.sortedByDescending { it.spent }
        ToolExecutionOutcome(
          responseJson = buildJsonObject {
            put("currency", currency)
            put("totalSpent", NumoFinancialEngine.totalSpent)
            put("monthlyBudgetTarget", NumoFinancialEngine.monthlyBudgetTarget)
            put(
              "topCategories",
              buildJsonArray {
                sorted.forEach { c ->
                  add(buildJsonObject {
                    put("name", c.name)
                    put("spent", c.spent)
                    put("limit", c.limit)
                    put("percentageUsed", c.percentage)
                  })
                }
              }
            )
          }
        )
      }

      // Confirmation-gated action proposal tools (never mutate state directly)
      "createTransaction" -> {
        val action = PendingFinancialAction(
          id = UUID.randomUUID().toString(),
          kind = PendingActionKind.TRANSACTION,
          amount = (args["amount"].asDoubleOrNull() ?: 0.0).coerceAtLeast(0.0),
          description = args["description"].asStringOrNull().orEmpty().ifBlank { "Transaction" },
          account = args["account"].asStringOrNull().orEmpty().ifBlank { "M-PESA" },
          category = args["category"].asStringOrNull().orEmpty().ifBlank { "Other" },
          isIncome = args["isIncome"].asBooleanOrNull() ?: false
        )
        awaitingConfirmationOutcome(action)
      }

      "payBill" -> {
        val requestedTitle = args["billTitle"].asStringOrNull().orEmpty()
        val matchedBill = NumoFinancialEngine.bills.firstOrNull {
          !it.isPaid && it.title.contains(requestedTitle, ignoreCase = true)
        } ?: NumoFinancialEngine.bills.firstOrNull { !it.isPaid }
        val action = PendingFinancialAction(
          id = UUID.randomUUID().toString(),
          kind = PendingActionKind.PAY_BILL,
          amount = matchedBill?.amount ?: 0.0,
          description = matchedBill?.title ?: requestedTitle.ifBlank { "Bill Payment" },
          account = args["account"].asStringOrNull().orEmpty().ifBlank { "M-PESA" },
          category = matchedBill?.category ?: "Bills & Utilities",
          isIncome = false
        )
        awaitingConfirmationOutcome(action)
      }

      "updateSavingsGoal" -> {
        val goalTitle = args["goalTitle"].asStringOrNull().orEmpty().ifBlank {
          NumoFinancialEngine.goals.firstOrNull()?.title ?: "Emergency Fund"
        }
        val action = PendingFinancialAction(
          id = UUID.randomUUID().toString(),
          kind = PendingActionKind.CONTRIBUTE_GOAL,
          amount = (args["amount"].asDoubleOrNull() ?: 0.0).coerceAtLeast(0.0),
          description = goalTitle,
          account = args["account"].asStringOrNull().orEmpty().ifBlank { "M-PESA" },
          category = "Savings Goal",
          isIncome = false
        )
        awaitingConfirmationOutcome(action)
      }

      "createBudget" -> {
        val category = args["category"].asStringOrNull().orEmpty().ifBlank { "General" }
        val limit = (args["monthlyLimit"].asDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
        val action = PendingFinancialAction(
          id = UUID.randomUUID().toString(),
          kind = PendingActionKind.CREATE_BUDGET,
          amount = limit,
          description = "Set $category budget limit",
          account = "Monthly Budget",
          category = category,
          isIncome = false
        )
        awaitingConfirmationOutcome(action)
      }

      "updateTransaction" -> {
        val title = args["title"].asStringOrNull().orEmpty().ifBlank { "Transaction" }
        val action = PendingFinancialAction(
          id = UUID.randomUUID().toString(),
          kind = PendingActionKind.UPDATE_TRANSACTION,
          amount = (args["newAmount"].asDoubleOrNull() ?: 0.0).coerceAtLeast(0.0),
          description = title,
          account = "Ledger",
          category = args["newCategory"].asStringOrNull().orEmpty().ifBlank { "Other" },
          isIncome = false
        )
        awaitingConfirmationOutcome(action)
      }

      "updateAssetValuation" -> {
        val asset = args["assetName"].asStringOrNull().orEmpty().ifBlank { "Portfolio Asset" }
        val valAmount = (args["newValuation"].asDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
        val action = PendingFinancialAction(
          id = UUID.randomUUID().toString(),
          kind = PendingActionKind.UPDATE_ASSET,
          amount = valAmount,
          description = asset,
          account = "Investments",
          category = "Portfolio",
          isIncome = true
        )
        awaitingConfirmationOutcome(action)
      }

      else -> {
        Log.w(TAG, "Unknown Gemini tool call requested: ${call.name}")
        ToolExecutionOutcome(
          responseJson = buildJsonObject {
            put("error", "Unknown tool: ${call.name}")
          }
        )
      }
    }
  }

  private fun awaitingConfirmationOutcome(action: PendingFinancialAction): ToolExecutionOutcome {
    return ToolExecutionOutcome(
      responseJson = buildJsonObject {
        put("status", "AWAITING_USER_CONFIRMATION")
        put("requiresConfirmation", true)
        put("kind", action.kind.name)
        put("amount", action.amount)
        put("description", action.description)
        put("account", action.account)
        put("category", action.category)
        put("isIncome", action.isIncome)
      },
      pendingAction = action
    )
  }

  private fun JsonElement?.asStringOrNull(): String? =
    (this as? JsonPrimitive)?.contentOrNull

  private fun JsonElement?.asDoubleOrNull(): Double? =
    this?.jsonPrimitive?.doubleOrNull ?: this?.jsonPrimitive?.contentOrNull?.toDoubleOrNull()

  private fun JsonElement?.asIntOrNull(): Int? =
    this?.jsonPrimitive?.intOrNull ?: this?.jsonPrimitive?.contentOrNull?.toIntOrNull()

  private fun JsonElement?.asBooleanOrNull(): Boolean? =
    this?.jsonPrimitive?.booleanOrNull ?: this?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull()
}
