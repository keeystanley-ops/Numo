package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Exact Color Tokens sampled from the Numo Design System & 12 Reference Screens
 */
object NumoDS {
  // Warm Peach/Cream Canvas & Neumorphic Surfaces
  val PeachBgTop = Color(0xFFFEF0E7)
  val PeachBgMid = Color(0xFFFCE8DA)
  val PeachBgBottom = Color(0xFFFBE2D1)
  val CardSurface = Color(0xFFFBEEE6)
  val CardSurfaceLight = Color(0xFFFDF5EF)
  val CardBorderLight = Color(0xE6FFFFFF)
  val CardShadowWarm = Color(0x2ED9B69C)
  val BottomNavBg = Color(0xFFFFF1E6)

  // Core Brand & Color Palette Swatches
  val Cyan = Color(0xFF00E5FF)
  val CyanBright = Color(0xFF2CE6D8)
  val Turquoise = Color(0xFF14B8A6)
  val Magenta = Color(0xFFFF2D9B)
  val MagentaDeep = Color(0xFF970E42)
  val MagentaMid = Color(0xFFC2185B)
  val Black = Color(0xFF0B0B0B)
  val DuneOchre = Color(0xFFF58800)
  val DeepUmber = Color(0xFF5B3A29)

  // Hero Teal Claymorphism Gradient
  val HeroTealTopLeft = Color(0xFF1BE3D8)
  val HeroTealMid = Color(0xFF0A6F62)
  val HeroTealDark = Color(0xFF033B31)

  // Button Teal Claymorphism Gradient
  val BtnTealTop = Color(0xFF0E8F73)
  val BtnTealMid = Color(0xFF065E4B)
  val BtnTealBottom = Color(0xFF033A2E)

  // Mint Glass Card (Upcoming Bills Hero & Smart Tip)
  val MintGlassLight = Color(0xFFC9F2EA)
  val MintGlassSoft = Color(0xFFE1F6F1)

  // Semantic & Category Colors
  val TextPrimary = Color(0xFF111827)
  val TextSecondary = Color(0xFF5C4D44)
  val TextMuted = Color(0xFF7D6E65)

  val SuccessGreen = Color(0xFF16A34A)
  val ExpenseRed = Color(0xFFC81E3A)
  val TransportBlue = Color(0xFF0369A1)
  val TransportBlueLight = Color(0xFF0EA5E9)
  val PurpleAccent = Color(0xFF7E22CE)
  val OrangeBright = Color(0xFFF97316)

  val TrackBg = Color(0xFFF3DFCE)
}

@Composable
fun NumoScreenBackground(
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            NumoDS.PeachBgTop,
            NumoDS.PeachBgMid,
            NumoDS.PeachBgBottom
          )
        )
      ),
    content = content
  )
}

@Composable
fun NumoNeumorphicCard(
  modifier: Modifier = Modifier,
  cornerRadius: Dp = 20.dp,
  backgroundColor: Color = NumoDS.CardSurface,
  onClick: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val shape = RoundedCornerShape(cornerRadius)
  val baseMod = modifier
    .shadow(
      elevation = 10.dp,
      shape = shape,
      ambientColor = NumoDS.CardShadowWarm,
      spotColor = NumoDS.CardShadowWarm
    )
    .clip(shape)
    .background(
      brush = Brush.linearGradient(
        colors = listOf(
          NumoDS.CardSurfaceLight,
          backgroundColor
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
      )
    )
    .border(1.2.dp, NumoDS.CardBorderLight, shape)

  val finalMod = if (onClick != null) {
    baseMod.clickable(
      interactionSource = remember { MutableInteractionSource() },
      indication = null,
      onClick = onClick
    )
  } else {
    baseMod
  }

  Box(modifier = finalMod, content = content)
}

@Composable
fun NumoClayHeroCard(
  modifier: Modifier = Modifier,
  cornerRadius: Dp = 22.dp,
  content: @Composable BoxScope.() -> Unit
) {
  val shape = RoundedCornerShape(cornerRadius)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .shadow(
        elevation = 14.dp,
        shape = shape,
        ambientColor = Color(0x40033B31),
        spotColor = Color(0x55033B31)
      )
      .clip(shape)
      .background(
        brush = Brush.linearGradient(
          colors = listOf(
            NumoDS.HeroTealTopLeft,
            NumoDS.HeroTealMid,
            NumoDS.HeroTealDark
          ),
          start = Offset(0f, 0f),
          end = Offset(900f, 500f)
        )
      )
      .border(
        width = 1.2.dp,
        brush = Brush.linearGradient(
          colors = listOf(
            Color.White.copy(alpha = 0.65f),
            Color.White.copy(alpha = 0.12f)
          )
        ),
        shape = shape
      ),
    content = content
  )
}

@Composable
fun NumoClayIconBadge(
  icon: ImageVector,
  topColor: Color,
  bottomColor: Color,
  modifier: Modifier = Modifier,
  size: Dp = 40.dp,
  iconSize: Dp = 20.dp,
  iconTint: Color = Color.White
) {
  Box(
    modifier = modifier
      .size(size)
      .shadow(
        elevation = 6.dp,
        shape = CircleShape,
        ambientColor = bottomColor.copy(alpha = 0.45f),
        spotColor = bottomColor.copy(alpha = 0.55f)
      )
      .clip(CircleShape)
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(topColor, bottomColor)
        )
      )
      .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = iconTint,
      modifier = Modifier.size(iconSize)
    )
  }
}

