package np.com.sampurnasimkhada.data.remote.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class GroqMessage(val role: String, val content: String)
data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val max_tokens: Int = 1024,
)
data class GroqChoice(val message: GroqMessage?)
data class GroqResponse(val choices: List<GroqChoice>?)

interface GroqApiService {
    @POST("openai/v1/chat/completions")
    suspend fun chat(@Body request: GroqRequest): retrofit2.Response<GroqResponse>
}

class GroqAuthInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()
        return chain.proceed(request)
    }
}

object GroqRetrofitFactory {
    private const val BASE_URL = "https://api.groq.com/"

    fun create(apiKey: String): GroqApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(GroqAuthInterceptor(apiKey))
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GroqApiService::class.java)
    }
}