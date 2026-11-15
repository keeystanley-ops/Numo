package com.example.ui.voice

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.voice.GeminiHeyNumoClient
import com.example.voice.HeyNumoMessage
import com.example.voice.HeyNumoSettings
import com.example.voice.NumoFinancialAssistantEngine
import com.example.voice.NumoVoiceAudioManager
import com.example.voice.PendingFinancialAction
import com.example.voice.VoiceOrbState
import kotlinx.coroutines.launch

enum class HeyNumoSubScreen {
  LISTENING,
  HOME,
  CHAT
}

/**
 * Coordinates the 3-screen Hey Numo experience (Listening, Home, Chat),
 * on-device SpeechRecognizer, foreground wake-word spotting, TTS, permissions, and Settings.
 */
@Composable
fun HeyNumoHost(
  userName: String,
  userId: String = userName,
  customAccounts: List<Pair<String, String>>,
  initialScreen: HeyNumoSubScreen = HeyNumoSubScreen.HOME,
  startListeningImmediately: Boolean = false,
  onOpenBudget: () -> Unit,
  onOpenNotifications: () -> Unit,
  onBackToDashboard: () -> Unit
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(Unit) {
    HeyNumoSettings.init(context)
  }

  val audioManager = remember { NumoVoiceAudioManager(context) }
  val engine = remember(customAccounts) { NumoFinancialAssistantEngine(customAccounts) }

  var currentScreen by remember(initialScreen) { mutableStateOf(initialScreen) }
  var showSettings by remember { mutableStateOf(false) }

  var orbState by remember { mutableStateOf(VoiceOrbState.IDLE) }
  var rmsLevel by remember { mutableFloatStateOf(0f) }
  var partialTranscript by remember { mutableStateOf("") }
  var statusError by remember { mutableStateOf<String?>(null) }

  var hasMicPermission by remember { mutableStateOf(audioManager.hasRecordAudioPermission()) }
  var pendingStartVoiceAfterPermission by remember { mutableStateOf(false) }

  val messages = remember { mutableStateListOf<HeyNumoMessage>() }
  var pendingResetHistory by remember { mutableStateOf(false) }

  fun pulseHaptic() {
    if (HeyNumoSettings.hapticFeedbackEnabled) {
      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
  }

  fun submitQuery(query: String, openChat: Boolean) {
    val clean = query.trim()
    if (clean.isEmpty()) return

    HeyNumoSettings.recordRecentPrompt(clean)
    messages.add(HeyNumoMessage(isUser = true, text = clean))
    orbState = VoiceOrbState.THINKING

    if (openChat) {
      currentScreen = HeyNumoSubScreen.CHAT
    }

    val shouldReset = pendingResetHistory
    pendingResetHistory = false

    coroutineScope.launch {
      val reply = engine.processUserQueryWithGemini(
        userId = userId,
        rawQuery = clean,
        resetHistory = shouldReset
      )
      messages.add(
        HeyNumoMessage(
          isUser = false,
          text = reply.displayText,
          pendingAction = reply.pendingAction
        )
      )

      if (HeyNumoSettings.voiceResponseEnabled) {
        audioManager.speak(reply.spokenText)
      } else {
        orbState = VoiceOrbState.IDLE
      }
    }
  }

  val micPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted ->
    hasMicPermission = granted
    if (granted) {
      if (pendingStartVoiceAfterPermission) {
        pendingStartVoiceAfterPermission = false
        currentScreen = HeyNumoSubScreen.LISTENING
        statusError = null
        audioManager.startListening()
      } else if (HeyNumoSettings.wakeWordEnabled && currentScreen == HeyNumoSubScreen.HOME) {
        audioManager.startWakeWordListening()
      }
    } else {
      pendingStartVoiceAfterPermission = false
      statusError = "Microphone permission is required for voice commands."
      orbState = VoiceOrbState.ERROR
    }
  }

  fun beginVoiceCapture() {
    pulseHaptic()
    statusError = null
    if (!audioManager.hasRecordAudioPermission()) {
      pendingStartVoiceAfterPermission = true
      micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
      return
    }
    hasMicPermission = true
    currentScreen = HeyNumoSubScreen.LISTENING
    audioManager.startListening()
  }

  // Wire audio manager callbacks
  DisposableEffect(audioManager) {
    audioManager.onOrbStateChanged = { state -> orbState = state }
    audioManager.onRmsChanged = { rms -> rmsLevel = rms }
    audioManager.onPartialTranscript = { partial -> partialTranscript = partial }
    audioManager.onVoiceError = { err -> statusError = err }
    audioManager.onFinalTranscript = { finalText ->
      partialTranscript = ""
      submitQuery(finalText, openChat = true)
    }
    audioManager.onWakeWordDetected = {
      pulseHaptic()
      beginVoiceCapture()
    }
    onDispose {
      audioManager.destroy()
    }
  }

  // Auto-start listening if triggered via wake word or direct voice launch
  LaunchedEffect(startListeningImmediately) {
    if (startListeningImmediately) {
      beginVoiceCapture()
    }
  }

  // Manage foreground wake-word listening on Home screen and pause cleanly on background
  LaunchedEffect(currentScreen, HeyNumoSettings.wakeWordEnabled, hasMicPermission) {
    if (currentScreen == HeyNumoSubScreen.HOME && HeyNumoSettings.wakeWordEnabled && hasMicPermission) {
      audioManager.startWakeWordListening()
    } else if (currentScreen != HeyNumoSubScreen.HOME) {
      audioManager.stopWakeWordListening()
    }
  }

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> {
          hasMicPermission = audioManager.hasRecordAudioPermission()
          if (currentScreen == HeyNumoSubScreen.HOME && HeyNumoSettings.wakeWordEnabled && hasMicPermission) {
            audioManager.startWakeWordListening()
          }
        }
        Lifecycle.Event.ON_PAUSE -> {
          audioManager.stopWakeWordListening()
          audioManager.stopListening()
          audioManager.stopSpeaking()
        }
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  BackHandler {
    when {
      showSettings -> showSettings = false
      currentScreen == HeyNumoSubScreen.LISTENING -> {
        audioManager.stopListening()
        audioManager.stopSpeaking()
        currentScreen = HeyNumoSubScreen.HOME
      }
      currentScreen == HeyNumoSubScreen.CHAT -> {
        audioManager.stopListening()
        audioManager.stopSpeaking()
        currentScreen = HeyNumoSubScreen.HOME
      }
      else -> {
        audioManager.destroy()
        onBackToDashboard()
      }
    }
  }

  Crossfade(
    targetState = currentScreen,
    animationSpec = tween(260),
    label = "HeyNumoScreenTransition"
  ) { screen ->
    when (screen) {
      HeyNumoSubScreen.LISTENING -> {
        HeyNumoListeningScreen(
          orbState = orbState,
          rmsLevel = rmsLevel,
          partialTranscript = partialTranscript,
          statusError = statusError,
          onMicClick = {
            when (orbState) {
              VoiceOrbState.LISTENING -> {
                pulseHaptic()
                audioManager.stopListening()
              }
              VoiceOrbState.SPEAKING -> {
                pulseHaptic()
                audioManager.stopSpeaking()
                beginVoiceCapture()
              }
              else -> beginVoiceCapture()
            }
          },
          onOpenHome = {
            audioManager.stopListening()
            audioManager.stopSpeaking()
            currentScreen = HeyNumoSubScreen.HOME
          },
          onSwitchToKeyboard = {
            audioManager.stopListening()
            currentScreen = HeyNumoSubScreen.CHAT
          },
          onOpenSettings = { showSettings = true },
          onBack = {
            audioManager.stopListening()
            audioManager.stopSpeaking()
            currentScreen = HeyNumoSubScreen.HOME
          }
        )
      }

      HeyNumoSubScreen.HOME -> {
        HeyNumoHomeScreen(
          userName = userName,
          onStartVoiceAssistant = { beginVoiceCapture() },
          onSubmitPrompt = { prompt -> submitQuery(prompt, openChat = true) },
          onStartNewChat = {
            messages.clear()
            pendingResetHistory = true
            currentScreen = HeyNumoSubScreen.CHAT
          },
          onOpenSettings = { showSettings = true },
          onOpenNotifications = onOpenNotifications,
          onViewBudget = onOpenBudget,
          onBack = {
            audioManager.destroy()
            onBackToDashboard()
          }
        )
      }

      HeyNumoSubScreen.CHAT -> {
        HeyNumoChatScreen(
          messages = messages,
          orbState = orbState,
          onSendMessage = { text -> submitQuery(text, openChat = false) },
          onConfirmAction = { action: PendingFinancialAction ->
            pulseHaptic()
            val idx = messages.indexOfFirst { it.pendingAction?.id == action.id }
            if (idx >= 0) {
              val old = messages[idx]
              messages[idx] = old.copy(
                pendingAction = action.copy(
                  statusLabel = "Confirmed",
                  isResolved = true,
                  wasConfirmed = true
                )
              )
            }
            val reply = engine.executePendingAction(action)
            messages.add(HeyNumoMessage(isUser = false, text = reply.displayText))
            coroutineScope.launch {
              GeminiHeyNumoClient.confirmAction(userId, action.id, confirmed = true)
            }
            if (HeyNumoSettings.voiceResponseEnabled) {
              audioManager.speak(reply.spokenText)
            }
          },
          onCancelAction = { action: PendingFinancialAction ->
            pulseHaptic()
            val idx = messages.indexOfFirst { it.pendingAction?.id == action.id }
            if (idx >= 0) {
              val old = messages[idx]
              messages[idx] = old.copy(
                pendingAction = action.copy(
                  statusLabel = "Cancelled",
                  isResolved = true,
                  wasConfirmed = false
                )
              )
            }
            val reply = engine.cancelPendingAction(action)
            messages.add(HeyNumoMessage(isUser = false, text = reply.displayText))
            coroutineScope.launch {
              GeminiHeyNumoClient.confirmAction(userId, action.id, confirmed = false)
            }
            if (HeyNumoSettings.voiceResponseEnabled) {
              audioManager.speak(reply.spokenText)
            }
          },
          onMicClick = { beginVoiceCapture() },
          onSwitchToVoiceMode = { beginVoiceCapture() },
          onOpenSettings = { showSettings = true },
          onBack = {
            audioManager.stopSpeaking()
            currentScreen = HeyNumoSubScreen.HOME
          }
        )
      }
    }
  }

  if (showSettings) {
    HeyNumoSettingsDialog(
      hasMicPermission = hasMicPermission,
      onRequestMicPermission = {
        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
      },
      onWakeWordToggled = { enabled ->
        if (enabled && currentScreen == HeyNumoSubScreen.HOME && hasMicPermission) {
          audioManager.startWakeWordListening()
        } else if (!enabled) {
          audioManager.stopWakeWordListening()
        }
      },
      onClearHistory = {
        messages.clear()
        pendingResetHistory = true
      },
      onDismiss = { showSettings = false }
    )
  }
}
