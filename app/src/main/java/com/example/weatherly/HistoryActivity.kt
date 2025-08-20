package com.example.weatherly

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherly.data.WeatherDatabase
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryActivity : AppCompatActivity() {

    private lateinit var adapter: WeatherAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

        val toolbar = findViewById<Toolbar>(R.id.toolbarHistory)
        setSupportActionBar(toolbar)
        supportActionBar?.title=""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Rimuovo il titolo standard della Toolbar
        toolbar.title = ""

        // Creo il TextView per il titolo
        val title = TextView(this).apply {
            text = "Weatherly"
            setTextColor(ContextCompat.getColor(context, R.color.black))
            textSize = 32f
            typeface = ResourcesCompat.getFont(context, R.font.roboto)
            setTypeface(typeface, Typeface.BOLD)
            setShadowLayer(4f, 2f, 2f, 0x80000000.toInt())
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }

        // LayoutParams per posizionarlo vicino alla freccia e occupare lo spazio residuo
        val params = Toolbar.LayoutParams(
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        ).apply {
            marginStart = 16 // distanza dalla freccia
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        title.layoutParams = params

        // Aggiungo il TextView alla Toolbar
        toolbar.addView(title)

        // Bottone per svuotare lo storico
        val btnClearHistory = findViewById<MaterialButton>(R.id.btnClearHistory)
        btnClearHistory.setOnClickListener {
            lifecycleScope.launch {
                WeatherDatabase.getDatabase(this@HistoryActivity).weatherDao().deleteAll()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@HistoryActivity,
                        "Lo storico è stato svuotato",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // RecyclerView
        val rvWeather = findViewById<RecyclerView>(R.id.rvWeather)
        adapter = WeatherAdapter(emptyList())
        rvWeather.layoutManager = LinearLayoutManager(this)
        rvWeather.adapter = adapter

        // Carico i dati dallo storico
        val dao = WeatherDatabase.getDatabase(this).weatherDao()
        lifecycleScope.launch {
            dao.getAllWeather().collect { list ->
                adapter.setData(list)
            }
        }
    }

    //Traduzione descrizione meteo
    private fun translateDescription(desc: String, mainWeather: String, isDay: Boolean): String {
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


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
