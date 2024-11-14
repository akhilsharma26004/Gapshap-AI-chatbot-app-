package com.example.gapshap

import com.example.gapshap.repository.weatherKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class WeatherRepository {

    // Ensure your API key is properly stored in the Constants object
    private val apiKey = weatherKey.weatherKey

    // Fetch weather data using a suspend function
    suspend fun getWeatherData(city: String): Map<String, String> {
        return withContext(Dispatchers.IO) {
            try {
                // Construct the API URL
                val url = URL("https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric")
                val connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000
                    connect()
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    // Parse the JSON response
                    val inputStream = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(inputStream)

                    // Extract the data you need
                    val description = jsonResponse.getJSONArray("weather")
                        .getJSONObject(0)
                        .getString("description")
                    val temperature = jsonResponse.getJSONObject("main").getDouble("temp")

                    // Clean up the connection
                    connection.disconnect()

                    return@withContext mapOf(
                        "description" to description,
                        "temperature" to temperature.toString()
                    )
                } else {
                    connection.disconnect()
                    throw Exception("Error: HTTP response code ${connection.responseCode}")
                }
            } catch (e: Exception) {
                throw Exception("Error fetching weather data: ${e.message}")
            }
        }
    }
}

// Optional standalone function to fetch weather data
suspend fun fetchWeatherData(city: String): Map<String, String> {
    return withContext(Dispatchers.IO) {
        try {
            // Construct the API URL
            val url = URL("https://api.openweathermap.org/data/2.5/weather?q=$city&appid=${weatherKey.weatherKey}&units=metric")
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                connect()
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                // Parse the JSON response
                val inputStream = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(inputStream)

                // Extract the data you need
                val description = jsonResponse.getJSONArray("weather")
                    .getJSONObject(0)
                    .getString("description")
                val temperature = jsonResponse.getJSONObject("main").getDouble("temp")

                // Clean up the connection
                connection.disconnect()

                return@withContext mapOf(
                    "description" to description,
                    "temperature" to temperature.toString()
                )
            } else {
                connection.disconnect()
                throw Exception("Error: HTTP response code ${connection.responseCode}")
            }
        } catch (e: Exception) {
            throw Exception("Error fetching weather data: ${e.message}")
        }
    }
}
