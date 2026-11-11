package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.animation.TopScrollProgressBar
import com.example.ui.animation.animateIntCount
import com.example.ui.animation.animateProgress
import com.example.ui.animation.bouncyClickable
import com.example.ui.animation.formatAmount
import com.example.ui.animation.staggeredEntrance

/**
 * Financial Goals Screen matching Screen 7 & Goal Progress detail (Screen 11).
 * Features:
 * - Top header with "+ Add Goal" action
 * - Hero Card ("Build Your Future") with mountain silhouette gradient art
 * - Pill filter: [My Goals | Suggested]
 * - Goal list items:
 *   - Emergency Fund (48%, KES 120,000 / 250,000)
 *   - Dream Vacation (0%, KES 0 / 200,000)
 *   - New Laptop (60%, KES 60,000 / 100,000)
 * - Tapping Emergency Fund opens Screen 11 (Goal Progress):
 *   - 72% circular ring gauge (KES 180,000 of 250,000)
 *   - Emergency Fund / Target: 3 months expenses
 *   - Smooth area growth chart
 *   - Monthly Contribution card (KES 10,000 [Edit])
 */
@Composable
fun GoalsScreen(
  currency: String = "KES",
  onBack: () -> Unit = {}
) {
  var selectedGoalDetail by remember { mutableStateOf<String?>(null) }
  var selectedPillTab by remember { mutableIntStateOf(0) } // 0: My Goals, 1: Suggested
  var showAddGoalDialog by remember { mutableStateOf(false) }

  AnimatedContent(
    targetState = selectedGoalDetail,
    transitionSpec = {
      if (targetState != null) {
        (slideInHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn(tween(250)))
          .togetherWith(slideOutHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { -it / 3 } + fadeOut(tween(200)))
      } else {
        (slideInHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { -it / 3 } + fadeIn(tween(250)))
          .togetherWith(slideOutHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeOut(tween(200)))
      }
    },
    label = "goalsNavigationTransition"
  ) { detailGoal ->
    if (detailGoal != null) {
      // SCREEN 11: GOAL PROGRESS DETAIL
      GoalProgressDetailScreen(
        goalName = detailGoal,
        currency = currency,
        onBack = { selectedGoalDetail = null }
      )
    } else {
      // SCREEN 7: FINANCIAL GOALS MAIN
      GoalsMainContent(
        currency = currency,
        selectedPillTab = selectedPillTab,
        onPillTabSelected = { selectedPillTab = it },
        onGoalClick = { goalName -> selectedGoalDetail = goalName },
        onAddGoalClick = { showAddGoalDialog = true }
      )
    }
  }

  if (showAddGoalDialog) {
    AddGoalDialog(
      currency = currency,
      onDismiss = { showAddGoalDialog = false },
      onSave = { _, _ -> showAddGoalDialog = false }
    )
  }
}

