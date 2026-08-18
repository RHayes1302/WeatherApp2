package com.example.weatherapp.network


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// object = singleton - only one RetrofitClient exists in the entier app
object RetrofitClient {
    // "by lazy" = create this only when it is first accessed
    val weatherApiService: WeatherApiService by lazy{
        Retrofit.Builder()
            .baseUrl(AppConstants.WEATHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}