package com.example.weatherly

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherly.data.WeatherData
import java.text.SimpleDateFormat
import java.util.*

class WeatherAdapter(private var weatherList: List<WeatherData>) :
    RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>() {

    class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCity: TextView = itemView.findViewById(R.id.tvCity)
        val tvTemp: TextView = itemView.findViewById(R.id.tvTemp)
        val tvDesc: TextView = itemView.findViewById(R.id.tvDesc)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val ivArrow: ImageView = itemView.findViewById(R.id.ivArrow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weather, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val weather = weatherList[position]
        holder.tvCity.text = weather.city
        holder.tvTemp.text = "${weather.temperature} °C"
        holder.tvDesc.text = translateDescription(weather.description)

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.tvTimestamp.text = sdf.format(Date(weather.timestamp))

        // Animazione
        val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.slide_in_bottom)
        holder.itemView.startAnimation(animation)

        // Clic sulla freccia → apre SecondActivity con city + lat + lon
        holder.ivArrow.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, SecondActivity::class.java).apply {
                putExtra("city", weather.city)
                putExtra("lat", weather.lat)
                putExtra("lon", weather.lon)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = weatherList.size

    fun setData(newList: List<WeatherData>) {
        weatherList = newList
        notifyDataSetChanged()
    }

    private fun translateDescription(desc: String): String {
        val map = mapOf(
            "clear sky" to "Cielo sereno",
            "few clouds" to "Poche nuvole",
            "scattered clouds" to "Nuvolosità sparsa",
            "broken clouds" to "Nubi sparse",
            "overcast clouds" to "Cielo coperto",
            "light rain" to "Pioggia leggera",
            "moderate rain" to "Pioggia moderata",
            "heavy intensity rain" to "Pioggia intensa",
            "snow" to "Neve",
            "light snow" to "Neve leggera",
            "heavy snow" to "Neve abbondante",
            "mist" to "Nebbia",
            "thunderstorm" to "Temporale"
        )
        return map[desc.lowercase()] ?: desc.replaceFirstChar { it.uppercase() }
    }
}
