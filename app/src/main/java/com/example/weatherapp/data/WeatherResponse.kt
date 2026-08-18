package com.example.weatherapp.data

data class WeatherResponse (
    val name: String,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind
)

data class Main(
    val temp: Double, // current temp
    val humidity: Int // humidity %
)

data class Weather(
    val description: String // Clear Weather
)

data class Wind(
    val speed: Double // wind speed in meters per second
)