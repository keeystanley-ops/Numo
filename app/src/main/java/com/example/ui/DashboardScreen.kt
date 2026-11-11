package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.animation.bouncyClickable

enum class DashboardTab {
  HOME,
  ANALYTICS,
  BUDGET,
  GOALS,
  PROFILE
}

/**
 * Numo Main Container coordinating Home and Analytics pages
 * with smooth tab transitions and springy bottom navigation interactions.
 */
@Composable
fun DashboardScreen(
  userEmail: String = "Ayesha Khan",
  provider: String = "Google",
  userCurrency: String = "KES",
  customAccounts: List<Pair<String, String>> = emptyList(),
  onSignOut: () -> Unit = {}
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Home, 1: Analytics, 2: Budget, 3: Goals, 4: Profile
  var showSpendingInsights by remember { mutableStateOf(false) }
  var showFinancialCalendar by remember { mutableStateOf(false) }

  val displayName = if (userEmail.contains("@")) {
    val namePart = userEmail.substringBefore("@")
    if (namePart.equals("user", ignoreCase = true) || namePart.isBlank()) "Ayesha Khan" else namePart.replaceFirstChar { it.uppercase() }
  } else {
    userEmail
  }

  // Handle back button smoothly
  BackHandler(enabled = (showSpendingInsights || showFinancialCalendar || selectedTab != 0)) {
    when {
      showSpendingInsights -> showSpendingInsights = false
      showFinancialCalendar -> showFinancialCalendar = false
      selectedTab != 0 -> selectedTab = 0
    }
  }

  if (showSpendingInsights) {
    SpendingInsightsScreen(
      currency = userCurrency,
      onBack = { showSpendingInsights = false }
    )
  } else if (showFinancialCalendar) {
    FinancialCalendarScreen(
      currency = userCurrency,
      onBack = { showFinancialCalendar = false }
    )
  } else {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = Color(0xFFF8FAF9),
      contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
      bottomBar = {
        NumoBottomNavigationBar(
          selectedTabIndex = selectedTab,
          onTabSelected = { index ->
            selectedTab = index
          }
        )
      }
    ) { paddingValues ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(bottom = paddingValues.calculateBottomPadding())
      ) {
        AnimatedContent(
          targetState = selectedTab,
          transitionSpec = {
            if (targetState > initialState) {
              (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { width -> width / 4 } + fadeIn(tween(250)))
                .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { width -> -width / 4 } + fadeOut(tween(200)))
            } else {
              (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { width -> -width / 4 } + fadeIn(tween(250)))
                .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { width -> width / 4 } + fadeOut(tween(200)))
            }
          },
          label = "dashboardTabTransition"
        ) { tabIndex ->
          when (tabIndex) {
            0 -> {
              HomeScreen(
                userName = displayName,
                currency = userCurrency,
                onNavigateToAnalytics = { selectedTab = 1 },
                onNavigateToCalendar = { showFinancialCalendar = true },
                onNavigateToInsights = { showSpendingInsights = true }
              )
            }
            1 -> {
              AnalyticsScreen(
                currency = userCurrency,
                onBack = { selectedTab = 0 }
              )
            }
            2 -> {
              BudgetScreen(
                currency = userCurrency,
                onBack = { selectedTab = 0 }
              )
            }
            3 -> {
              GoalsScreen(
                currency = userCurrency,
                onBack = { selectedTab = 0 }
              )
            }
            4 -> {
              ProfileScreen(
                userName = displayName,
                userEmail = userEmail,
                currency = userCurrency,
                onSignOut = onSignOut
              )
            }
            else -> {
              HomeScreen(
                userName = displayName,
                currency = userCurrency,
                onNavigateToAnalytics = { selectedTab = 1 },
                onNavigateToCalendar = { showFinancialCalendar = true },
                onNavigateToInsights = { showSpendingInsights = true }
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Numo 5-Tab Bottom Navigation Bar with tactile feedback and spring selection indicators.
 */
@Composable
fun NumoBottomNavigationBar(
  selectedTabIndex: Int,
  onTabSelected: (Int) -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .navigationBarsPadding(),
    color = Color.White,
    shadowElevation = 12.dp
  ) {
    Column {
      // Top subtle border
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(1.dp)
          .background(Color(0xFFF1F5F9))
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Tab 0: Home
        BottomNavTabItem(
          icon = if (selectedTabIndex == 0) Icons.Filled.Home else Icons.Outlined.Home,
          label = "Home",
          isSelected = (selectedTabIndex == 0),
          onClick = { onTabSelected(0) },
          testTag = "nav_tab_home"
        )

        // Tab 1: Analytics
        BottomNavTabItem(
          icon = Icons.Outlined.BarChart,
          label = "Analytics",
          isSelected = (selectedTabIndex == 1),
          onClick = { onTabSelected(1) },
          testTag = "nav_tab_analytics"
        )

        // Tab 2: Budget (Center Badge Icon)
        BottomNavCenterBadgeTab(
          icon = Icons.Default.AccountBalanceWallet,
          label = "Budget",
          isSelected = (selectedTabIndex == 2),
          onClick = { onTabSelected(2) },
          testTag = "nav_tab_budget"
        )

        // Tab 3: Goals
        BottomNavTabItem(
          icon = Icons.Outlined.FavoriteBorder,
          label = "Goals",
          isSelected = (selectedTabIndex == 3),
          onClick = { onTabSelected(3) },
          testTag = "nav_tab_goals"
        )

        // Tab 4: Profile
        BottomNavTabItem(
          icon = Icons.Outlined.PersonOutline,
          label = "Profile",
          isSelected = (selectedTabIndex == 4),
          onClick = { onTabSelected(4) },
          testTag = "nav_tab_profile"
        )
      }
    }
  }
}

@Composable
fun BottomNavTabItem(
  icon: ImageVector,
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  testTag: String
) {
  val activeColor = Color(0xFF02844E)
  val inactiveColor = Color(0xFF64748B)

  val iconScale by animateFloatAsState(
    targetValue = if (isSelected) 1.08f else 1.0f,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "tabIconScale"
  )

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .bouncyClickable { onClick() }
      .padding(horizontal = 12.dp, vertical = 4.dp)
      .testTag(testTag)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = if (isSelected) activeColor else inactiveColor,
      modifier = Modifier
        .size(24.dp)
        .graphicsLayer {
          scaleX = iconScale
          scaleY = iconScale
        }
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) activeColor else inactiveColor
    )
  }
}

@Composable
fun BottomNavCenterBadgeTab(
  icon: ImageVector,
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  testTag: String
) {
  val activeColor = Color(0xFF02844E)
  val inactiveColor = Color(0xFF64748B)

  val badgeScale by animateFloatAsState(
    targetValue = if (isSelected) 1.12f else 1.0f,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "centerBadgeScale"
  )

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .bouncyClickable { onClick() }
      .padding(horizontal = 10.dp, vertical = 2.dp)
      .testTag(testTag)
  ) {
    Box(
      modifier = Modifier
        .size(34.dp)
        .graphicsLayer {
          scaleX = badgeScale
          scaleY = badgeScale
        }
        .clip(CircleShape)
        .background(if (isSelected) Color(0xFF02844E) else Color(0xFFE8F8F2))
        .border(1.dp, Color(0xFFD1F2E5), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (isSelected) Color.White else Color(0xFF02844E),
        modifier = Modifier.size(18.dp)
      )
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) activeColor else inactiveColor
    )
  }
}
