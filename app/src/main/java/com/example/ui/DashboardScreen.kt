package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.components.NumoBottomNavBar
import com.example.ui.components.NumoNavTab
import com.example.ui.profile.CategoriesScreen
import com.example.ui.profile.HelpSupportScreen
import com.example.ui.profile.LinkedAccountsScreen
import com.example.ui.profile.NotificationsScreen
import com.example.ui.profile.PersonalInformationScreen
import com.example.ui.profile.SecurityPrivacyScreen
import com.example.ui.voice.HeyNumoHost
import com.example.ui.voice.HeyNumoSubScreen
import com.example.voice.HeyNumoSettings
import com.example.voice.NumoVoiceAudioManager

private enum class NumoDashboardPage {
  HOME,
  ANALYTICS,
  BUDGET_CONTROL,
  TRANSACTIONS,
  FINANCIAL_GOALS,
  UPCOMING_BILLS,
  SAVINGS_GOALS,
  DEBT_MANAGEMENT,
  SPENDING_INSIGHTS,
  ADD_TRANSACTION,
  PROFILE,
  SETTINGS,
  PERSONAL_INFORMATION,
  LINKED_ACCOUNTS,
  SECURITY_PRIVACY,
  NOTIFICATIONS,
  CATEGORIES,
  HELP_SUPPORT,
  HEY_NUMO
}

