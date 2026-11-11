package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.animation.TopScrollProgressBar
import com.example.ui.animation.animateIntCount
import com.example.ui.animation.animateProgress
import com.example.ui.animation.bouncyClickable
import com.example.ui.animation.formatAmount
import com.example.ui.animation.staggeredEntrance

/**
 * Numo Home Page matching the left screen of homescreenandanalytics.png
 * enhanced with premium scrolling physics, animated counters, progressive charts,
 * collapsing header, and tactile touch interactions.
 */
@Composable
fun HomeScreen(
  userName: String = "Ayesha Khan",
  currency: String = "KES",
  onNavigateToAnalytics: () -> Unit = {},
  onNavigateToCalendar: () -> Unit = {},
  onNavigateToInsights: () -> Unit = {}
) {
  var isBalanceVisible by remember { mutableStateOf(true) }
  val scrollState = rememberScrollState()

  // Dynamic animated financial counters
  val animatedTotalBudget = animateIntCount(targetValue = 120000, durationMillis = 850)
  val animatedSpent = animateIntCount(targetValue = 78420, durationMillis = 850)
  val animatedRemaining = animateIntCount(targetValue = 41580, durationMillis = 850)

  // Animated progress bar
  val animatedProgressFraction = animateProgress(targetProgress = 0.6535f, delayMillis = 180, durationMillis = 950)

  // Animated Donut Chart sweep progress (0f -> 1f)
  val donutChartProgress = animateProgress(targetProgress = 1f, delayMillis = 250, durationMillis = 1000)

  // Scroll ratio for top progress indicator
  val scrollProgress by remember {
    derivedStateOf {
      if (scrollState.maxValue > 0) scrollState.value.toFloat() / scrollState.maxValue.toFloat() else 0f
    }
  }

  // Header collapse detection
  val isHeaderCollapsed by remember {
    derivedStateOf { scrollState.value > 140 }
  }

  val headerAlpha by animateFloatAsState(
    targetValue = if (isHeaderCollapsed) 0f else 1f,
    animationSpec = tween(durationMillis = 200),
    label = "headerAlpha"
  )

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = Color(0xFFF8FAF9)
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Main Scrollable Body
      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .verticalScroll(scrollState)
          .padding(horizontal = 20.dp)
      ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Expanded Header (Greeting + Name + Bell + Avatar) with smooth alpha fade
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
              alpha = headerAlpha
              translationY = -scrollState.value * 0.15f // subtle parallax
            },
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = "Good Morning,",
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "$userName 👋",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF0F172A)
              )
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            // Notification Bell with bouncy feedback
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                .bouncyClickable { /* Notifications */ },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = Color(0xFF0F172A),
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // User Avatar Image with bouncy interaction
            Image(
              painter = painterResource(id = R.drawable.img_user_avatar),
              contentDescription = "User Avatar",
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .border(1.5.dp, Color(0xFF00C884).copy(alpha = 0.5f), CircleShape)
                .bouncyClickable { /* Profile */ },
              contentScale = ContentScale.Crop
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Monthly Budget Hero Card (Staggered Entrance 0)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = 0, baseDelayMs = 20)
            .shadow(
              elevation = 10.dp,
              shape = RoundedCornerShape(24.dp),
              ambientColor = Color(0x33015B35),
              spotColor = Color(0x4D015B35)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
              brush = Brush.verticalGradient(
                colors = listOf(
                  Color(0xFF005832),
                  Color(0xFF017443),
                  Color(0xFF028D52)
                )
              )
            )
            .padding(20.dp)
            .testTag("monthly_budget_card")
        ) {
          Column {
            // Top Row: "Monthly Budget" and "Edit" button
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Monthly Budget",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFD1FAE5)
              )

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color.White.copy(alpha = 0.22f))
                  .bouncyClickable { /* Edit Budget */ }
                  .padding(horizontal = 14.dp, vertical = 5.dp)
              ) {
                Text(
                  text = "Edit",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color.White
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Budget Amount + Eye Toggle with animated number
            Row(
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isBalanceVisible) "$currency ${formatAmount(animatedTotalBudget)}" else "$currency ••••••",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color.White
              )
              Spacer(modifier = Modifier.width(10.dp))
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .bouncyClickable { isBalanceVisible = !isBalanceVisible },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isBalanceVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                  contentDescription = "Toggle Balance",
                  tint = Color.White.copy(alpha = 0.85f),
                  modifier = Modifier.size(20.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Smooth Animated Progress Bar
            LinearProgressIndicator(
              progress = { animatedProgressFraction },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
              color = Color.White,
              trackColor = Color.White.copy(alpha = 0.25f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Spent & Remaining with counting animation
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "$currency ${formatAmount(animatedSpent)} spent",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
              )
              Text(
                text = "$currency ${formatAmount(animatedRemaining)} remaining",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 3. Quick Action Buttons (Staggered Entrance 1)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = 1, baseDelayMs = 60),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          QuickActionButton(
            icon = Icons.AutoMirrored.Outlined.Send,
            label = "Send",
            isRotated = true,
            onClick = {}
          )
          QuickActionButton(
            icon = Icons.Outlined.FileDownload,
            label = "Receive",
            onClick = {}
          )
          QuickActionButton(
            icon = Icons.Outlined.Description,
            label = "Bills",
            onClick = onNavigateToCalendar
          )
          QuickActionButton(
            icon = Icons.Default.MoreHoriz,
            label = "More",
            onClick = onNavigateToAnalytics
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Spending Overview Section (Staggered Entrance 2)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = 2, baseDelayMs = 100),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Spending Overview",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          )
          Text(
            text = "See All →",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF02844E),
            modifier = Modifier.bouncyClickable { onNavigateToInsights() }
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Donut Chart & Category Breakdown Card with Sweep Animation
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = 2, baseDelayMs = 120)
            .shadow(
              elevation = 3.dp,
              shape = RoundedCornerShape(22.dp),
              ambientColor = Color(0x0A000000),
              spotColor = Color(0x14000000)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(22.dp))
            .padding(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            // Animated Donut Chart Canvas
            Box(
              modifier = Modifier.size(116.dp),
              contentAlignment = Alignment.Center
            ) {
              Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                val arcRadius = (size.minDimension - strokeWidth) / 2
                val arcTopLeft = androidx.compose.ui.geometry.Offset(
                  (size.width - 2 * arcRadius) / 2,
                  (size.height - 2 * arcRadius) / 2
                )
                val arcSize = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2)

                val p = donutChartProgress

                // Slice 1: Food & Dining (32% = 115.2°) Blue
                drawArc(
                  color = Color(0xFF0084FF),
                  startAngle = -90f,
                  sweepAngle = 115.2f * p,
                  useCenter = false,
                  topLeft = arcTopLeft,
                  size = arcSize,
                  style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                // Slice 2: Transport (18% = 64.8°) Green
                drawArc(
                  color = Color(0xFF10B981),
                  startAngle = -90f + 115.2f * p,
                  sweepAngle = 64.8f * p,
                  useCenter = false,
                  topLeft = arcTopLeft,
                  size = arcSize,
                  style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                // Slice 3: Shopping (16% = 57.6°) Orange
                drawArc(
                  color = Color(0xFFF59E0B),
                  startAngle = -90f + (115.2f + 64.8f) * p,
                  sweepAngle = 57.6f * p,
                  useCenter = false,
                  topLeft = arcTopLeft,
                  size = arcSize,
                  style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                // Slice 4: Health & Fitness (12% = 43.2°) Purple
                drawArc(
                  color = Color(0xFFA855F7),
                  startAngle = -90f + (115.2f + 64.8f + 57.6f) * p,
                  sweepAngle = 43.2f * p,
                  useCenter = false,
                  topLeft = arcTopLeft,
                  size = arcSize,
                  style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                // Slice 5: Others (22% = 79.2°) Teal
                drawArc(
                  color = Color(0xFF14B8A6),
                  startAngle = -90f + (115.2f + 64.8f + 57.6f + 43.2f) * p,
                  sweepAngle = 79.2f * p,
                  useCenter = false,
                  topLeft = arcTopLeft,
                  size = arcSize,
                  style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
              }

              // Center Counting Text
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = currency,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium,
                  color = Color(0xFF64748B)
                )
                Text(
                  text = formatAmount((78420 * donutChartProgress).toInt()),
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF0F172A)
                )
                Text(
                  text = "Spent",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium,
                  color = Color(0xFF64748B)
                )
              }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Legend Column
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              DonutLegendItem(color = Color(0xFF0084FF), label = "Food & Dining", percentage = "32%")
              DonutLegendItem(color = Color(0xFF10B981), label = "Transport", percentage = "18%")
              DonutLegendItem(color = Color(0xFFF59E0B), label = "Shopping", percentage = "16%")
              DonutLegendItem(color = Color(0xFFA855F7), label = "Health & Fitness", percentage = "12%")
              DonutLegendItem(color = Color(0xFF14B8A6), label = "Others", percentage = "22%")
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Money Tip Card (Staggered Entrance 3)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = 3, baseDelayMs = 140)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFE8FBF4))
            .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(18.dp))
            .bouncyClickable { /* Tip info */ }
            .padding(horizontal = 16.dp, vertical = 13.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.7f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = "Money Tip",
                tint = Color(0xFF02844E),
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Money Tip",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
              )
              Text(
                text = "You're on track to meet your monthly budget!",
                fontSize = 11.sp,
                color = Color(0xFF475569)
              )
            }

            Icon(
              imageVector = Icons.Outlined.KeyboardArrowRight,
              contentDescription = null,
              tint = Color(0xFF02844E),
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 6. Upcoming Bills Section (Staggered Entrance 4)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = 4, baseDelayMs = 160),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Upcoming Bills",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          )
          Text(
            text = "See All →",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF02844E),
            modifier = Modifier.bouncyClickable { onNavigateToCalendar() }
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bill Item Card with interactive feedback
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = 4, baseDelayMs = 180)
            .shadow(
              elevation = 2.dp,
              shape = RoundedCornerShape(20.dp),
              ambientColor = Color(0x0A000000)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(20.dp))
            .bouncyClickable { onNavigateToCalendar() }
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFE8FBF4)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Bolt,
                  contentDescription = "Electricity",
                  tint = Color(0xFF02844E),
                  modifier = Modifier.size(24.dp)
                )
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column {
                Text(
                  text = "KPLC Electricity",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "$currency 2,500",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                  text = "Due in 3 days",
                  fontSize = 11.sp,
                  color = Color(0xFF64748B)
                )
              }
            }

            // Pay Button with spring interaction
            Button(
              onClick = onNavigateToCalendar,
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF015B35)
              ),
              shape = RoundedCornerShape(20.dp),
              modifier = Modifier
                .height(38.dp)
                .bouncyClickable { onNavigateToCalendar() }
                .testTag("pay_bill_button")
            ) {
              Text(
                text = "Pay",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 7. Financial Goals Carousel (Section 10 & 13)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = 5, baseDelayMs = 200),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Financial Goals",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          )
          Text(
            text = "See All →",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF02844E),
            modifier = Modifier.bouncyClickable { /* Goals */ }
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Carousel with snapping card feel
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = 5, baseDelayMs = 220)
        ) {
          item {
            GoalCarouselCard(
              title = "Emergency Fund",
              saved = "$currency 320,000",
              target = "$currency 500,000",
              progress = 0.64f,
              color = Color(0xFF02844E)
            )
          }
          item {
            GoalCarouselCard(
              title = "Land Investment",
              saved = "$currency 450,000",
              target = "$currency 1,200,000",
              progress = 0.375f,
              color = Color(0xFF0084FF)
            )
          }
          item {
            GoalCarouselCard(
              title = "Car Upgrade",
              saved = "$currency 180,000",
              target = "$currency 600,000",
              progress = 0.30f,
              color = Color(0xFFF59E0B)
            )
          }
        }

        Spacer(modifier = Modifier.height(30.dp))
      }

      // Compact Sticky Collapsing Header (Slides down smoothly when scrolled)
      AnimatedVisibility(
        visible = isHeaderCollapsed,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier.align(Alignment.TopCenter)
      ) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, ambientColor = Color(0x14000000)),
          color = Color.White
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .statusBarsPadding()
              .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Image(
                painter = painterResource(id = R.drawable.img_user_avatar),
                contentDescription = "User Avatar",
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .border(1.dp, Color(0xFF00C884), CircleShape),
                contentScale = ContentScale.Crop
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Numo",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF02844E),
                  letterSpacing = 0.5.sp
                )
                Text(
                  text = userName,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF0F172A)
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFFE8FBF4))
                  .bouncyClickable { onNavigateToAnalytics() }
                  .padding(horizontal = 10.dp, vertical = 5.dp)
              ) {
                Text(
                  text = "$currency ${formatAmount(animatedSpent)}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF02844E)
                )
              }

              Spacer(modifier = Modifier.width(8.dp))

              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFF1F5F9))
                  .bouncyClickable { /* Notifications */ },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Outlined.Notifications,
                  contentDescription = "Notifications",
                  tint = Color(0xFF0F172A),
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }

      // Top 2dp Scroll Progress Indicator
      TopScrollProgressBar(
        scrollProgress = scrollProgress,
        modifier = Modifier.align(Alignment.TopCenter)
      )
    }
  }
}

