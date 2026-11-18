package com.example.ui.voice

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoFloatingDecorations
import com.example.ui.components.NumoScreenBackground
import com.example.voice.VoiceOrbState

/**
 * SCREEN 1 — Hey Numo Voice Listening Interface (left phone in reference).
 */
@Composable
fun HeyNumoListeningScreen(
  orbState: VoiceOrbState,
  rmsLevel: Float,
  partialTranscript: String,
  statusError: String?,
  listeningHint: String? = null,
  onMicClick: () -> Unit,
  onOpenHome: () -> Unit,
  onSwitchToKeyboard: () -> Unit,
  onOpenSettings: () -> Unit,
  onBack: () -> Unit
) {
  val headline = when (orbState) {
    VoiceOrbState.LISTENING -> "Listening..."
    VoiceOrbState.THINKING -> "Thinking..."
    VoiceOrbState.SPEAKING -> "Speaking..."
    VoiceOrbState.ERROR -> "Didn't Catch That"
    VoiceOrbState.IDLE -> "Hey Numo"
  }

  val subtitle = when {
    orbState == VoiceOrbState.ERROR && !statusError.isNullOrBlank() -> statusError
    orbState == VoiceOrbState.LISTENING && partialTranscript.isNotBlank() -> "\"$partialTranscript\""
    orbState == VoiceOrbState.LISTENING && !listeningHint.isNullOrBlank() -> listeningHint
    orbState == VoiceOrbState.LISTENING -> "Speak now, I'm all ears!"
    orbState == VoiceOrbState.THINKING -> "Checking your Numo accounts..."
    orbState == VoiceOrbState.SPEAKING -> "Tap mic to interrupt or ask another"
    else -> "Go ahead, I'm listening... Tap mic to speak"
  }

  NumoScreenBackground {
    NumoFloatingDecorations(variant = 0)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top bar: Back, "Hey Numo" status pill, Menu
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        HeyNumoCircleButton(
          icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
          contentDescription = "Back",
          onClick = onBack
        )

        HeyNumoStatusPill(isActive = orbState != VoiceOrbState.ERROR)

        HeyNumoCircleButton(
          icon = Icons.Default.MoreHoriz,
          contentDescription = "Voice Settings",
          onClick = onOpenSettings
        )
      }

      // Center Orb + Status + Live Waveform
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 12.dp)
      ) {
        HeyNumoOrb(
          orbState = orbState,
          rmsLevel = rmsLevel,
          size = 224.dp,
          logoSize = 90.dp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = headline,
          fontSize = 24.sp,
          fontWeight = FontWeight.ExtraBold,
          color = NumoDS.TextPrimary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = subtitle,
          fontSize = 14.5.sp,
          lineHeight = 20.sp,
          fontWeight = FontWeight.Medium,
          color = if (orbState == VoiceOrbState.ERROR) NumoDS.Magenta else NumoDS.TextSecondary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        HeyNumoWaveform(
          orbState = orbState,
          rmsLevel = rmsLevel
        )
      }

      // Bottom Controls: Home | Large Glowing Mic | Keyboard
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 28.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        HeyNumoCircleButton(
          icon = Icons.Default.Home,
          contentDescription = "Hey Numo Home",
          onClick = onOpenHome,
          size = 54.dp,
          iconSize = 24.dp
        )

        // Center glowing teal claymorphic microphone button
        Box(
          modifier = Modifier
            .size(76.dp)
            .shadow(16.dp, CircleShape, spotColor = NumoDS.Turquoise.copy(alpha = 0.65f))
            .clip(CircleShape)
            .background(
              Brush.verticalGradient(
                colors = if (orbState == VoiceOrbState.LISTENING) {
                  listOf(Color(0xFF56F6EC), NumoDS.Turquoise, NumoDS.HeroTealDark)
                } else {
                  listOf(NumoDS.BtnTealTop, NumoDS.BtnTealMid, NumoDS.BtnTealBottom)
                }
              )
            )
            .border(2.dp, Color.White.copy(alpha = 0.55f), CircleShape)
            .clickable(onClick = onMicClick),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (orbState == VoiceOrbState.SPEAKING) Icons.Default.Stop else Icons.Default.Mic,
            contentDescription = "Microphone",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
          )
        }

        HeyNumoCircleButton(
          icon = Icons.Default.Keyboard,
          contentDescription = "Switch to Keyboard Chat",
          onClick = onSwitchToKeyboard,
          size = 54.dp,
          iconSize = 24.dp
        )
      }
    }
  }
}