@Composable
fun GoalsMainContent(
  currency: String,
  selectedPillTab: Int,
  onPillTabSelected: (Int) -> Unit,
  onGoalClick: (String) -> Unit,
  onAddGoalClick: () -> Unit
) {
  val scrollState = rememberScrollState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAF9))
  ) {
    TopScrollProgressBar(scrollState = scrollState)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      // Top Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Financial Goals",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A),
          modifier = Modifier.testTag("goals_title")
        )

        IconButton(
          onClick = onAddGoalClick,
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color(0xFFE8F8F2))
            .bouncyClickable { onAddGoalClick() }
            .testTag("btn_add_goal")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Goal",
            tint = Color(0xFF02844E),
            modifier = Modifier.size(22.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // HERO CARD: "Build Your Future" with Mountain Graphic
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(150.dp)
          .staggeredEntrance(delayMs = 40)
          .shadow(8.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFF1E3A8A).copy(alpha = 0.3f))
          .clip(RoundedCornerShape(22.dp))
          .background(
            Brush.linearGradient(
              colors = listOf(
                Color(0xFF1E3A8A),
                Color(0xFF1E40AF),
                Color(0xFF0284C7)
              )
            )
          )
      ) {
        // Mountain silhouettes Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height

          // Back Mountain
          val pathBack = Path().apply {
            moveTo(w * 0.45f, h)
            lineTo(w * 0.75f, h * 0.35f)
            lineTo(w, h * 0.7f)
            lineTo(w, h)
            close()
          }
          drawPath(pathBack, color = Color(0xFF38BDF8).copy(alpha = 0.25f))

          // Front Mountain
          val pathFront = Path().apply {
            moveTo(w * 0.6f, h)
            lineTo(w * 0.85f, h * 0.42f)
            lineTo(w * 0.95f, h * 0.62f)
            lineTo(w * 1.05f, h * 0.48f)
            lineTo(w * 1.15f, h)
            close()
          }
          drawPath(pathFront, color = Color(0xFF0284C7).copy(alpha = 0.45f))

          // Subtle sun glow circle
          drawCircle(
            color = Color(0xFFBAE6FD).copy(alpha = 0.35f),
            radius = 32.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(w * 0.82f, h * 0.30f)
          )
        }

        // Card Content
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(22.dp),
          verticalArrangement = Arrangement.Center
        ) {
          Text(
            text = "Build Your\nFuture",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            lineHeight = 26.sp
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Set goals, track progress,\nand make it happen.",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.85f),
            lineHeight = 16.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // PILL SELECTOR: [My Goals | Suggested]
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(24.dp))
          .background(Color(0xFFEDF2F7))
          .padding(4.dp)
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(if (selectedPillTab == 0) Color(0xFF02844E) else Color.Transparent)
            .bouncyClickable { onPillTabSelected(0) }
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "My Goals",
            fontSize = 13.sp,
            fontWeight = if (selectedPillTab == 0) FontWeight.Bold else FontWeight.Medium,
            color = if (selectedPillTab == 0) Color.White else Color(0xFF64748B)
          )
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(if (selectedPillTab == 1) Color(0xFF02844E) else Color.Transparent)
            .bouncyClickable { onPillTabSelected(1) }
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Suggested",
            fontSize = 13.sp,
            fontWeight = if (selectedPillTab == 1) FontWeight.Bold else FontWeight.Medium,
            color = if (selectedPillTab == 1) Color.White else Color(0xFF64748B)
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      if (selectedPillTab == 0) {
        // 1. Emergency Fund (48%)
        GoalCardItem(
          title = "Emergency Fund",
          currentAmount = 120000,
          targetAmount = 250000,
          percentage = 48,
          currency = currency,
          icon = Icons.Default.Shield,
          iconBg = Color(0xFFE8F8F2),
          iconTint = Color(0xFF02844E),
          progressColor = Color(0xFF02844E),
          delayMs = 80,
          onClick = { onGoalClick("Emergency Fund") },
          testTag = "goal_emergency_fund"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Dream Vacation (0%)
        GoalCardItem(
          title = "Dream Vacation",
          currentAmount = 0,
          targetAmount = 200000,
          percentage = 0,
          currency = currency,
          icon = Icons.Default.Flight,
          iconBg = Color(0xFFEFF6FF),
          iconTint = Color(0xFF2563EB),
          progressColor = Color(0xFF2563EB),
          delayMs = 120,
          onClick = { onGoalClick("Dream Vacation") },
          testTag = "goal_dream_vacation"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. New Laptop (60%)
        GoalCardItem(
          title = "New Laptop",
          currentAmount = 60000,
          targetAmount = 100000,
          percentage = 60,
          currency = currency,
          icon = Icons.Default.Laptop,
          iconBg = Color(0xFFF5F3FF),
          iconTint = Color(0xFF7C3AED),
          progressColor = Color(0xFF7C3AED),
          delayMs = 160,
          onClick = { onGoalClick("New Laptop") },
          testTag = "goal_new_laptop"
        )
      } else {
        // SUGGESTED GOALS
        GoalCardItem(
          title = "Home Down Payment",
          currentAmount = 0,
          targetAmount = 1500000,
          percentage = 0,
          currency = currency,
          icon = Icons.Outlined.Flag,
          iconBg = Color(0xFFFEF3C7),
          iconTint = Color(0xFFF59E0B),
          progressColor = Color(0xFFF59E0B),
          delayMs = 80,
          onClick = { onAddGoalClick() },
          testTag = "goal_suggested_home"
        )
        Spacer(modifier = Modifier.height(12.dp))
        GoalCardItem(
          title = "Retirement Booster",
          currentAmount = 0,
          targetAmount = 500000,
          percentage = 0,
          currency = currency,
          icon = Icons.Default.Shield,
          iconBg = Color(0xFFE8F8F2),
          iconTint = Color(0xFF02844E),
          progressColor = Color(0xFF02844E),
          delayMs = 120,
          onClick = { onAddGoalClick() },
          testTag = "goal_suggested_retire"
        )
      }

      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}

/**
 * Individual Goal Card matching Screen 7
 */
@Composable
fun GoalCardItem(
  title: String,
  currentAmount: Int,
  targetAmount: Int,
  percentage: Int,
  currency: String,
  icon: ImageVector,
  iconBg: Color,
  iconTint: Color,
  progressColor: Color,
  delayMs: Int,
  onClick: () -> Unit,
  testTag: String
) {
  val animatedProgress = animateProgress(percentage / 100f, durationMs = 850)

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .staggeredEntrance(delayMs = delayMs)
      .clip(RoundedCornerShape(18.dp))
      .bouncyClickable { onClick() }
      .testTag(testTag),
    color = Color.White,
    shape = RoundedCornerShape(18.dp),
    shadowElevation = 1.dp
  ) {
    Column(
      modifier = Modifier.padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(iconBg),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "$currency ${formatAmount(currentAmount)} / ${formatAmount(targetAmount)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B)
          )
        }

        Text(
          text = "$percentage%",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Progress bar
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(CircleShape)
          .background(Color(0xFFF1F5F9))
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth(animatedProgress)
            .height(6.dp)
            .clip(CircleShape)
            .background(progressColor)
        )
      }
    }
  }
}

