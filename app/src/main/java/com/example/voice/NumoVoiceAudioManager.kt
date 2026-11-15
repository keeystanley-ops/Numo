package com.example.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.content.ContextCompat
import java.util.Locale
import java.util.UUID

enum class VoiceOrbState {
  IDLE,
  LISTENING,
  THINKING,
  SPEAKING,
  ERROR
}

/**
 * Manages Android Microphone permissions, Speech-to-Text (`SpeechRecognizer`),
 * foreground on-device wake-word spotting ("Hey Numo"), and Text-to-Speech (`TextToSpeech`).
 */
class NumoVoiceAudioManager(private val context: Context) {
  private val appContext = context.applicationContext
  private val mainHandler = Handler(Looper.getMainLooper())

  private var speechRecognizer: SpeechRecognizer? = null
  private var wakeRecognizer: SpeechRecognizer? = null
  private var tts: TextToSpeech? = null
  private var ttsReady = false

  private var isListeningActive = false
  private var isWakeModeActive = false

  var onOrbStateChanged: ((VoiceOrbState) -> Unit)? = null
  var onRmsChanged: ((Float) -> Unit)? = null
  var onPartialTranscript: ((String) -> Unit)? = null
  var onFinalTranscript: ((String) -> Unit)? = null
  var onVoiceError: ((String) -> Unit)? = null
  var onWakeWordDetected: (() -> Unit)? = null

