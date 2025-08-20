package com.example.weatherly.network

data class WeatherResponse(
    val main: Main,
    val weather: List<WeatherDescription>,
    val name: String,
    val sys: Sys
)

data class Main(
    val temp: Double
)

data class WeatherDescription(
    val main: String,          // "Clear", "Clouds", "Rain", ecc.
    val description: String    // "cielo sereno", "leggera pioggia", ecc.
)

data class Sys(
    val sunrise: Long,  // timestamp UNIX in secondi
    val sunset: Long    // timestamp UNIX in secondi
)
