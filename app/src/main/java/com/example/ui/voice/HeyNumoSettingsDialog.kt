package com.example.ui.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.NumoDS
import com.example.voice.HeyNumoSettings
import kotlin.math.roundToInt

/**
 * Voice Assistant Settings Modal matching Numo's cream + turquoise + claymorphic design system.
 */
@Composable
fun HeyNumoSettingsDialog(
  hasMicPermission: Boolean,
  onRequestMicPermission: () -> Unit,
  onWakeWordToggled: (Boolean) -> Unit,
  onPreviewVoice: (String) -> Unit,
  onClearHistory: () -> Unit,
  onDismiss: () -> Unit
) {
  val languages = listOf(
    "en-US" to "English (US)",
    "en-KE" to "English (Kenya)",
    "sw-KE" to "Kiswahili (KE)"
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .shadow(20.dp, RoundedCornerShape(28.dp), ambientColor = NumoDS.CardShadowWarm, spotColor = NumoDS.CardShadowWarm)
        .clip(RoundedCornerShape(28.dp))
        .background(
          Brush.verticalGradient(
            listOf(NumoDS.PeachBgTop, NumoDS.PeachBgBottom)
          )
        )
        .border(1.5.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(28.dp))
        .padding(20.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Hey Numo Settings",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = NumoDS.TextPrimary
          )
          Text(
            text = "Voice activation, speech & privacy",
            fontSize = 12.sp,
            color = NumoDS.TextMuted
          )
        }
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(NumoDS.CardSurfaceLight)
            .border(1.dp, NumoDS.CardBorderLight, CircleShape)
            .clickable { onDismiss() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close Settings",
            tint = NumoDS.TextPrimary,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // Microphone Permission Status Card
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .background(
            if (hasMicPermission) NumoDS.Turquoise.copy(alpha = 0.10f)
            else NumoDS.DuneOchre.copy(alpha = 0.16f)
          )
          .border(
            1.dp,
            if (hasMicPermission) NumoDS.Turquoise.copy(alpha = 0.35f)
            else NumoDS.DuneOchre.copy(alpha = 0.45f),
            RoundedCornerShape(18.dp)
          )
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (hasMicPermission) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
            contentDescription = null,
            tint = if (hasMicPermission) NumoDS.HeroTealMid else NumoDS.DuneOchre,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (hasMicPermission) "Microphone Permission Granted" else "Microphone Permission Required",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )
        }
        Text(
          text = if (hasMicPermission) {
            "Ready for voice commands and foreground \"Hey Numo\" detection."
          } else {
            "Grant microphone access so you can speak to Numo or use \"Hey Numo\" wake detection."
          },
          fontSize = 12.sp,
          color = NumoDS.TextSecondary,
          lineHeight = 16.sp
        )
        if (!hasMicPermission) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(
                Brush.horizontalGradient(listOf(NumoDS.BtnTealTop, NumoDS.BtnTealBottom))
              )
              .clickable { onRequestMicPermission() }
              .padding(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Text(
              text = "Grant Microphone Permission",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }

      // Wake Phrase Toggle
      SettingsToggleRow(
        icon = Icons.Default.Mic,
        iconTint = NumoDS.HeroTealMid,
        title = "\"Hey Numo\" Wake Phrase",
        subtitle = "Listens in foreground while Numo is open (paused in background per Android OS limits)",
        checked = HeyNumoSettings.wakeWordEnabled,
        onCheckedChange = { enabled ->
          if (enabled && !hasMicPermission) {
            onRequestMicPermission()
          }
          HeyNumoSettings.updateWakeWord(enabled)
          onWakeWordToggled(enabled)
        }
      )

      // Voice Response Toggle
      SettingsToggleRow(
        icon = Icons.Default.RecordVoiceOver,
        iconTint = NumoDS.Magenta,
        title = "Voice Response (Text-to-Speech)",
        subtitle = "Read assistant replies aloud automatically",
        checked = HeyNumoSettings.voiceResponseEnabled,
        onCheckedChange = { HeyNumoSettings.updateVoiceResponse(it) }
      )

      // Haptic Feedback Toggle
      SettingsToggleRow(
        icon = Icons.Default.Vibration,
        iconTint = NumoDS.DuneOchre,
        title = "Haptic Feedback",
        subtitle = "Subtle tactile pulse on wake-word & mic tap",
        checked = HeyNumoSettings.hapticFeedbackEnabled,
        onCheckedChange = { HeyNumoSettings.updateHaptic(it) }
      )

      // Speech Speed Slider
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .background(NumoDS.CardSurfaceLight)
          .border(1.dp, NumoDS.CardBorderLight, RoundedCornerShape(18.dp))
          .padding(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Speech Speed",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )
          Text(
            text = "${((HeyNumoSettings.speechRate * 100f).roundToInt() / 100f)}x",
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = NumoDS.HeroTealMid
          )
        }
        Slider(
          value = HeyNumoSettings.speechRate,
          onValueChange = { HeyNumoSettings.updateSpeechRate(it) },
          valueRange = 0.75f..1.5f,
          steps = 5,
          colors = SliderDefaults.colors(
            thumbColor = NumoDS.HeroTealMid,
            activeTrackColor = NumoDS.Turquoise,
            inactiveTrackColor = NumoDS.TrackBg
          )
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("0.75x Slower", fontSize = 11.sp, color = NumoDS.TextMuted)
          Text("1.0x Normal", fontSize = 11.sp, color = NumoDS.TextMuted)
          Text("1.5x Faster", fontSize = 11.sp, color = NumoDS.TextMuted)
        }
      }

      // Voice Selection & Preview
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .background(NumoDS.CardSurfaceLight)
          .border(1.dp, NumoDS.CardBorderLight, RoundedCornerShape(18.dp))
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "Numo Voice",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )
        Text(
          text = "Choose how Numo sounds. Tap Play to preview each Gemini voice.",
          fontSize = 11.sp,
          color = NumoDS.TextMuted
        )
        HeyNumoSettings.voiceOptions.forEach { option ->
          val selected = HeyNumoSettings.selectedVoice == option.id
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(
                if (selected) NumoDS.Turquoise.copy(alpha = 0.14f)
                else NumoDS.TrackBg.copy(alpha = 0.7f)
              )
              .border(
                1.dp,
                if (selected) NumoDS.Turquoise else NumoDS.CardBorderLight,
                RoundedCornerShape(12.dp)
              )
              .clickable { HeyNumoSettings.updateSelectedVoice(option.id) }
              .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = option.id,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (selected) NumoDS.HeroTealMid else NumoDS.TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = option.character,
                  fontSize = 11.sp,
                  color = NumoDS.TextMuted
                )
                if (option.isRecommended) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Default",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NumoDS.HeroTealMid
                  )
                }
              }
              Text(
                text = option.description,
                fontSize = 11.sp,
                color = NumoDS.TextSecondary
              )
            }
            Box(
              modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(NumoDS.Turquoise.copy(alpha = 0.16f))
                .clickable {
                  HeyNumoSettings.updateSelectedVoice(option.id)
                  onPreviewVoice(option.id)
                },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Preview ${option.id}",
                tint = NumoDS.HeroTealMid,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }

      // Language Selector
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .background(NumoDS.CardSurfaceLight)
          .border(1.dp, NumoDS.CardBorderLight, RoundedCornerShape(18.dp))
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "Recognition & Voice Language",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          languages.forEach { (tag, label) ->
            val selected = HeyNumoSettings.languageTag == tag
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(
                  if (selected) NumoDS.Turquoise.copy(alpha = 0.16f)
                  else NumoDS.TrackBg
                )
                .border(
                  1.dp,
                  if (selected) NumoDS.Turquoise else NumoDS.CardBorderLight,
                  RoundedCornerShape(12.dp)
                )
                .clickable { HeyNumoSettings.updateLanguage(tag) }
                .padding(vertical = 8.dp, horizontal = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) NumoDS.HeroTealMid else NumoDS.TextSecondary,
                maxLines = 1
              )
            }
          }
        }
      }

      // Privacy Notice
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(NumoDS.TrackBg.copy(alpha = 0.85f))
          .border(1.dp, NumoDS.CardBorderLight, RoundedCornerShape(16.dp))
          .padding(12.dp),
        verticalAlignment = Alignment.Top
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = null,
          tint = NumoDS.HeroTealMid,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Privacy Notice: Hey Numo uses Android's on-device SpeechRecognizer for wake-word spotting and local financial engine queries. Voice audio is never streamed to external AI servers, and transactions always require your explicit Confirm tap.",
          fontSize = 11.sp,
          color = NumoDS.TextSecondary,
          lineHeight = 15.sp
        )
      }

      // Clear Conversation History
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(NumoDS.Magenta.copy(alpha = 0.10f))
          .border(1.dp, NumoDS.Magenta.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
          .clickable {
            HeyNumoSettings.clearRecentPrompts()
            onClearHistory()
          }
          .padding(vertical = 12.dp, horizontal = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.DeleteOutline,
          contentDescription = null,
          tint = NumoDS.Magenta,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Clear Conversation History",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.Magenta
        )
      }
    }
  }
}

@Composable
private fun SettingsToggleRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconTint: Color,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .background(NumoDS.CardSurfaceLight)
      .border(1.dp, NumoDS.CardBorderLight, RoundedCornerShape(18.dp))
      .padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(iconTint.copy(alpha = 0.12f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.size(19.dp)
      )
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = NumoDS.TextPrimary
      )
      Text(
        text = subtitle,
        fontSize = 11.sp,
        color = NumoDS.TextMuted,
        lineHeight = 15.sp
      )
    }
    Spacer(modifier = Modifier.width(8.dp))
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = NumoDS.Turquoise,
        uncheckedThumbColor = Color.White,
        uncheckedTrackColor = Color(0xFFD6C7BA)
      )
    )
  }
}