  init {
    tts = TextToSpeech(appContext) { status ->
      if (status == TextToSpeech.SUCCESS) {
        ttsReady = true
        applyTtsSettings()
      }
    }
    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
      override fun onStart(utteranceId: String?) {
        mainHandler.post { onOrbStateChanged?.invoke(VoiceOrbState.SPEAKING) }
      }

      override fun onDone(utteranceId: String?) {
        mainHandler.post { onOrbStateChanged?.invoke(VoiceOrbState.IDLE) }
      }

      @Deprecated("Deprecated in Java")
      override fun onError(utteranceId: String?) {
        mainHandler.post { onOrbStateChanged?.invoke(VoiceOrbState.IDLE) }
      }
    })
  }

  fun hasRecordAudioPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
      appContext,
      Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
  }

  fun isSpeechRecognitionAvailable(): Boolean {
    return SpeechRecognizer.isRecognitionAvailable(appContext)
  }

  private fun applyTtsSettings() {
    val locale = when (HeyNumoSettings.languageTag) {
      "en-KE" -> Locale("en", "KE")
      "sw-KE" -> Locale("sw", "KE")
      else -> Locale.US
    }
    tts?.language = locale
    tts?.setSpeechRate(HeyNumoSettings.speechRate)
  }

  fun speak(text: String) {
    if (!HeyNumoSettings.voiceResponseEnabled || !ttsReady || text.isBlank()) {
      onOrbStateChanged?.invoke(VoiceOrbState.IDLE)
      return
    }
    applyTtsSettings()
    val utteranceId = UUID.randomUUID().toString()
    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
  }

  fun stopSpeaking() {
    tts?.stop()
    onOrbStateChanged?.invoke(VoiceOrbState.IDLE)
  }

  fun startListening() {
    if (!hasRecordAudioPermission()) {
      onOrbStateChanged?.invoke(VoiceOrbState.ERROR)
      onVoiceError?.invoke("Microphone permission is required to use Hey Numo voice.")
      return
    }
    if (!isSpeechRecognitionAvailable()) {
      onOrbStateChanged?.invoke(VoiceOrbState.ERROR)
      onVoiceError?.invoke("Speech recognition is not available on this device. Use the keyboard input instead.")
      return
    }

    stopWakeWordListening()
    stopSpeaking()

    mainHandler.post {
      try {
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
          setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
              isListeningActive = true
              onOrbStateChanged?.invoke(VoiceOrbState.LISTENING)
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
              // Normalize typical SpeechRecognizer rmsdB (-2..10) into 0f..1f
              val norm = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1f)
              this@NumoVoiceAudioManager.onRmsChanged?.invoke(norm)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
              isListeningActive = false
              onOrbStateChanged?.invoke(VoiceOrbState.THINKING)
            }

            override fun onError(error: Int) {
              isListeningActive = false
              val msg = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Check your microphone."
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission denied."
                SpeechRecognizer.ERROR_NO_MATCH -> "I didn't catch that. Tap the mic and try again."
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out. Tap the mic to speak."
                SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                  "Speech service unavailable. You can type your question below."
                else -> "Could not hear clearly. Please try again."
              }
              onOrbStateChanged?.invoke(VoiceOrbState.ERROR)
              onVoiceError?.invoke(msg)
            }

            override fun onResults(results: Bundle?) {
              isListeningActive = false
              val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
              val transcript = matches?.firstOrNull()?.trim().orEmpty()
              if (transcript.isNotEmpty()) {
                onFinalTranscript?.invoke(transcript)
              } else {
                onOrbStateChanged?.invoke(VoiceOrbState.ERROR)
                onVoiceError?.invoke("I didn't catch that. Tap the mic to try again.")
              }
            }

            override fun onPartialResults(partialResults: Bundle?) {
              val partial = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
              if (partial.isNotEmpty()) {
                onPartialTranscript?.invoke(partial)
              }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
          })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
          putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
          putExtra(RecognizerIntent.EXTRA_LANGUAGE, HeyNumoSettings.languageTag)
          putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
          putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        speechRecognizer?.startListening(intent)
      } catch (e: Exception) {
        isListeningActive = false
        onOrbStateChanged?.invoke(VoiceOrbState.ERROR)
        onVoiceError?.invoke("Unable to start microphone: ${e.message ?: "Unknown error"}")
      }
    }
  }

  fun stopListening() {
    isListeningActive = false
    mainHandler.post {
      try {
        speechRecognizer?.stopListening()
      } catch (_: Exception) {
      }
    }
  }

  /**
   * Starts foreground on-device wake-word spotting for "Hey Numo" when enabled and permitted.
   * Uses `EXTRA_PREFER_OFFLINE` so no audio is continuously streamed to external AI APIs.
   */
  fun startWakeWordListening() {
    if (!HeyNumoSettings.wakeWordEnabled || !hasRecordAudioPermission() || !isSpeechRecognitionAvailable()) {
      return
    }
    if (isListeningActive || isWakeModeActive) return
    isWakeModeActive = true
    scheduleNextWakeCycle(delayMs = 200L)
  }

  fun stopWakeWordListening() {
    isWakeModeActive = false
    mainHandler.removeCallbacksAndMessages(null)
    mainHandler.post {
      try {
        wakeRecognizer?.cancel()
        wakeRecognizer?.destroy()
        wakeRecognizer = null
      } catch (_: Exception) {
      }
    }
  }

  private fun scheduleNextWakeCycle(delayMs: Long) {
    if (!isWakeModeActive || isListeningActive || !HeyNumoSettings.wakeWordEnabled) return
    mainHandler.postDelayed({
      if (!isWakeModeActive || isListeningActive || !HeyNumoSettings.wakeWordEnabled) return@postDelayed
      try {
        wakeRecognizer?.destroy()
        wakeRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
          setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
              if (isWakeModeActive && !isListeningActive) {
                scheduleNextWakeCycle(delayMs = 800L)
              }
            }

            override fun onResults(results: Bundle?) {
              val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
              if (containsWakePhrase(matches)) {
                stopWakeWordListening()
                onWakeWordDetected?.invoke()
              } else if (isWakeModeActive && !isListeningActive) {
                scheduleNextWakeCycle(delayMs = 350L)
              }
            }

            override fun onPartialResults(partialResults: Bundle?) {
              val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
              if (containsWakePhrase(matches)) {
                stopWakeWordListening()
                onWakeWordDetected?.invoke()
              }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
          })
        }

        val wakeIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
          putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
          putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
          putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
          putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
          }
        }
        wakeRecognizer?.startListening(wakeIntent)
      } catch (_: Exception) {
        if (isWakeModeActive) scheduleNextWakeCycle(delayMs = 1500L)
      }
    }, delayMs)
  }

  private fun containsWakePhrase(candidates: List<String>): Boolean {
    return candidates.any { text ->
      val normalized = text.lowercase(Locale.US).replace(Regex("[^a-z\\s]"), "")
      normalized.contains("hey numo") ||
        normalized.contains("hi numo") ||
        normalized.contains("ok numo") ||
        normalized.contains("hey newmo") ||
        normalized.contains("hey pneumo")
    }
  }

  fun destroy() {
    stopWakeWordListening()
    mainHandler.post {
      speechRecognizer?.destroy()
      speechRecognizer = null
    }
    tts?.stop()
    tts?.shutdown()
    tts = null
  }
}
