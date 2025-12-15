package id.example.wastify.data

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// --- Weather Models ---
data class WeatherResponse(
    @SerializedName("current_weather") val currentWeather: CurrentWeather
)

data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val weathercode: Int
)

// --- Weather API Client (Separate from your Python Backend) ---
object WeatherClient {
    private const val BASE_URL = "https://api.open-meteo.com/"

    val service: WeatherService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)
    }
}

interface WeatherService {
    // Hardcoded to South Tangerang/Jakarta coordinates for demo
    @GET("v1/forecast?latitude=-6.29&longitude=106.73&current_weather=true")
    suspend fun getWeather(): WeatherResponse
}

// --- Static Quotes ---
val EnvironmentalQuotes = listOf(
    "The greatest threat to our planet is the belief that someone else will save it.",
    "Waste isn't waste until we waste it.",
    "There is no such thing as 'away'. When we throw anything away it must go somewhere.",
    "Refuse what you do not need; reduce what you do need.",
    "Clean up the earth, it's the only home we have."
)