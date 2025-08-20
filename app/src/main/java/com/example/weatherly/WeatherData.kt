package com.example.weatherly.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_table")
data class WeatherData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val city: String,
    val temperature: Double,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val lat: Double,
    val lon: Double
)
