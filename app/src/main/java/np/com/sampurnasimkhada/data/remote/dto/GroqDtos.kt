package np.com.sampurnasimkhada.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * One message in the conversation history.
 * [role] is either "user" or "assistant".
 */
data class MessageDto(
    @SerializedName("role")    val role: String,
    @SerializedName("content") val content: String,
)

/**
 * Structured medicine-info payload expected from the AI when the
 * Medicine Detail screen requests it.
 *
 * The AI is prompted to return *only* valid JSON matching this shape.
 */
data class MedicineInfoDto(
    @SerializedName("uses")         val uses: List<String> = emptyList(),
    @SerializedName("side_effects") val sideEffects: List<String> = emptyList(),
    @SerializedName("warnings")     val warnings: List<String> = emptyList(),
    @SerializedName("tip")          val tip: String = "",
)
