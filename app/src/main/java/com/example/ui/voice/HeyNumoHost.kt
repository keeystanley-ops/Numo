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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voice.HeyNumoChatViewModel
import com.example.voice.HeyNumoSettings
import com.example.voice.NumoVoiceAudioManager
import com.example.voice.PendingFinancialAction
import com.example.voice.VoiceOrbState

enum class HeyNumoSubScreen {
  LISTENING,
  HOME,
  CHAT
}

/**
 * Coordinates the 3-screen Hey Numo experience (Listening, Home, Chat),
 * on-device SpeechRecognizer, foreground wake-word spotting, TTS, permissions,
 * Settings, and Firebase AI Logic multi-turn Gemini chat.
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
  onBackToDashboard: () -> Unit,
  chatViewModel: HeyNumoChatViewModel = viewModel()
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val lifecycleOwner = LocalLifecycleOwner.current

  LaunchedEffect(Unit) {
    HeyNumoSettings.init(context)
  }

  LaunchedEffect(customAccounts) {
    chatViewModel.configureAccounts(customAccounts)
  }

  val audioManager = remember { NumoVoiceAudioManager(context) }

  var currentScreen by rememberSaveable(initialScreen) { mutableStateOf(initialScreen) }
  var showSettings by remember { mutableStateOf(false) }

  val messages by chatViewModel.messages.collectAsStateWithLifecycle()
  val chatUiState by chatViewModel.uiState.collectAsStateWithLifecycle()
  val orbState by chatViewModel.orbState.collectAsStateWithLifecycle()

  var rmsLevel by remember { mutableFloatStateOf(0f) }
  var partialTranscript by remember { mutableStateOf("") }
  var listeningHint by remember { mutableStateOf<String?>(null) }
  var statusError by remember { mutableStateOf<String?>(null) }

  var hasMicPermission by remember { mutableStateOf(audioManager.hasRecordAudioPermission()) }
  var pendingStartVoiceAfterPermission by remember { mutableStateOf(false) }

  fun pulseHaptic() {
    if (HeyNumoSettings.hapticFeedbackEnabled) {
      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
  }

  fun submitQuery(query: String, openChat: Boolean) {
    val clean = query.trim()
    if (clean.isEmpty()) return
    statusError = null

    if (openChat) {
      currentScreen = HeyNumoSubScreen.CHAT
    }

    chatViewModel.submitQuery(
      userId = userId,
      userName = userName,
      rawQuery = clean,
      customAccounts = customAccounts,
      speakReply = HeyNumoSettings.voiceResponseEnabled,
      onSpeak = { spokenText -> audioManager.speak(spokenText) }
    )
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
      chatViewModel.setOrbState(VoiceOrbState.ERROR)
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
    audioManager.onOrbStateChanged = { state -> chatViewModel.setOrbState(state) }
    audioManager.onRmsChanged = { rms -> rmsLevel = rms }
    audioManager.onPartialTranscript = { partial -> partialTranscript = partial }
    audioManager.onListeningHintChanged = { hint -> listeningHint = hint }
    audioManager.onVoiceError = { err -> statusError = err }
    audioManager.onFinalTranscript = { finalText ->
      partialTranscript = ""
      listeningHint = null
      submitQuery(finalText, openChat = true)
    }
    audioManager.onFinalAudioWav = { wavBytes ->
      partialTranscript = ""
      listeningHint = null
      statusError = null
      currentScreen = HeyNumoSubScreen.CHAT
      chatViewModel.submitAudioQuery(
        userId = userId,
        userName = userName,
        audioWavBytes = wavBytes,
        customAccounts = customAccounts,
        speakReply = HeyNumoSettings.voiceResponseEnabled,
        onSpeak = { spokenText -> audioManager.speak(spokenText) }
      )
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
          listeningHint = listeningHint,
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
            chatViewModel.clearConversation()
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
          chatUiState = chatUiState,
          orbState = orbState,
          onSendMessage = { text -> submitQuery(text, openChat = false) },
          onRetryLastMessage = {
            chatViewModel.retryLastQuery(
              speakReply = HeyNumoSettings.voiceResponseEnabled,
              onSpeak = { spokenText -> audioManager.speak(spokenText) }
            )
          },
          onDismissError = { chatViewModel.dismissError() },
          onConfirmAction = { action: PendingFinancialAction ->
            pulseHaptic()
            chatViewModel.confirmAction(action) { spokenText ->
              audioManager.speak(spokenText)
            }
          },
          onCancelAction = { action: PendingFinancialAction ->
            pulseHaptic()
            chatViewModel.cancelAction(action) { spokenText ->
              audioManager.speak(spokenText)
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
      onPreviewVoice = { voiceName ->
        audioManager.previewVoice(voiceName)
      },
      onClearHistory = {
        chatViewModel.clearConversation()
      },
      onDismiss = { showSettings = false }
    )
  }
}
