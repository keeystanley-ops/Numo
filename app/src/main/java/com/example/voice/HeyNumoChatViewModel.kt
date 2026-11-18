package com.example.voice

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.UUID

sealed interface ChatUiState {
  data object Idle : ChatUiState
  data object Loading : ChatUiState
  data class Streaming(val messageId: String, val text: String) : ChatUiState
  data class Success(val text: String) : ChatUiState
  data class Error(val message: String) : ChatUiState
}

/**
 * MVVM ViewModel backing Hey Numo's multi-turn Gemini chatbot.
 * Coordinates Firebase AI Logic streaming, 60s timeouts, cooperative cancellation,
 * explicit error propagation, and financial confirmation cards.
 */
class HeyNumoChatViewModel : ViewModel() {
  companion object {
    private const val TAG = "HeyNumoChatViewModel"
    private const val INFERENCE_TIMEOUT_MS = 60_000L
  }

  private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
  val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

  private val _messages = MutableStateFlow<List<HeyNumoMessage>>(emptyList())
  val messages: StateFlow<List<HeyNumoMessage>> = _messages.asStateFlow()

  private val _orbState = MutableStateFlow(VoiceOrbState.IDLE)
  val orbState: StateFlow<VoiceOrbState> = _orbState.asStateFlow()

  private var assistantEngine = NumoFinancialAssistantEngine()
  private var pendingResetHistory = false
  private var activeJob: Job? = null

  private var lastUserId: String = ""
  private var lastUserName: String = ""
  private var lastCustomAccounts: List<Pair<String, String>> = emptyList()
  private var lastQueryText: String? = null
  private var lastAudioWav: ByteArray? = null
  private var onSpeakCallback: ((String) -> Unit)? = null

  fun configureAccounts(customAccounts: List<Pair<String, String>>) {
    lastCustomAccounts = customAccounts
    assistantEngine = NumoFinancialAssistantEngine(customAccounts)
  }

  fun setOrbState(state: VoiceOrbState) {
    _orbState.value = state
  }

  fun submitQuery(
    userId: String,
    userName: String,
    rawQuery: String,
    customAccounts: List<Pair<String, String>>,
    speakReply: Boolean,
    onSpeak: (String) -> Unit
  ) {
    val cleanQuery = rawQuery.trim()
    if (cleanQuery.isEmpty()) return

    lastUserId = userId
    lastUserName = userName
    lastCustomAccounts = customAccounts
    lastQueryText = cleanQuery
    lastAudioWav = null
    onSpeakCallback = onSpeak

    HeyNumoSettings.recordRecentPrompt(cleanQuery)
    appendUserMessageIfNotDuplicateRetry(cleanQuery)

    executeGeminiTurn(
      userId = userId,
      userName = userName,
      query = cleanQuery,
      audioWavBytes = null,
      customAccounts = customAccounts,
      speakReply = speakReply,
      onSpeak = onSpeak
    )
  }

  fun submitAudioQuery(
    userId: String,
    userName: String,
    audioWavBytes: ByteArray,
    customAccounts: List<Pair<String, String>>,
    speakReply: Boolean,
    onSpeak: (String) -> Unit
  ) {
    if (audioWavBytes.isEmpty()) return

    lastUserId = userId
    lastUserName = userName
    lastCustomAccounts = customAccounts
    lastQueryText = null
    lastAudioWav = audioWavBytes
    onSpeakCallback = onSpeak

    appendUserMessageIfNotDuplicateRetry("Voice message")

    executeGeminiTurn(
      userId = userId,
      userName = userName,
      query = "",
      audioWavBytes = audioWavBytes,
      customAccounts = customAccounts,
      speakReply = speakReply,
      onSpeak = onSpeak
    )
  }

  fun retryLastQuery(
    speakReply: Boolean,
    onSpeak: (String) -> Unit
  ) {
    val audio = lastAudioWav
    val query = lastQueryText
    if (audio == null && query == null) return
    executeGeminiTurn(
      userId = lastUserId,
      userName = lastUserName,
      query = query.orEmpty(),
      audioWavBytes = audio,
      customAccounts = lastCustomAccounts,
      speakReply = speakReply,
      onSpeak = onSpeak
    )
  }

  private fun appendUserMessageIfNotDuplicateRetry(query: String) {
    val current = _messages.value
    // Avoid duplicating user message if retrying immediately after an error for the exact same turn
    val alreadyLastUser = _uiState.value is ChatUiState.Error &&
      current.lastOrNull()?.isUser == true &&
      current.lastOrNull()?.text == query
    if (!alreadyLastUser) {
      _messages.update { it + HeyNumoMessage(isUser = true, text = query) }
    }
  }

