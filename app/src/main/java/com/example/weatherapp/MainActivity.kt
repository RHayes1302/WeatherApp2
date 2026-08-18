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
import com.example.weatherapp.network.AppConstants
import com.example.weatherapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            onClick = {
                val trimmedCity = city.trim()
                if (trimmedCity.isEmpty()) {
                    Toast.makeText(context, "Please enter a city name", Toast.LENGTH_SHORT).show()
                } else {
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

                            // Step 5: update the UI state on success
                            if (response.isSuccessful) {
                                val weather = response.body()
                                if (weather != null) {
                                    cityResult = "City: ${weather.name}"
                                    temResult = "Tempature: ${weather.main.temp}"
                                    descResult = "Description: ${weather.weather.firstOrNull()?.description ?: "--"}"
                                }
                            } else {
                                // Step 6: not successful (e.g. city not found)
                                Log.e("WeatherApp", "HTTP error: ${response.code()} ${response.errorBody()?.string()}")
                                Toast.makeText(
                                    context,
                                    "City not found. Check the name and try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            // Step 7: network/connection failure
                            Log.e("WeatherApp", "Network call failed", e)
                            Toast.makeText(
                                context,
                                "Network error. Check your connection.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Get Weather")
        }
        Text(cityResult, fontSize = 20.sp, modifier = Modifier.padding(top = 24.dp))
        Text(temResult, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
        Text(descResult, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
    }
}