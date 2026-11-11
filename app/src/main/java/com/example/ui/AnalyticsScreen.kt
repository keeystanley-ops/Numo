package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.outlined.CalendarMonth
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.animation.TopScrollProgressBar
import com.example.ui.animation.animateIntCount
import com.example.ui.animation.animateProgress
import com.example.ui.animation.bouncyClickable
import com.example.ui.animation.formatAmount
import com.example.ui.animation.staggeredEntrance

/**
 * Numo Analytics Screen matching the right screen of homescreenandanalytics.png
 * enhanced with progressive chart reveals, animated category bars, counting numbers,
 * smooth period switching, and tactile touch interactions.
 */
@Composable
fun AnalyticsScreen(
  currency: String = "KES",
  onBack: () -> Unit = {}
) {
  var selectedPeriod by remember { mutableStateOf("Month") }
  val scrollState = rememberScrollState()

  // Target spending amount depends on period
  val targetSpending = when (selectedPeriod) {
    "Week" -> 18250
    "Year" -> 840500
    else -> 78420
  }

  val animatedTotalSpending = animateIntCount(targetValue = targetSpending, durationMillis = 800)

  // Animated chart growth factor (0f -> 1f)
  val chartProgress = animateProgress(targetProgress = 1f, delayMillis = 150, durationMillis = 900)

  // Scroll ratio for top progress indicator
  val scrollProgress by remember {
    derivedStateOf {
      if (scrollState.maxValue > 0) scrollState.value.toFloat() / scrollState.maxValue.toFloat() else 0f
    }
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = Color(0xFFF8FAF9)
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .verticalScroll(scrollState)
          .padding(horizontal = 20.dp)
      ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Top Bar (Back Arrow, Title, Calendar Icon)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .bouncyClickable { onBack() }
              .testTag("analytics_back_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color(0xFF0F172A),
              modifier = Modifier.size(24.dp)
            )
          }

          Text(
            text = "Analytics",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = Color(0xFF0F172A)
          )

          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .bouncyClickable { /* Select Date Range */ },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.CalendarMonth,
              contentDescription = "Calendar",
              tint = Color(0xFF0F172A),
              modifier = Modifier.size(22.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Time Period Segmented Selector (Week / Month / Year) with spring feedback
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = 0, baseDelayMs = 20),
          contentAlignment = Alignment.Center
        ) {
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(24.dp))
              .background(Color(0xFFF1F5F9))
              .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            TimePeriodTab(
              title = "Week",
              isSelected = (selectedPeriod == "Week"),
              onClick = { selectedPeriod = "Week" }
            )
            TimePeriodTab(
              title = "Month",
              isSelected = (selectedPeriod == "Month"),
              onClick = { selectedPeriod = "Month" }
            )
            TimePeriodTab(
              title = "Year",
              isSelected = (selectedPeriod == "Year"),
              onClick = { selectedPeriod = "Year" }
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Total Spending Hero (Counting Animation)
        Column(
          modifier = Modifier.staggeredEntrance(index = 1, baseDelayMs = 40)
        ) {
          Text(
            text = "Total Spending",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "$currency ${formatAmount(animatedTotalSpending)}.00",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = Color(0xFF0F172A)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "↓ 12.5%",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF02844E)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "vs. last month",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = Color(0xFF02844E)
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Bar & Area Chart with Progressive Render & Animated Tooltip
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .staggeredEntrance(index = 2, baseDelayMs = 80)
        ) {
          // Animated Tooltip Badge above June bar
          val tooltipAlpha by animateFloatAsState(
            targetValue = if (chartProgress > 0.7f) 1f else 0f,
            animationSpec = tween(durationMillis = 300),
            label = "tooltipAlpha"
          )

          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(end = 12.dp)
              .graphicsLayer {
                alpha = tooltipAlpha
                translationY = (1f - tooltipAlpha) * 8f * density
              }
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFF015B35))
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "$currency 98,420",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
              // Small pointer triangle
              Canvas(modifier = Modifier.size(width = 8.dp, height = 5.dp)) {
                val path = Path().apply {
                  moveTo(0f, 0f)
                  lineTo(size.width, 0f)
                  lineTo(size.width / 2, size.height)
                  close()
                }
                drawPath(path, color = Color(0xFF015B35))
              }
            }
          }

          // Main Chart Canvas + Axes
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(top = 28.dp)
          ) {
            // Y-Axis Labels
            Column(
              modifier = Modifier
                .width(36.dp)
                .height(150.dp),
              verticalArrangement = Arrangement.SpaceBetween,
              horizontalAlignment = Alignment.End
            ) {
              Text(text = "2K", fontSize = 11.sp, color = Color(0xFF94A3B8))
              Text(text = "1.5K", fontSize = 11.sp, color = Color(0xFF94A3B8))
              Text(text = "50K", fontSize = 11.sp, color = Color(0xFF94A3B8))
              Text(text = "0", fontSize = 11.sp, color = Color(0xFF94A3B8))
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Chart Drawing Canvas (Smooth Area Background + 6 Rounded Bars)
            Column(modifier = Modifier.weight(1f)) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(150.dp)
              ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                  val barWidth = 14.dp.toPx()
                  val baseBarHeights = when (selectedPeriod) {
                    "Week" -> listOf(0.30f, 0.55f, 0.40f, 0.70f, 0.50f, 0.65f)
                    "Year" -> listOf(0.50f, 0.60f, 0.45f, 0.65f, 0.75f, 0.85f)
                    else -> listOf(0.45f, 0.40f, 0.65f, 0.50f, 0.60f, 0.90f)
                  }
                  val count = baseBarHeights.size
                  val stepX = size.width / count
                  val p = chartProgress

                  // 1. Draw smooth area curve behind bars
                  val areaPath = Path()
                  val startX = stepX * 0.5f
                  val startY = size.height * (1f - (baseBarHeights[0] * 0.85f) * p)
                  areaPath.moveTo(startX, startY)

                  for (i in 1 until count) {
                    val prevX = stepX * (i - 0.5f)
                    val prevY = size.height * (1f - (baseBarHeights[i - 1] * 0.85f) * p)
                    val currX = stepX * (i + 0.5f)
                    val currY = size.height * (1f - (baseBarHeights[i] * 0.85f) * p)
                    val ctrlX = (prevX + currX) / 2
                    areaPath.quadraticTo(ctrlX, prevY, currX, currY)
                  }

                  // Close area path to bottom
                  val lastX = stepX * (count - 0.5f)
                  areaPath.lineTo(lastX, size.height)
                  areaPath.lineTo(startX, size.height)
                  areaPath.close()

                  drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                      colors = listOf(
                        Color(0xFF00C884).copy(alpha = 0.16f * p),
                        Color(0xFF00C884).copy(alpha = 0.04f * p),
                        Color.Transparent
                      )
                    )
                  )

                  // 2. Draw 6 rounded vertical bars with staggered growth
                  for (i in 0 until count) {
                    val centerX = stepX * (i + 0.5f)
                    // Slightly stagger bar reveals
                    val barStagger = ((p - i * 0.05f) / (1f - i * 0.05f)).coerceIn(0f, 1f)
                    val barHeight = size.height * baseBarHeights[i] * barStagger
                    val topY = size.height - barHeight

                    drawRoundRect(
                      color = Color(0xFF02844E),
                      topLeft = Offset(centerX - barWidth / 2, topY),
                      size = Size(barWidth, barHeight),
                      cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              // X-Axis Labels (Jan, Feb, Mar, Apr, May, Jun)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
              ) {
                val months = when (selectedPeriod) {
                  "Week" -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                  "Year" -> listOf("'21", "'22", "'23", "'24", "'25", "'26")
                  else -> listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
                }
                months.forEach { label ->
                  Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Top Spending Categories Section (Staggered Entrance 3)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = 3, baseDelayMs = 120),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Top Spending Categories",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          )
          Text(
            text = "See All →",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF02844E),
            modifier = Modifier.bouncyClickable { /* View all categories */ }
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Categories List with Animated Progress Bars
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = 4, baseDelayMs = 150),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          SpendingCategoryItem(
            icon = Icons.Default.Restaurant,
            iconBg = Color(0xFFFFF2E8),
            iconTint = Color(0xFFF97316),
            title = "Food & Dining",
            percentage = "32%",
            amount = "$currency 24,980",
            targetProgress = 0.32f,
            progressColor = Color(0xFFF97316),
            delayMs = 100
          )

          SpendingCategoryItem(
            icon = Icons.Default.DirectionsCar,
            iconBg = Color(0xFFEBF5FF),
            iconTint = Color(0xFF3B82F6),
            title = "Transport",
            percentage = "18%",
            amount = "$currency 14,120",
            targetProgress = 0.18f,
            progressColor = Color(0xFF3B82F6),
            delayMs = 160
          )

          SpendingCategoryItem(
            icon = Icons.Default.ShoppingBag,
            iconBg = Color(0xFFFEF9C3),
            iconTint = Color(0xFFEAB308),
            title = "Shopping",
            percentage = "16%",
            amount = "$currency 12,460",
            targetProgress = 0.16f,
            progressColor = Color(0xFFEAB308),
            delayMs = 220
          )

          SpendingCategoryItem(
            icon = Icons.Default.Favorite,
            iconBg = Color(0xFFFCE7F3),
            iconTint = Color(0xFFEC4899),
            title = "Health & Fitness",
            percentage = "9%",
            amount = "$currency 7,110",
            targetProgress = 0.09f,
            progressColor = Color(0xFFEC4899),
            delayMs = 280
          )

          SpendingCategoryItem(
            icon = Icons.Default.SportsEsports,
            iconBg = Color(0xFFF3E8FF),
            iconTint = Color(0xFF8B5CF6),
            title = "Entertainment",
            percentage = "8%",
            amount = "$currency 6,260",
            targetProgress = 0.08f,
            progressColor = Color(0xFF8B5CF6),
            delayMs = 340
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 6. Budget Health Card (Staggered Entrance 5)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .staggeredEntrance(index = 5, baseDelayMs = 200)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFE8FBF4))
            .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(18.dp))
            .bouncyClickable { /* Budget details */ }
            .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.TrackChanges,
                contentDescription = "Budget Health",
                tint = Color(0xFF02844E),
                modifier = Modifier.size(22.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = "Budget Health",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "On Track",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF475569)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "On Track",
                tint = Color(0xFF02844E),
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(28.dp))
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
fun TimePeriodTab(
  title: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(20.dp))
      .background(if (isSelected) Color(0xFF02844E) else Color.Transparent)
      .bouncyClickable { onClick() }
      .padding(horizontal = 20.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = title,
      fontSize = 13.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) Color.White else Color(0xFF64748B)
    )
  }
}

@Composable
fun SpendingCategoryItem(
  icon: ImageVector,
  iconBg: Color,
  iconTint: Color,
  title: String,
  percentage: String,
  amount: String,
  targetProgress: Float,
  progressColor: Color,
  delayMs: Int = 100
) {
  val animatedProgress = animateProgress(
    targetProgress = targetProgress,
    delayMillis = delayMs,
    durationMillis = 850
  )

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .bouncyClickable { /* Category Detail */ },
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(42.dp)
        .clip(RoundedCornerShape(12.dp))
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
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = percentage,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = amount,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(CircleShape),
        color = progressColor,
        trackColor = Color(0xFFF1F5F9)
      )
    }
  }
}