/**
 * Screen 11: Goal Progress Detail Screen matching user mockup
 * - Circular progress ring: 72% KES 180,000 of 250,000
 * - Emergency Fund / Target: 3 months expenses
 * - Translucent green area chart
 * - Monthly Contribution KES 10,000 [Edit]
 */
@Composable
fun GoalProgressDetailScreen(
  goalName: String,
  currency: String,
  onBack: () -> Unit
) {
  var monthlyContrib by remember { mutableIntStateOf(10000) }
  var showEditContribDialog by remember { mutableStateOf(false) }

  val targetPercent = 72
  val currentSaved = 180000
  val totalGoal = 250000

  val animatedPercent = animateProgress(targetPercent / 100f, durationMs = 1000)
  val animatedSaved = animateIntCount(currentSaved, durationMs = 900)

  val scrollState = rememberScrollState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAF9))
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      // Top Navigation
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.White)
            .bouncyClickable { onBack() }
            .testTag("btn_goal_detail_back")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Back",
            tint = Color(0xFF0F172A)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
          text = "Goal Progress",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // CIRCULAR PROGRESS GAUGE HERO
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier.size(190.dp),
          contentAlignment = Alignment.Center
        ) {
          // Circular Progress Arc
          Canvas(modifier = Modifier.size(180.dp)) {
            val strokeWidth = 14.dp.toPx()

            // Background track
            drawCircle(
              color = Color(0xFFE2E8F0),
              style = Stroke(width = strokeWidth)
            )

            // Progress arc
            drawArc(
              brush = Brush.sweepGradient(
                colors = listOf(
                  Color(0xFF10B981),
                  Color(0xFF02844E),
                  Color(0xFF059669)
                )
              ),
              startAngle = -90f,
              sweepAngle = 360f * animatedPercent,
              useCenter = false,
              style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
          }

          // Center Text
          Column(
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "$targetPercent%",
              fontSize = 32.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "$currency ${formatAmount(animatedSaved)}",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF02844E)
            )
            Text(
              text = "of ${formatAmount(totalGoal)}",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = Color(0xFF64748B)
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = goalName,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Target: 3 months expenses",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFF64748B)
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      // GROWTH CURVE CHART CARD
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(2.dp, RoundedCornerShape(20.dp)),
        color = Color.White,
        shape = RoundedCornerShape(20.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Savings Trajectory",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0F172A)
            )
            Text(
              text = "+$currency 10,000/mo",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF02844E)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Green area chart
          Canvas(
            modifier = Modifier
              .fillMaxWidth()
              .height(110.dp)
          ) {
            val w = size.width
            val h = size.height

            val fillPath = Path().apply {
              moveTo(0f, h * 0.85f)
              cubicTo(w * 0.25f, h * 0.80f, w * 0.5f, h * 0.55f, w * 0.75f, h * 0.35f)
              cubicTo(w * 0.85f, h * 0.25f, w * 0.95f, h * 0.15f, w, h * 0.10f)
              lineTo(w, h)
              lineTo(0f, h)
              close()
            }

            drawPath(
              path = fillPath,
              brush = Brush.verticalGradient(
                colors = listOf(
                  Color(0xFF02844E).copy(alpha = 0.35f),
                  Color(0xFF02844E).copy(alpha = 0.05f),
                  Color.Transparent
                )
              )
            )

            val strokePath = Path().apply {
              moveTo(0f, h * 0.85f)
              cubicTo(w * 0.25f, h * 0.80f, w * 0.5f, h * 0.55f, w * 0.75f, h * 0.35f)
              cubicTo(w * 0.85f, h * 0.25f, w * 0.95f, h * 0.15f, w, h * 0.10f)
            }

            drawPath(
              path = strokePath,
              color = Color(0xFF02844E),
              style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // MONTHLY CONTRIBUTION CARD
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 1.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Monthly Contribution",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$currency ${formatAmount(monthlyContrib)}",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0F172A)
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFFE8F8F2))
              .bouncyClickable { showEditContribDialog = true }
              .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Edit",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF02844E)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Button(
        onClick = onBack,
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02844E)),
        shape = RoundedCornerShape(16.dp)
      ) {
        Text("Done", fontWeight = FontWeight.Bold, fontSize = 15.sp)
      }

      Spacer(modifier = Modifier.height(60.dp))
    }
  }

  if (showEditContribDialog) {
    Dialog(onDismissRequest = { showEditContribDialog = false }) {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        modifier = Modifier.padding(16.dp)
      ) {
        var tempContrib by remember { mutableStateOf(monthlyContrib.toString()) }
        Column(modifier = Modifier.padding(20.dp)) {
          Text("Edit Monthly Contribution", fontWeight = FontWeight.Bold, fontSize = 18.sp)
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = tempContrib,
            onValueChange = { tempContrib = it },
            label = { Text("Monthly Contribution ($currency)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
          Spacer(modifier = Modifier.height(16.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            Button(
              onClick = { showEditContribDialog = false },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF64748B))
            ) {
              Text("Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                monthlyContrib = tempContrib.toIntOrNull() ?: monthlyContrib
                showEditContribDialog = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02844E))
            ) {
              Text("Save")
            }
          }
        }
      }
    }
  }
}

@Composable
fun AddGoalDialog(
  currency: String,
  onDismiss: () -> Unit,
  onSave: (String, Int) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var target by remember { mutableStateOf("") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color.White,
      modifier = Modifier.padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(22.dp)) {
        Text("Create Financial Goal", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Goal Name (e.g. New Car)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
          value = target,
          onValueChange = { target = it },
          label = { Text("Target Amount ($currency)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF64748B))
          ) {
            Text("Cancel")
          }
          Spacer(modifier = Modifier.width(10.dp))
          Button(
            onClick = {
              val amt = target.toIntOrNull() ?: 50000
              onSave(title.ifBlank { "New Goal" }, amt)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02844E))
          ) {
            Text("Create Goal")
          }
        }
      }
    }
  }
}
