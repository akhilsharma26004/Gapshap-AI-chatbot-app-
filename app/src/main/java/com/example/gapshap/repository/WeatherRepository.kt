package com.example.gapshap.repository

import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

// Initialize OkHttpClient
private val client = OkHttpClient()

// Function to fetch weather data from OpenWeather API
fun fetchWeatherData(
    location: String,
    apiKey: String,
    onResult: (JSONObject?) -> Unit
) {
    val url = "https://api.openweathermap.org/data/2.5/weather?q=$location&appid=$apiKey&units=metric"
    val request = Request.Builder().url(url).build()

    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            e.printStackTrace()
            onResult(null)
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            response.use {
                if (!it.isSuccessful) {
                    println("Failed to fetch weather data: ${it.message}")
                    onResult(null)
                } else {
                    response.body?.let { responseBody ->
                        val json = JSONObject(responseBody.string())
                        onResult(json)
                    } ?: onResult(null)
                }
            }
        }
    })
}

// Function to store weather data in Firebase Firestore
fun storeWeatherData(
    location: String,
    weatherData: JSONObject
) {
    val firestore = FirebaseFirestore.getInstance()
    val weatherCollection = firestore.collection("Weather")

    // Extract weather details safely
    val mainData = weatherData.optJSONObject("main")
    val weatherArray = weatherData.optJSONArray("weather")

    val temperature = mainData?.optDouble("temp") ?: 0.0
    val condition = weatherArray?.optJSONObject(0)?.optString("description") ?: "Unknown"
    val timestamp = System.currentTimeMillis()

    // Create data map to store
    val weatherInfo = mapOf(
        "location" to location,
        "temperature" to temperature,
        "condition" to condition,
        "timestamp" to timestamp
    )

    // Store data in Firestore
    weatherCollection.document(location).set(weatherInfo)
        .addOnSuccessListener { println("Weather data for $location stored successfully.") }
        .addOnFailureListener { e -> println("Error storing weather data for $location: $e") }
}