@Composable
fun DashboardScreen(
  userEmail: String = "Ayesha Wanjiru",
  provider: String = "Google",
  userCurrency: String = "KES",
  customAccounts: List<Pair<String, String>> = emptyList(),
  onSignOut: () -> Unit = {}
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  var currentPage by remember { mutableStateOf(NumoDashboardPage.HOME) }
  var heyNumoInitialScreen by remember { mutableStateOf(HeyNumoSubScreen.HOME) }
  var heyNumoStartListening by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    HeyNumoSettings.init(context)
  }

  val wakeWordAudioManager = remember { NumoVoiceAudioManager(context) }

  DisposableEffect(wakeWordAudioManager) {
    wakeWordAudioManager.onWakeWordDetected = {
      heyNumoInitialScreen = HeyNumoSubScreen.LISTENING
      heyNumoStartListening = true
      currentPage = NumoDashboardPage.HEY_NUMO
    }
    onDispose {
      wakeWordAudioManager.destroy()
    }
  }

  LaunchedEffect(currentPage, HeyNumoSettings.wakeWordEnabled) {
    if (
      currentPage != NumoDashboardPage.HEY_NUMO &&
      HeyNumoSettings.wakeWordEnabled &&
      wakeWordAudioManager.hasRecordAudioPermission()
    ) {
      wakeWordAudioManager.startWakeWordListening()
    } else {
      wakeWordAudioManager.stopWakeWordListening()
    }
  }

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> {
          if (
            currentPage != NumoDashboardPage.HEY_NUMO &&
            HeyNumoSettings.wakeWordEnabled &&
            wakeWordAudioManager.hasRecordAudioPermission()
          ) {
            wakeWordAudioManager.startWakeWordListening()
          }
        }
        Lifecycle.Event.ON_PAUSE -> {
          wakeWordAudioManager.stopWakeWordListening()
        }
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  val displayName = remember(userEmail) {
    if (userEmail.contains("@")) {
      val namePart = userEmail.substringBefore("@")
      if (namePart.equals("user", ignoreCase = true) || namePart.isBlank()) {
        "Ayesha Wanjiru"
      } else {
        namePart.replaceFirstChar { it.uppercase() }
      }
    } else if (userEmail.isNotBlank()) {
      userEmail
    } else {
      "Ayesha Wanjiru"
    }
  }

  val emailDisplay = remember(userEmail) {
    if (userEmail.contains("@")) userEmail else "ayesha@numo.com"
  }

  val selectedNavTab = when (currentPage) {
    NumoDashboardPage.HOME,
    NumoDashboardPage.UPCOMING_BILLS,
    NumoDashboardPage.HEY_NUMO -> NumoNavTab.HOME
    NumoDashboardPage.ANALYTICS,
    NumoDashboardPage.SPENDING_INSIGHTS -> NumoNavTab.ANALYTICS
    NumoDashboardPage.BUDGET_CONTROL,
    NumoDashboardPage.TRANSACTIONS,
    NumoDashboardPage.ADD_TRANSACTION -> NumoNavTab.BUDGET
    NumoDashboardPage.FINANCIAL_GOALS,
    NumoDashboardPage.SAVINGS_GOALS,
    NumoDashboardPage.DEBT_MANAGEMENT -> NumoNavTab.GOALS
    NumoDashboardPage.PROFILE,
    NumoDashboardPage.SETTINGS,
    NumoDashboardPage.PERSONAL_INFORMATION,
    NumoDashboardPage.LINKED_ACCOUNTS,
    NumoDashboardPage.SECURITY_PRIVACY,
    NumoDashboardPage.NOTIFICATIONS,
    NumoDashboardPage.CATEGORIES,
    NumoDashboardPage.HELP_SUPPORT -> NumoNavTab.PROFILE
  }

  val showBottomBar = currentPage !in setOf(
    NumoDashboardPage.PERSONAL_INFORMATION,
    NumoDashboardPage.LINKED_ACCOUNTS,
    NumoDashboardPage.SECURITY_PRIVACY,
    NumoDashboardPage.HEY_NUMO
  )

  BackHandler(enabled = currentPage != NumoDashboardPage.HOME) {
    currentPage = when (currentPage) {
      NumoDashboardPage.UPCOMING_BILLS,
      NumoDashboardPage.ADD_TRANSACTION -> NumoDashboardPage.HOME
      NumoDashboardPage.SPENDING_INSIGHTS -> NumoDashboardPage.ANALYTICS
      NumoDashboardPage.TRANSACTIONS -> NumoDashboardPage.BUDGET_CONTROL
      NumoDashboardPage.SAVINGS_GOALS,
      NumoDashboardPage.DEBT_MANAGEMENT -> NumoDashboardPage.FINANCIAL_GOALS
      NumoDashboardPage.SETTINGS,
      NumoDashboardPage.PERSONAL_INFORMATION,
      NumoDashboardPage.LINKED_ACCOUNTS,
      NumoDashboardPage.SECURITY_PRIVACY,
      NumoDashboardPage.NOTIFICATIONS,
      NumoDashboardPage.CATEGORIES,
      NumoDashboardPage.HELP_SUPPORT -> NumoDashboardPage.PROFILE
      else -> NumoDashboardPage.HOME
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      when (currentPage) {
        NumoDashboardPage.HOME -> HomeScreen(
          userName = displayName,
          currency = userCurrency,
          onAddTransaction = { currentPage = NumoDashboardPage.ADD_TRANSACTION },
          onPayBill = { currentPage = NumoDashboardPage.UPCOMING_BILLS },
          onOpenTransactions = { currentPage = NumoDashboardPage.TRANSACTIONS },
          onOpenBudget = { currentPage = NumoDashboardPage.BUDGET_CONTROL },
          onOpenProfile = { currentPage = NumoDashboardPage.PROFILE },
          onOpenInsights = { currentPage = NumoDashboardPage.SPENDING_INSIGHTS },
          onOpenHeyNumo = {
            heyNumoInitialScreen = HeyNumoSubScreen.HOME
            heyNumoStartListening = false
            currentPage = NumoDashboardPage.HEY_NUMO
          }
        )

        NumoDashboardPage.ANALYTICS -> AnalyticsScreen(
          currency = userCurrency,
          onOpenInsights = { currentPage = NumoDashboardPage.SPENDING_INSIGHTS },
          onOpenSettings = { currentPage = NumoDashboardPage.SETTINGS },
          onBack = { currentPage = NumoDashboardPage.HOME }
        )

        NumoDashboardPage.BUDGET_CONTROL -> BudgetScreen(
          currency = userCurrency,
          onOpenTransactions = { currentPage = NumoDashboardPage.TRANSACTIONS },
          onOpenAddTransaction = { currentPage = NumoDashboardPage.ADD_TRANSACTION },
          onBack = { currentPage = NumoDashboardPage.HOME }
        )

        NumoDashboardPage.TRANSACTIONS -> TransactionsScreen(
          currency = userCurrency,
          onAddTransaction = { currentPage = NumoDashboardPage.ADD_TRANSACTION },
          onBack = { currentPage = NumoDashboardPage.BUDGET_CONTROL }
        )

        NumoDashboardPage.FINANCIAL_GOALS -> GoalsScreen(
          currency = userCurrency,
          onOpenSavingsGoalDetail = { currentPage = NumoDashboardPage.SAVINGS_GOALS },
          onOpenDebtManagement = { currentPage = NumoDashboardPage.DEBT_MANAGEMENT },
          onBack = { currentPage = NumoDashboardPage.HOME }
        )

        NumoDashboardPage.UPCOMING_BILLS -> UpcomingBillsScreen(
          currency = userCurrency,
          onBack = { currentPage = NumoDashboardPage.HOME }
        )

        NumoDashboardPage.SAVINGS_GOALS -> SavingsGoalsScreen(
          currency = userCurrency,
          onBack = { currentPage = NumoDashboardPage.FINANCIAL_GOALS }
        )

        NumoDashboardPage.DEBT_MANAGEMENT -> DebtManagementScreen(
          currency = userCurrency,
          onBack = { currentPage = NumoDashboardPage.FINANCIAL_GOALS }
        )

        NumoDashboardPage.SPENDING_INSIGHTS -> SpendingInsightsScreen(
          currency = userCurrency,
          onBack = { currentPage = NumoDashboardPage.ANALYTICS }
        )

        NumoDashboardPage.ADD_TRANSACTION -> AddTransactionScreen(
          currency = userCurrency,
          onCancel = { currentPage = NumoDashboardPage.HOME },
          onSave = { currentPage = NumoDashboardPage.TRANSACTIONS }
        )

        NumoDashboardPage.PROFILE -> ProfileScreen(
          userName = displayName,
          userEmail = emailDisplay,
          onOpenPersonalInfo = { currentPage = NumoDashboardPage.PERSONAL_INFORMATION },
          onOpenLinkedAccounts = { currentPage = NumoDashboardPage.LINKED_ACCOUNTS },
          onOpenSecurityPrivacy = { currentPage = NumoDashboardPage.SECURITY_PRIVACY },
          onOpenNotifications = { currentPage = NumoDashboardPage.NOTIFICATIONS },
          onOpenCategories = { currentPage = NumoDashboardPage.CATEGORIES },
          onOpenSettings = { currentPage = NumoDashboardPage.SETTINGS },
          onOpenHelpSupport = { currentPage = NumoDashboardPage.HELP_SUPPORT },
          onSignOut = onSignOut
        )

        NumoDashboardPage.SETTINGS -> SettingsScreen(
          onBack = { currentPage = NumoDashboardPage.PROFILE }
        )

        NumoDashboardPage.PERSONAL_INFORMATION -> PersonalInformationScreen(
          initialName = displayName,
          initialEmail = emailDisplay,
          onBack = { currentPage = NumoDashboardPage.PROFILE }
        )

        NumoDashboardPage.LINKED_ACCOUNTS -> LinkedAccountsScreen(
          currency = userCurrency,
          customAccounts = customAccounts,
          onBack = { currentPage = NumoDashboardPage.PROFILE }
        )

        NumoDashboardPage.SECURITY_PRIVACY -> SecurityPrivacyScreen(
          userEmail = emailDisplay,
          onSignOut = onSignOut,
          onBack = { currentPage = NumoDashboardPage.PROFILE }
        )

        NumoDashboardPage.NOTIFICATIONS -> NotificationsScreen(
          onBack = { currentPage = NumoDashboardPage.PROFILE }
        )

        NumoDashboardPage.CATEGORIES -> CategoriesScreen(
          onBack = { currentPage = NumoDashboardPage.PROFILE }
        )

        NumoDashboardPage.HELP_SUPPORT -> HelpSupportScreen(
          onBack = { currentPage = NumoDashboardPage.PROFILE }
        )

        NumoDashboardPage.HEY_NUMO -> HeyNumoHost(
          userName = displayName,
          customAccounts = customAccounts,
          initialScreen = heyNumoInitialScreen,
          startListeningImmediately = heyNumoStartListening,
          onOpenBudget = { currentPage = NumoDashboardPage.BUDGET_CONTROL },
          onOpenNotifications = { currentPage = NumoDashboardPage.NOTIFICATIONS },
          onBackToDashboard = { currentPage = NumoDashboardPage.HOME }
        )
      }
    }

    if (showBottomBar) {
      NumoBottomNavBar(
        selectedTab = selectedNavTab,
        onSelectTab = { tab ->
          currentPage = when (tab) {
            NumoNavTab.HOME -> NumoDashboardPage.HOME
            NumoNavTab.ANALYTICS -> NumoDashboardPage.ANALYTICS
            NumoNavTab.BUDGET -> NumoDashboardPage.BUDGET_CONTROL
            NumoNavTab.GOALS -> NumoDashboardPage.FINANCIAL_GOALS
            NumoNavTab.PROFILE -> NumoDashboardPage.PROFILE
          }
        }
      )
    }
  }
}
