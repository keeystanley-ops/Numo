package com.example.ui.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NumoDS

/**
 * Shared top navigation bar for Profile sub-screens.
 * Supports either arrow-only ("←") or "< Back" style to match the reference.
 */
@Composable
fun ProfileSubScreenTopBar(
  title: String,
  onBack: () -> Unit,
  showBackLabel: Boolean = false
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      modifier = Modifier
        .align(Alignment.CenterStart)
        .clip(RoundedCornerShape(12.dp))
        .clickable(onClick = onBack)
        .padding(horizontal = 4.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = if (showBackLabel) {
          Icons.AutoMirrored.Filled.KeyboardArrowLeft
        } else {
          Icons.AutoMirrored.Filled.ArrowBack
        },
        contentDescription = "Back",
        tint = NumoDS.TextPrimary,
        modifier = Modifier.size(22.dp)
      )
      if (showBackLabel) {
        Spacer(modifier = Modifier.width(2.dp))
        Text(
          text = "Back",
          fontSize = 15.sp,
          fontWeight = FontWeight.Medium,
          color = NumoDS.TextPrimary
        )
      }
    }

    Text(
      text = title,
      fontSize = 19.sp,
      fontWeight = FontWeight.Bold,
      color = NumoDS.TextPrimary,
      textAlign = TextAlign.Center
    )
  }
}

/**
 * Custom Claymorphic Pill Toggle matching Notifications & Security screens.
 */
@Composable
fun NumoPillToggle(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  activeColorTop: Color = NumoDS.CyanBright,
  activeColorBottom: Color = NumoDS.HeroTealMid
) {
  val trackColorTop by animateColorAsState(
    targetValue = if (checked) activeColorTop else Color(0xFFDAD1C8),
    label = "toggle_top"
  )
  val trackColorBottom by animateColorAsState(
    targetValue = if (checked) activeColorBottom else Color(0xFFC4B8AC),
    label = "toggle_bottom"
  )
  val thumbOffset by animateDpAsState(
    targetValue = if (checked) 22.dp else 2.dp,
    label = "toggle_thumb"
  )

  Box(
    modifier = Modifier
      .width(48.dp)
      .height(27.dp)
      .clip(CircleShape)
      .background(
        Brush.verticalGradient(colors = listOf(trackColorTop, trackColorBottom))
      )
      .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
      ) { onCheckedChange(!checked) },
    contentAlignment = Alignment.CenterStart
  ) {
    Box(
      modifier = Modifier
        .offset(x = thumbOffset)
        .size(22.dp)
        .shadow(4.dp, CircleShape)
        .clip(CircleShape)
        .background(Color.White)
    )
  }
}

/**
 * Shared recessed warm input field for Personal Information.
 */
@Composable
fun ProfileRecessedInput(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  keyboardType: KeyboardType = KeyboardType.Text
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = label,
      fontSize = 12.5.sp,
      fontWeight = FontWeight.SemiBold,
      color = NumoDS.TextPrimary,
      modifier = Modifier.padding(start = 4.dp, bottom = 5.dp)
    )
    val shape = RoundedCornerShape(16.dp)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .clip(shape)
        .background(
          Brush.verticalGradient(
            colors = listOf(Color(0xFFEDE0D2), Color(0xFFF6ECDF))
          )
        )
        .border(1.dp, Color(0xFFD9C7B5), shape)
        .padding(horizontal = 16.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      if (value.isEmpty()) {
        Text(
          text = placeholder,
          fontSize = 14.sp,
          color = NumoDS.TextMuted
        )
      }
      BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = NumoDS.TextPrimary
        ),
        cursorBrush = SolidColor(NumoDS.Turquoise),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}
