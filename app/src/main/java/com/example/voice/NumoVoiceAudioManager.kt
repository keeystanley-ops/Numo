package com.example.voice

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.PlaybackParams
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.SpeechConfig
import com.google.firebase.ai.type.Voice
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import java.util.UUID
import kotlin.math.sqrt

enum class VoiceOrbState {
  IDLE,
  LISTENING,
  THINKING,
  SPEAKING,
  ERROR
}

/**
 * Manages Android Microphone permissions, Speech-to-Text (`SpeechRecognizer` with
 * automatic in-app `AudioRecord` PCM->WAV fallback for Gemini multimodal audio),
 * foreground on-device wake-word spotting ("Hey Numo"), and natural male Gemini
 * Live-style Text-to-Speech (`gemini-3.1-flash-tts-preview`) with male device TTS fallback.
 */
class NumoVoiceAudioManager(private val context: Context) {
  companion object {
    private const val TAG = "NumoVoiceAudioManager"
    private const val GEMINI_TTS_MODEL = "gemini-3.1-flash-tts-preview"
    private const val TTS_TIMEOUT_MS = 15_000L
    private const val SAMPLE_RATE_HZ = 16_000
    private const val MAX_RAW_RECORDING_MS = 12_000L

    // Common male neural/on-device Android TTS voice name markers
    private val MALE_VOICE_TOKENS = listOf(
      "en-us-x-iol",
      "en-us-x-iom",
      "en-us-x-tpd",
      "en-gb-x-gbd",
      "en-gb-x-rjs",
      "-male",
      "_male",
      "#male"
    )
  }

  private val appContext = context.applicationContext
  private val mainHandler = Handler(Looper.getMainLooper())
  private val audioScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
  private val firebaseAi by lazy { Firebase.ai }

  private var speechRecognizer: SpeechRecognizer? = null
  private var wakeRecognizer: SpeechRecognizer? = null
  private var wakeCycleRunnable: Runnable? = null

  private var tts: TextToSpeech? = null
  private var ttsReady = false
  private var mediaPlayer: MediaPlayer? = null
  private var ttsFetchJob: Job? = null

  private var rawRecordJob: Job? = null
  @Volatile
  private var isRawRecording = false

  private var isListeningActive = false
  private var isWakeModeActive = false

  var onOrbStateChanged: ((VoiceOrbState) -> Unit)? = null
  var onRmsChanged: ((Float) -> Unit)? = null
  var onPartialTranscript: ((String) -> Unit)? = null
  var onFinalTranscript: ((String) -> Unit)? = null
  var onFinalAudioWav: ((ByteArray) -> Unit)? = null
  var onListeningHintChanged: ((String?) -> Unit)? = null
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
    val engine = tts ?: return
    val locale = when (HeyNumoSettings.languageTag) {
      "en-KE" -> Locale("en", "KE")
      "sw-KE" -> Locale("sw", "KE")
      else -> Locale.US
    }
    engine.language = locale
    engine.setSpeechRate(HeyNumoSettings.speechRate)
    engine.setPitch(0.92f)

