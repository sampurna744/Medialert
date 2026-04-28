package np.com.sampurnasimkhada.data.repository

import com.google.gson.Gson
import np.com.sampurnasimkhada.data.remote.api.GroqApiService
import np.com.sampurnasimkhada.data.remote.api.GroqMessage
import np.com.sampurnasimkhada.data.remote.api.GroqRequest
import np.com.sampurnasimkhada.data.remote.dto.MessageDto
import np.com.sampurnasimkhada.data.remote.dto.MedicineInfoDto

class GroqRepository(
    private val api: GroqApiService,
    private val gson: Gson = Gson(),
) {
    companion object {
        private const val MODEL = "llama-3.3-70b-versatile"
    }

    suspend fun sendChatMessage(
        messages: List<MessageDto>,
        systemPrompt: String? = null,
    ): Result<String> = runCatching {
        val groqMessages = mutableListOf<GroqMessage>()
        systemPrompt?.let { groqMessages.add(GroqMessage("system", it)) }
        messages.forEach { groqMessages.add(GroqMessage(it.role, it.content)) }

        val response = api.chat(GroqRequest(model = MODEL, messages = groqMessages))

        if (!response.isSuccessful) {
            error("API error ${response.code()}: ${response.errorBody()?.string()}")
        }

        response.body()?.choices?.firstOrNull()?.message?.content
            ?: error("Empty response")
    }

    suspend fun getMedicineInfo(
        medicineName: String,
        dosage: String,
    ): Result<MedicineInfoDto> = runCatching {
        val prompt = """
            For the medicine "$medicineName $dosage", provide a JSON object with exactly these keys:
            - "uses": array of 3 concise strings
            - "side_effects": array of 3 concise strings
            - "warnings": array of 2 concise strings
            - "tip": one helpful sentence
            Respond with ONLY the JSON object. No markdown, no explanation, no extra text.
        """.trimIndent()

        val response = api.chat(
            GroqRequest(
                model = MODEL,
                messages = listOf(
                    GroqMessage("system", "You are a medical information assistant. Respond only with valid JSON."),
                    GroqMessage("user", prompt)
                )
            )
        )

        if (!response.isSuccessful) {
            error("API error ${response.code()}: ${response.errorBody()?.string()}")
        }

        val text = response.body()?.choices?.firstOrNull()?.message?.content
            ?: error("Empty response")

        val cleanJson = text.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        gson.fromJson(cleanJson, MedicineInfoDto::class.java)
    }

    fun buildMedicineChatSystemPrompt(
        medicineNames: List<String>,
        focusMedicine: String? = null,
    ): String = buildString {
        append("You are MediAlert's medical information assistant. ")
        if (medicineNames.isNotEmpty()) {
            append("The user's medicines are: ${medicineNames.joinToString(", ")}. ")
        }
        focusMedicine?.let { append("They are currently asking about: $it. ") }
        append(
            "Be helpful, accurate and concise (2–4 sentences). " +
                    "Always advise the user to consult their doctor for personalised advice. " +
                    "Reply in plain text only — no markdown."
        )
    }
}