  private fun executeGeminiTurn(
    userId: String,
    userName: String,
    query: String,
    audioWavBytes: ByteArray?,
    customAccounts: List<Pair<String, String>>,
    speakReply: Boolean,
    onSpeak: (String) -> Unit
  ) {
    activeJob?.cancel()

    val assistantMsgId = UUID.randomUUID().toString()
    val shouldReset = pendingResetHistory
    pendingResetHistory = false

    _uiState.value = ChatUiState.Loading
    _orbState.value = VoiceOrbState.THINKING

    // Insert a streaming placeholder message in the scrollable thread
    _messages.update { current ->
      current + HeyNumoMessage(
        id = assistantMsgId,
        isUser = false,
        text = "",
        isStreaming = true
      )
    }

    activeJob = viewModelScope.launch {
      try {
        var finalCompleted: GeminiChatStreamEvent.Completed? = null

        withTimeout(INFERENCE_TIMEOUT_MS) {
          val stream = if (audioWavBytes != null) {
            GeminiHeyNumoClient.sendAudioChatStream(
              userId = userId,
              userName = userName,
              audioWavBytes = audioWavBytes,
              customAccounts = customAccounts,
              resetHistory = shouldReset
            )
          } else {
            GeminiHeyNumoClient.sendChatStream(
              userId = userId,
              userName = userName,
              message = query,
              customAccounts = customAccounts,
              resetHistory = shouldReset
            )
          }
          stream.collect { event ->
            when (event) {
              is GeminiChatStreamEvent.Delta -> {
                _uiState.value = ChatUiState.Streaming(assistantMsgId, event.accumulatedText)
                updateAssistantBubble(
                  id = assistantMsgId,
                  text = event.accumulatedText,
                  pendingAction = null,
                  isStreaming = true
                )
              }
              is GeminiChatStreamEvent.Completed -> {
                finalCompleted = event
                assistantEngine.setPendingAction(event.pendingAction)
                updateAssistantBubble(
                  id = assistantMsgId,
                  text = event.replyText,
                  pendingAction = event.pendingAction,
                  isStreaming = false
                )
              }
            }
          }
        }

        val completed = finalCompleted
        if (completed != null) {
          _uiState.value = ChatUiState.Success(completed.replyText)
          if (speakReply && HeyNumoSettings.voiceResponseEnabled) {
            onSpeak(completed.spokenText)
          } else {
            _orbState.value = VoiceOrbState.IDLE
          }
        } else {
          removeEmptyStreamingPlaceholder(assistantMsgId)
          val msg = "Hey Numo returned an empty response. Please try again."
          _uiState.value = ChatUiState.Error(msg)
          _orbState.value = VoiceOrbState.ERROR
        }
      } catch (e: TimeoutCancellationException) {
        Log.e(TAG, "Gemini chat generation timed out after 60s", e)
        removeEmptyStreamingPlaceholder(assistantMsgId)
        _uiState.value = ChatUiState.Error(
          "Hey Numo timed out after 60 seconds. Please check your connection and try again."
        )
        _orbState.value = VoiceOrbState.ERROR
      } catch (e: CancellationException) {
        // Always re-throw CancellationException to preserve cooperative coroutine cancellation
        throw e
      } catch (e: Exception) {
        Log.e(TAG, "Firebase AI chat generation failed", e)
        removeEmptyStreamingPlaceholder(assistantMsgId)
        val diagnostic = e.localizedMessage?.takeIf { it.isNotBlank() }
          ?: "Unable to reach Gemini right now. Please check your connection and try again."
        _uiState.value = ChatUiState.Error(diagnostic)
        _orbState.value = VoiceOrbState.ERROR
      }
    }
  }

  private fun updateAssistantBubble(
    id: String,
    text: String,
    pendingAction: PendingFinancialAction?,
    isStreaming: Boolean
  ) {
    _messages.update { list ->
      list.map { msg ->
        if (msg.id == id) {
          msg.copy(
            text = text,
            pendingAction = pendingAction,
            isStreaming = isStreaming
          )
        } else {
          msg
        }
      }
    }
  }

  private fun removeEmptyStreamingPlaceholder(id: String) {
    _messages.update { list ->
      list.filterNot { it.id == id && it.isStreaming && it.text.isBlank() }
        .map { msg ->
          if (msg.id == id && msg.isStreaming) msg.copy(isStreaming = false) else msg
        }
    }
  }

  fun confirmAction(
    action: PendingFinancialAction,
    onSpeak: (String) -> Unit
  ) {
    _messages.update { list ->
      list.map { msg ->
        if (msg.pendingAction?.id == action.id) {
          msg.copy(
            pendingAction = action.copy(
              statusLabel = "Confirmed",
              isResolved = true,
              wasConfirmed = true
            )
          )
        } else {
          msg
        }
      }
    }
    val reply = assistantEngine.executePendingAction(action)
    _messages.update { it + HeyNumoMessage(isUser = false, text = reply.displayText) }
    if (HeyNumoSettings.voiceResponseEnabled) {
      onSpeak(reply.spokenText)
    }
  }

  fun cancelAction(
    action: PendingFinancialAction,
    onSpeak: (String) -> Unit
  ) {
    _messages.update { list ->
      list.map { msg ->
        if (msg.pendingAction?.id == action.id) {
          msg.copy(
            pendingAction = action.copy(
              statusLabel = "Cancelled",
              isResolved = true,
              wasConfirmed = false
            )
          )
        } else {
          msg
        }
      }
    }
    val reply = assistantEngine.cancelPendingAction(action)
    _messages.update { it + HeyNumoMessage(isUser = false, text = reply.displayText) }
    if (HeyNumoSettings.voiceResponseEnabled) {
      onSpeak(reply.spokenText)
    }
  }

  fun clearConversation() {
    activeJob?.cancel()
    _messages.value = emptyList()
    pendingResetHistory = true
    GeminiHeyNumoClient.resetChatSession()
    assistantEngine.setPendingAction(null)
    _uiState.value = ChatUiState.Idle
    _orbState.value = VoiceOrbState.IDLE
  }

  fun dismissError() {
    if (_uiState.value is ChatUiState.Error) {
      _uiState.value = ChatUiState.Idle
      _orbState.value = VoiceOrbState.IDLE
    }
  }
}