    // Pick a warmer male device voice when available for offline/timeout fallback
    runCatching {
      val voices = engine.voices?.toList().orEmpty()
      val matchingLocale = voices.filter { v ->
        v.locale?.language.equals(locale.language, ignoreCase = true)
      }
      val preferredMale = matchingLocale.firstOrNull { v ->
        val nameLower = v.name.lowercase(Locale.US)
        MALE_VOICE_TOKENS.any { token -> nameLower.contains(token) }
      } ?: voices.firstOrNull { v ->
        val nameLower = v.name.lowercase(Locale.US)
        MALE_VOICE_TOKENS.any { token -> nameLower.contains(token) }
      }
      if (preferredMale != null) {
        engine.voice = preferredMale
      }
    }
  }

  fun speak(text: String) {
    if (!HeyNumoSettings.voiceResponseEnabled || text.isBlank()) {
      onOrbStateChanged?.invoke(VoiceOrbState.IDLE)
      return
    }
    speakWithVoice(text = text, voiceName = HeyNumoSettings.selectedVoice)
  }

  fun previewVoice(voiceName: String) {
    val sampleText =
      "Hey there! I'm Numo, your financial companion. You've got KES 3,500 left in your food budget this month."
    speakWithVoice(text = sampleText, voiceName = voiceName)
  }

  @OptIn(PublicPreviewAPI::class)
  private fun speakWithVoice(text: String, voiceName: String) {
    stopAudioPlaybackOnly()
    val resolvedVoice = voiceName.ifBlank { HeyNumoSettings.DEFAULT_VOICE_NAME }
    onOrbStateChanged?.invoke(VoiceOrbState.THINKING)

    ttsFetchJob = audioScope.launch {
      try {
        val wavOrAudioBytes = withContext(Dispatchers.IO) {
          withTimeout(TTS_TIMEOUT_MS) {
            val ttsModel = firebaseAi.generativeModel(
              modelName = GEMINI_TTS_MODEL,
              generationConfig = generationConfig {
                responseModalities = listOf(ResponseModality.AUDIO)
                speechConfig = SpeechConfig(voice = Voice(resolvedVoice))
              },
              systemInstruction = content {
                text(
                  "Read the user's message aloud naturally, warmly, and verbatim like Gemini Live. " +
                    "Do not add preambles, commentary, or extra words."
                )
              }
            )
            val response = ttsModel.generateContent(text)
            val audioPart = response.inlineDataParts.firstOrNull { part ->
              part.mimeType.startsWith("audio/", ignoreCase = true)
            } ?: response.inlineDataParts.firstOrNull()
              ?: return@withTimeout null

            convertAudioPartToPlayableBytes(audioPart.inlineData, audioPart.mimeType)
          }
        }

        if (wavOrAudioBytes != null && wavOrAudioBytes.isNotEmpty()) {
          playAudioBytes(wavOrAudioBytes, fallbackText = text)
        } else {
          speakWithDeviceTts(text)
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.w(TAG, "Gemini TTS failed for voice=$resolvedVoice; using male device TTS fallback", e)
        speakWithDeviceTts(text)
      }
    }
  }

  private fun convertAudioPartToPlayableBytes(raw: ByteArray, mimeType: String): ByteArray {
    if (raw.isEmpty()) return raw
    val lower = mimeType.lowercase(Locale.US)
    val alreadyHasRiffHeader = raw.size >= 12 &&
      raw[0] == 'R'.code.toByte() &&
      raw[1] == 'I'.code.toByte() &&
      raw[2] == 'F'.code.toByte() &&
      raw[3] == 'F'.code.toByte()
    if (alreadyHasRiffHeader || lower.contains("mpeg") || lower.contains("mp3") || lower.contains("ogg")) {
      return raw
    }
    if (lower.contains("l16") || lower.contains("pcm") || !lower.contains("wav")) {
      val sampleRate = Regex("rate=(\\d+)")
        .find(lower)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
        ?.coerceIn(8_000, 48_000)
        ?: 24_000
      return wrapPcmAsWav(pcmBytes = raw, sampleRateHz = sampleRate, channels = 1, bitsPerSample = 16)
    }
    return raw
  }

  private fun playAudioBytes(audioBytes: ByteArray, fallbackText: String) {
    try {
      val tempFile = File(appContext.cacheDir, "hey_numo_tts.wav")
      tempFile.writeBytes(audioBytes)
      val mp = MediaPlayer()
      mediaPlayer = mp
      mp.setDataSource(tempFile.absolutePath)
      mp.setOnPreparedListener { player ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          runCatching {
            val speed = HeyNumoSettings.speechRate.coerceIn(0.75f, 1.5f)
            player.playbackParams = PlaybackParams().setSpeed(speed)
          }
        }
        onOrbStateChanged?.invoke(VoiceOrbState.SPEAKING)
        player.start()
      }
      mp.setOnCompletionListener { player ->
        player.release()
        if (mediaPlayer === player) mediaPlayer = null
        onOrbStateChanged?.invoke(VoiceOrbState.IDLE)
      }
      mp.setOnErrorListener { player, _, _ ->
        player.release()
        if (mediaPlayer === player) mediaPlayer = null
        speakWithDeviceTts(fallbackText)
        true
      }
      mp.prepareAsync()
    } catch (_: Exception) {
      speakWithDeviceTts(fallbackText)
    }
  }

  private fun speakWithDeviceTts(text: String) {
    if (!ttsReady || text.isBlank()) {
      onOrbStateChanged?.invoke(VoiceOrbState.IDLE)
      return
    }
    applyTtsSettings()
    val utteranceId = UUID.randomUUID().toString()
    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
  }

  private fun stopAudioPlaybackOnly() {
    ttsFetchJob?.cancel()
    ttsFetchJob = null
    try {
      mediaPlayer?.stop()
    } catch (_: Exception) {
    }
    try {
      mediaPlayer?.release()
    } catch (_: Exception) {
    }
    mediaPlayer = null
    tts?.stop()
  }

  fun stopSpeaking() {
    stopAudioPlaybackOnly()
    onOrbStateChanged?.invoke(VoiceOrbState.IDLE)
  }

  fun startListening() {
    if (!hasRecordAudioPermission()) {
      onOrbStateChanged?.invoke(VoiceOrbState.ERROR)
      onVoiceError?.invoke("Microphone permission is required to use Hey Numo voice.")
      return
    }

    stopWakeWordListening()
    stopSpeaking()
    stopRawAudioCapture(deliverResult = false)
    onListeningHintChanged?.invoke(null)

    if (!isSpeechRecognitionAvailable()) {
      startRawAudioCapture()
      return
    }

    mainHandler.post {
      try {
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
          setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
              isListeningActive = true
              onListeningHintChanged?.invoke(null)
              onOrbStateChanged?.invoke(VoiceOrbState.LISTENING)
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
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
              when (error) {
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                  onOrbStateChanged?.invoke(VoiceOrbState.ERROR)
                  onVoiceError?.invoke("Microphone permission denied.")
                }
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                  onOrbStateChanged?.invoke(VoiceOrbState.ERROR)
                  onVoiceError?.invoke("I didn't catch that. Tap the mic and try again.")
                }
                // Service / OEM SpeechRecognizer issues -> fall back seamlessly to in-app AudioRecord + Gemini audio!
                SpeechRecognizer.ERROR_CLIENT,
                SpeechRecognizer.ERROR_SERVER,
                SpeechRecognizer.ERROR_AUDIO,
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
                SpeechRecognizer.ERROR_NETWORK,
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                  Log.w(TAG, "SpeechRecognizer error=$error; switching to in-app WAV recorder")
                  startRawAudioCapture()
                }
                else -> {
                  Log.w(TAG, "SpeechRecognizer error=$error; switching to in-app WAV recorder")
                  startRawAudioCapture()
                }
              }
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
        Log.w(TAG, "SpeechRecognizer launch threw exception; switching to in-app WAV recorder", e)
        isListeningActive = false
        startRawAudioCapture()
      }
    }
  }

  /**
   * Direct in-app microphone PCM -> WAV capture used cleanly whenever Android's
   * SpeechRecognizer service is unavailable or fails on OEM ROMs. Never opens external apps.
   */
  @SuppressLint("MissingPermission")
  private fun startRawAudioCapture() {
    if (!hasRecordAudioPermission() || isRawRecording) return
    val minBuffer = AudioRecord.getMinBufferSize(
      SAMPLE_RATE_HZ,
      AudioFormat.CHANNEL_IN_MONO,
      AudioFormat.ENCODING_PCM_16BIT
    )
    if (minBuffer <= 0) {
      onOrbStateChanged?.invoke(VoiceOrbState.ERROR)
      onVoiceError?.invoke("Microphone could not start. You can type your question using the keyboard icon.")
      return
    }

    isRawRecording = true
    isListeningActive = true
    onListeningHintChanged?.invoke("Tap mic again when done speaking")
    onOrbStateChanged?.invoke(VoiceOrbState.LISTENING)

    rawRecordJob = audioScope.launch {
      val pcmOut = ByteArrayOutputStream()
      val recordedBytes = withContext(Dispatchers.IO) {
        val recorder = try {
          AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE_HZ,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBuffer * 2
          )
        } catch (e: Exception) {
          Log.e(TAG, "AudioRecord creation failed", e)
          null
        }

        if (recorder == null || recorder.state != AudioRecord.STATE_INITIALIZED) {
          recorder?.release()
          return@withContext null
        }

        val buf = ByteArray(minBuffer)
        val startedAt = System.currentTimeMillis()
        try {
          recorder.startRecording()
          while (isActive && isRawRecording) {
            val read = recorder.read(buf, 0, buf.size)
            if (read > 0) {
              pcmOut.write(buf, 0, read)
              val rms = computeNormalizedPcmRms(buf, read)
              mainHandler.post { onRmsChanged?.invoke(rms) }
            }
            if (System.currentTimeMillis() - startedAt >= MAX_RAW_RECORDING_MS) {
              isRawRecording = false
            }
          }
        } catch (e: Exception) {
          Log.e(TAG, "AudioRecord loop failed", e)
        } finally {
          runCatching { recorder.stop() }
          runCatching { recorder.release() }
        }
        pcmOut.toByteArray()
      }

      isListeningActive = false
      onListeningHintChanged?.invoke(null)

      if (recordedBytes == null || recordedBytes.size < 3_200) {
        onOrbStateChanged?.invoke(VoiceOrbState.ERROR)
        onVoiceError?.invoke("Didn't capture clear audio. Tap the mic and speak closely.")
      } else {
        onOrbStateChanged?.invoke(VoiceOrbState.THINKING)
        val wav = wrapPcmAsWav(
          pcmBytes = recordedBytes,
          sampleRateHz = SAMPLE_RATE_HZ,
          channels = 1,
          bitsPerSample = 16
        )
        onFinalAudioWav?.invoke(wav)
      }
    }
  }

  private fun stopRawAudioCapture(deliverResult: Boolean) {
    if (!isRawRecording && rawRecordJob == null) return
    isRawRecording = false
    if (!deliverResult) {
      rawRecordJob?.cancel()
      rawRecordJob = null
      isListeningActive = false
      onListeningHintChanged?.invoke(null)
    }
  }

  fun stopListening() {
    if (isRawRecording) {
      // Setting false lets rawRecordJob finish cleanly and emit WAV audio
      isRawRecording = false
      isListeningActive = false
      onOrbStateChanged?.invoke(VoiceOrbState.THINKING)
      return
    }
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
    wakeCycleRunnable?.let { mainHandler.removeCallbacks(it) }
    wakeCycleRunnable = null
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
    wakeCycleRunnable?.let { mainHandler.removeCallbacks(it) }
    val runnable = Runnable {
      if (!isWakeModeActive || isListeningActive || !HeyNumoSettings.wakeWordEnabled) return@Runnable
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
        }
        wakeRecognizer?.startListening(wakeIntent)
      } catch (_: Exception) {
        if (isWakeModeActive) scheduleNextWakeCycle(delayMs = 1500L)
      }
    }
    wakeCycleRunnable = runnable
    mainHandler.postDelayed(runnable, delayMs)
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

  private fun computeNormalizedPcmRms(bytes: ByteArray, length: Int): Float {
    var sumSquares = 0.0
    var samples = 0
    var i = 0
    while (i + 1 < length) {
      val low = bytes[i].toInt() and 0xFF
      val high = bytes[i + 1].toInt()
      val sample = ((high shl 8) or low).toShort().toDouble()
      sumSquares += sample * sample
      samples++
      i += 2
    }
    if (samples == 0) return 0.08f
    val rms = sqrt(sumSquares / samples) / 14_000.0
    return rms.toFloat().coerceIn(0.05f, 1f)
  }

  private fun wrapPcmAsWav(
    pcmBytes: ByteArray,
    sampleRateHz: Int,
    channels: Int,
    bitsPerSample: Int
  ): ByteArray {
    val byteRate = sampleRateHz * channels * bitsPerSample / 8
    val blockAlign = channels * bitsPerSample / 8
    val totalDataLen = pcmBytes.size
    val totalAudioLen = totalDataLen + 36
    val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
      put("RIFF".toByteArray(Charsets.US_ASCII))
      putInt(totalAudioLen)
      put("WAVE".toByteArray(Charsets.US_ASCII))
      put("fmt ".toByteArray(Charsets.US_ASCII))
      putInt(16) // PCM subchunk size
      putShort(1.toShort()) // AudioFormat = 1 (PCM)
      putShort(channels.toShort())
      putInt(sampleRateHz)
      putInt(byteRate)
      putShort(blockAlign.toShort())
      putShort(bitsPerSample.toShort())
      put("data".toByteArray(Charsets.US_ASCII))
      putInt(totalDataLen)
    }.array()
    return ByteArray(44 + pcmBytes.size).apply {
      System.arraycopy(header, 0, this, 0, 44)
      System.arraycopy(pcmBytes, 0, this, 44, pcmBytes.size)
    }
  }

  fun destroy() {
    stopWakeWordListening()
    stopRawAudioCapture(deliverResult = false)
    stopAudioPlaybackOnly()
    audioScope.cancel()
    mainHandler.post {
      speechRecognizer?.destroy()
      speechRecognizer = null
    }
    tts?.stop()
    tts?.shutdown()
    tts = null
  }
}
