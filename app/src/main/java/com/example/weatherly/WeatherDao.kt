package com.example.weatherly.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Insert
    suspend fun insert(weather: WeatherData)

    @Query("SELECT * FROM weather_table ORDER BY timestamp DESC")
    fun getAllWeather(): Flow<List<WeatherData>>

    @Query("SELECT * FROM weather_table WHERE city = :city LIMIT 1")
    suspend fun getByCity(city: String): WeatherData?

    @Query("DELETE FROM weather_table")
    suspend fun deleteAll()
}
