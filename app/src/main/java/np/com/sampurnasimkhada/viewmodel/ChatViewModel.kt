package np.com.sampurnasimkhada.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import np.com.sampurnasimkhada.data.remote.dto.MessageDto
import np.com.sampurnasimkhada.data.repository.GroqRepository
import np.com.sampurnasimkhada.data.repository.MedicineRepository

data class ChatMessage(val role: String, val text: String)  // role: "user" | "assistant"

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isLoading: Boolean = false,
    val contextMedicineName: String? = null,
)

class ChatViewModel(
    private val aiRepo: GroqRepository,
    private val medicineRepo: MedicineRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _state.asStateFlow()

    // History sent to the API (excludes the welcome message)
    private val apiHistory = mutableListOf<MessageDto>()

    fun init(contextMedicineId: Long?) {
        viewModelScope.launch {
            val meds    = medicineRepo.getAllMedicines()
            val ctxMed  = contextMedicineId?.let { id -> meds.find { it.id == id } }
            val welcome = if (ctxMed != null)
                "Hi! I can see you're asking about ${ctxMed.name} (${ctxMed.dosage}). What would you like to know?"
            else
                "Hi! I'm your medicine assistant 🤖\n\nI can help with:\n• Medicine uses & dosages\n• Side effects & interactions\n• Drug warnings\n• General health questions\n\nWhat would you like to know?"

            _state.update {
                it.copy(
                    messages              = listOf(ChatMessage("assistant", welcome)),
                    contextMedicineName   = ctxMed?.let { m -> "${m.name} ${m.dosage}" },
                )
            }

            // Auto-query if context medicine provided
            if (ctxMed != null) {
                send("Tell me about ${ctxMed.name} ${ctxMed.dosage} — main uses, side effects, and warnings.", auto = true)
            }
        }
    }

    fun onInput(v: String) { _state.update { it.copy(input = v) } }

    fun send(text: String = _state.value.input.trim(), auto: Boolean = false) {
        if (text.isBlank() || _state.value.isLoading) return
        if (!auto) _state.update { it.copy(input = "") }

        _state.update { it.copy(
            messages = it.messages + ChatMessage("user", text),
            isLoading = true,
        )}
        apiHistory.add(MessageDto("user", text))

        viewModelScope.launch {
            val meds = medicineRepo.getAllMedicines()
            val systemPrompt = aiRepo.buildMedicineChatSystemPrompt(
                medicineNames  = meds.map { it.name },
                focusMedicine  = _state.value.contextMedicineName,
            )
            aiRepo.sendChatMessage(apiHistory.toList(), systemPrompt)
                .onSuccess { reply ->
                    apiHistory.add(MessageDto("assistant", reply))
                    _state.update { it.copy(
                        messages  = it.messages + ChatMessage("assistant", reply),
                        isLoading = false,
                    )}
                }
                .onFailure { error ->
                    _state.update { it.copy(
                        messages = it.messages + ChatMessage("assistant", "Error: ${error.message}"),
                        isLoading = false,
                    )}

                }
        }
    }
}