@Composable
fun NumoClayProgressBar(
  progress: Float,
  fillTopColor: Color = NumoDS.CyanBright,
  fillBottomColor: Color = NumoDS.Turquoise,
  trackColor: Color = NumoDS.TrackBg,
  height: Dp = 8.dp,
  modifier: Modifier = Modifier
) {
  val clamped = progress.coerceIn(0f, 1f)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(height)
      .clip(CircleShape)
      .background(trackColor)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(clamped)
        .fillMaxHeight()
        .shadow(
          elevation = 3.dp,
          shape = CircleShape,
          spotColor = fillBottomColor.copy(alpha = 0.45f)
        )
        .clip(CircleShape)
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(fillTopColor, fillBottomColor)
          )
        )
    )
  }
}

@Composable
fun NumoPrimaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  height: Dp = 48.dp
) {
  Box(
    modifier = modifier
      .height(height)
      .shadow(
        elevation = 10.dp,
        shape = RoundedCornerShape(50),
        ambientColor = Color(0x40033A2E),
        spotColor = Color(0x55033A2E)
      )
      .clip(RoundedCornerShape(50))
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            NumoDS.BtnTealTop,
            NumoDS.BtnTealMid,
            NumoDS.BtnTealBottom
          )
        )
      )
      .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(50))
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      fontSize = 15.sp,
      fontWeight = FontWeight.SemiBold,
      fontFamily = FontFamily.SansSerif,
      color = Color.White
    )
  }
}

@Composable
fun NumoSecondaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  height: Dp = 44.dp
) {
  NumoNeumorphicCard(
    modifier = modifier.height(height),
    cornerRadius = 50.dp,
    onClick = onClick
  ) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.SansSerif,
        color = NumoDS.TextPrimary
      )
    }
  }
}

@Composable
fun NumoSegmentedControl(
  options: List<String>,
  selectedIndex: Int,
  onSelect: (Int) -> Unit,
  activeTopColor: Color = NumoDS.BtnTealTop,
  activeBottomColor: Color = NumoDS.BtnTealBottom,
  modifier: Modifier = Modifier
) {
  NumoNeumorphicCard(
    modifier = modifier
      .fillMaxWidth()
      .height(42.dp),
    cornerRadius = 50.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(3.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      options.forEachIndexed { index, title ->
        val isSelected = index == selectedIndex
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .then(
              if (isSelected) {
                Modifier
                  .shadow(4.dp, RoundedCornerShape(50), spotColor = activeBottomColor.copy(alpha = 0.4f))
                  .clip(RoundedCornerShape(50))
                  .background(
                    brush = Brush.verticalGradient(
                      colors = listOf(activeTopColor, activeBottomColor)
                    )
                  )
              } else {
                Modifier.clip(RoundedCornerShape(50))
              }
            )
            .clickable { onSelect(index) },
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) Color.White else NumoDS.TextPrimary
          )
        }
      }
    }
  }
}

@Composable
fun NumoToggleSwitch(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  activeTopColor: Color = NumoDS.BtnTealTop,
  activeBottomColor: Color = NumoDS.BtnTealBottom,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .width(50.dp)
      .height(28.dp)
      .shadow(3.dp, RoundedCornerShape(50), spotColor = activeBottomColor.copy(alpha = 0.35f))
      .clip(RoundedCornerShape(50))
      .background(
        brush = Brush.verticalGradient(
          colors = if (checked) {
            listOf(activeTopColor, activeBottomColor)
          } else {
            listOf(Color(0xFFD6C7BA), Color(0xFFC2B2A4))
          }
        )
      )
      .clickable { onCheckedChange(!checked) }
      .padding(3.dp)
  ) {
    Box(
      modifier = Modifier
        .size(22.dp)
        .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
        .shadow(2.dp, CircleShape)
        .clip(CircleShape)
        .background(Color.White)
    )
  }
}

@Composable
fun NumoBlackPlusButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .size(34.dp)
      .shadow(5.dp, CircleShape, spotColor = Color(0x55000000))
      .clip(CircleShape)
      .background(NumoDS.Black)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "+",
      color = Color.White,
      fontSize = 20.sp,
      fontWeight = FontWeight.Medium,
      modifier = Modifier.offset(y = (-1).dp)
    )
  }
}

enum class NumoNavTab(val label: String) {
  HOME("Home"),
  ANALYTICS("Analytics"),
  BUDGET("Budget"),
  GOALS("Goals"),
  PROFILE("Profile")
}

@Composable
fun NumoBottomNavBar(
  selectedTab: NumoNavTab,
  onSelectTab: (NumoNavTab) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .shadow(
        elevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ambientColor = Color(0x285B3A29),
        spotColor = Color(0x305B3A29)
      )
      .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
      .background(NumoDS.BottomNavBg)
      .border(
        width = 1.dp,
        color = Color.White.copy(alpha = 0.8f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
      )
      .navigationBarsPadding()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      NumoNavTab.entries.forEach { tab ->
        val isSelected = tab == selectedTab
        val icon = when (tab) {
          NumoNavTab.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
          NumoNavTab.ANALYTICS -> if (isSelected) Icons.Filled.BarChart else Icons.Outlined.BarChart
          NumoNavTab.BUDGET -> if (isSelected) Icons.Filled.CalendarToday else Icons.Outlined.CalendarToday
          NumoNavTab.GOALS -> if (isSelected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
          NumoNavTab.PROFILE -> if (isSelected) Icons.Filled.Person else Icons.Outlined.Person
        }

        Column(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null
            ) { onSelectTab(tab) }
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = tab.label,
            tint = if (isSelected) NumoDS.BtnTealMid else NumoDS.TextPrimary,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = tab.label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) NumoDS.BtnTealMid else NumoDS.TextPrimary
          )
        }
      }
    }
  }
}