@Composable
fun QuickActionButton(
  icon: ImageVector,
  label: String,
  isRotated: Boolean = false,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.bouncyClickable { onClick() }
  ) {
    Box(
      modifier = Modifier
        .size(58.dp)
        .clip(RoundedCornerShape(18.dp))
        .background(Color(0xFFE8F8F2))
        .border(1.dp, Color(0xFFD1F2E5), RoundedCornerShape(18.dp)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = Color(0xFF02844E),
        modifier = Modifier
          .size(24.dp)
          .then(if (isRotated) Modifier.rotate(-30f) else Modifier)
      )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = label,
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      color = Color(0xFF1E293B)
    )
  }
}

@Composable
fun DonutLegendItem(
  color: Color,
  label: String,
  percentage: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(color)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF475569)
      )
    }

    Text(
      text = percentage,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = Color(0xFF0F172A)
    )
  }
}

@Composable
fun GoalCarouselCard(
  title: String,
  saved: String,
  target: String,
  progress: Float,
  color: Color
) {
  val animatedGoalProgress = animateProgress(targetProgress = progress, delayMillis = 200, durationMillis = 800)

  Box(
    modifier = Modifier
      .width(220.dp)
      .shadow(2.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x0A000000))
      .clip(RoundedCornerShape(18.dp))
      .background(Color.White)
      .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(18.dp))
      .bouncyClickable { /* Open Goal */ }
      .padding(14.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )
        Icon(
          imageVector = Icons.Outlined.Flag,
          contentDescription = null,
          tint = color,
          modifier = Modifier.size(16.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = saved,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0F172A)
      )
      Text(
        text = "of $target",
        fontSize = 11.sp,
        color = Color(0xFF64748B)
      )

      Spacer(modifier = Modifier.height(10.dp))

      LinearProgressIndicator(
        progress = { animatedGoalProgress },
        modifier = Modifier
          .fillMaxWidth()
          .height(5.dp)
          .clip(CircleShape),
        color = color,
        trackColor = Color(0xFFF1F5F9)
      )
    }
  }
}
