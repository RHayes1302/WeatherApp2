package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//Class 1

import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import com.example.weatherapp.data.Weather
import com.example.weatherapp.network.AppConstants
import com.example.weatherapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
// Class 2

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WeatherScreen()
                }
            }
        }
    }
}

@Composable
fun WeatherScreen() {
    var city by remember { mutableStateOf("") }

    var cityResult by remember { mutableStateOf("city:--") }
    var temResult by remember { mutableStateOf("Tempature: --") }
    var descResult by remember { mutableStateOf("Description:--") }
    var windResult by remember { mutableStateOf("Wind Speed:--") }
    var humidityResult by remember { mutableStateOf("Humidity") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current // Toast.makeText() requires Android context
    val scope = rememberCoroutineScope() // Step 1: needed to launch a coroutine from onClick

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("Enter city name") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            enabled = !isLoading,
            onClick = {
                val trimmedCity = city.trim()
                if (trimmedCity.isEmpty()) {
                    Toast.makeText(context, "Please enter a city name", Toast.LENGTH_SHORT).show()
                } else {
                    isLoading = true
                    // Step 2: launch a coroutine tied to this composable's lifecycle
                    scope.launch {
                        // Step 7: wrap the network call and result handling in try/catch
                        try {
                            // Step 3: move the actual network call off the main thread
                            val response = withContext(Dispatchers.IO) {
                                // Step 4: make the API call
                                RetrofitClient.weatherApiService.getWeather(
                                    trimmedCity,
                                    AppConstants.API_KEY,
                                    AppConstants.Unit
                                )
                            }
                            Log.d("WeatherApp", "Request URL: ${response.raw().request.url}")
                            Log.d("WeatherApp", "Response Code: ${response.code()}")

                            // Step 5: update the UI state on success
                            if (response.isSuccessful) {
                                val weather = response.body()
                                if (weather != null) {
                                    cityResult = "City: ${weather.name}"
                                    temResult = "Tempature: ${weather.main.temp}"
                                    descResult =
                                        "Description: ${weather.weather.firstOrNull()?.description ?: "--"}"
                                    // ASSIGNMENT 2, step 1: wind speed
                                    windResult = "Wind Speed: ${weather.wind.speed}m/s"
                                    // ASSIGNMENT 2, step 2: humidity
                                    humidityResult = "Humidity: ${weather.main.humidity}%"
                                }
                            } else {
                                // Step 6: not successful (e.g. city not found)
                                Log.e(
                                    "WeatherApp",
                                    "HTTP error: ${response.code()} ${
                                        response.errorBody()?.string()
                                    }"
                                )
                                // ASSIGNMENT 2, step 3: differentiate error messages by status code
                                val message = when (response.code()) {
                                    404 -> "City not found. Check the name and try again."
                                    401 -> "Invalid API key. Please check your configuration."
                                    429 -> "Too many requests. Please wait a moment and try again."
                                    else -> "Something went wrong (code ${response.code()})."
                                }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            // Step 7: network/connection failure
                            Log.e("WeatherApp", "Network call failed", e)
                            Toast.makeText(
                                context,
                                "Network error. Check your connection.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } finally {
                            isLoading = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text(if (isLoading) "Loading..." else "Get Weather")
        }
        Text(cityResult, fontSize = 20.sp, modifier = Modifier.padding(top = 24.dp))
        Text(temResult, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
        Text(descResult, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
        Text(windResult, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
        Text(humidityResult, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
    }
}