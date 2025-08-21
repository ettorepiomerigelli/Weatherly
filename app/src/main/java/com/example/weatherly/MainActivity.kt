package com.example.weatherly

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.example.weatherly.api.RetrofitInstance
import com.example.weatherly.data.WeatherData
import com.example.weatherly.data.WeatherDatabase
import com.example.weatherly.helpers.LocationHelper
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var locationHelper: LocationHelper
    private lateinit var tvCity: TextView
    private lateinit var tvTemp: TextView
    private lateinit var tvDescription: TextView
    private lateinit var lottieWeather: LottieAnimationView
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var btnAddToHistory: MaterialButton

    private val apiKey = "8beded1cb9ef6b562666b4ed3b81f1a5"

    // Ultimi dati scaricati (non ancora salvati)
    private var lastCity: String? = null
    private var lastTemp: Double? = null
    private var lastDesc: String? = null
    private var lastlat: Double? = null
    private var lastlon: Double? = null


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) getLocationAndWeather()
        else tvCity.text = "❌ Permesso posizione negato"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Inizializza view
        mainLayout = findViewById(R.id.main)
        tvCity = findViewById(R.id.tvCity)
        tvTemp = findViewById(R.id.tvTemp)
        tvDescription = findViewById(R.id.tvDescription)
        lottieWeather = findViewById(R.id.lottieWeather)
        btnAddToHistory = findViewById(R.id.btnAddToHistory)
        val fabHistory = findViewById<FloatingActionButton>(R.id.fabHistory)

        locationHelper = LocationHelper(this)

        // Gestione insets (status bar, nav bar ecc.)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Pulsante aggiunta allo storico
        btnAddToHistory.setOnClickListener {
            saveWeatherToHistory()
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        // Pulsante floating per aprire lo storico
        fabHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // Controllo permesso posizione
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED -> getLocationAndWeather()
            else -> requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getLocationAndWeather() {
        lifecycleScope.launch {
            try {
                val location = locationHelper.getLastLocation()
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    lastlat = lat
                    lastlon = lon

                    val response = withContext(Dispatchers.IO) {
                        RetrofitInstance.api.getWeatherByLatLon(lat, lon, apiKey)
                    }

                    if (response.isSuccessful) {
                        val weather = response.body()
                        if (weather != null) {
                            val city = weather.name
                            val temp = weather.main.temp
                            val desc = weather.weather[0].description
                            val mainWeather = weather.weather[0].main.lowercase()

                            // Controlla se è giorno o notte
                            val currentTime = (System.currentTimeMillis() / 1000).toInt()
                            val isDayTime = currentTime in weather.sys.sunrise..weather.sys.sunset

                            // Traduzione + commento divertente
                            val translatedDesc = translateDescription(desc, mainWeather, isDayTime)
                            val funnyComment = getWeatherComment(mainWeather, !isDayTime)

                            // Aggiornamento UI
                            tvCity.text = city
                            tvTemp.text = "${temp.toInt()}°C"
                            tvDescription.text = "$translatedDesc\n$funnyComment"
                            tvDescription.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

                            // Ombre più marcate per leggibilità
                            val shadowColor = Color.BLACK
                            tvCity.setShadowLayer(8f, 2f, 2f, shadowColor)
                            tvTemp.setShadowLayer(8f, 2f, 2f, shadowColor)
                            tvDescription.setShadowLayer(6f, 1.5f, 1.5f, shadowColor)

                            // Lottie + sfondo in base al meteo + ora
                            val (lottieRes, bgRes) = when (mainWeather) {
                                "clear" -> if (isDayTime) R.raw.sun to R.drawable.bg_sun else R.raw.clear_night to R.drawable.bg_moon
                                "clouds" -> if (isDayTime) R.raw.cloud to R.drawable.bg_cloud else R.raw.cloud_night to R.drawable.bg_cloud_night
                                "rain" -> if (isDayTime) R.raw.rain to R.drawable.bg_rain else R.raw.rain_night to R.drawable.bg_rain_night
                                "snow" -> if (isDayTime) R.raw.snow to R.drawable.bg_snow else R.raw.snow_night to R.drawable.bg_snow_night
                                else -> if (isDayTime) R.raw.sun to R.drawable.bg_sun else R.raw.clear_night to R.drawable.bg_moon
                            }

                            lottieWeather.setAnimation(lottieRes)
                            lottieWeather.playAnimation()
                            mainLayout.setBackgroundResource(bgRes)

                            // Salva ultimo meteo caricato
                            lastCity = city
                            lastTemp = temp
                            lastDesc = translatedDesc
                        }
                    } else {
                        tvCity.text = "⚠️ Errore API: ${response.code()}"
                    }
                } else {
                    tvCity.text = "📡 Posizione non trovata"
                }
            } catch (e: Exception) {
                tvCity.text = "❌ Errore: ${e.localizedMessage}"
            }
        }
    }

    // Traduzioni base delle descrizioni meteo
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

    // Commenti divertenti coerenti giorno/notte + emoji 🌞🌙
    private fun getWeatherComment(mainWeather: String, isNight: Boolean): String {
        return when (mainWeather.lowercase()) {
            "clear" -> if (isNight) "Notte serena 🌙, sogni d’oro!" else "Sole splendente ☀️, goditelo!"
            "clouds" -> if (isNight) "Nuvole leggere 🌌, cielo tranquillo" else "Nuvole in azione ☁\uFE0F, sole in pausa."
            "rain" -> if (isNight) "Pioggia notturna 🌧️, romantico vero?" else "Pioggia leggera ☔, porta l’ombrello!"
            "snow" -> if (isNight) "Fiocchi di neve 🌨️ sotto la luna ❄️" else "Neve in arrivo ❄️, divertiti!"
            else -> if (isNight) "Cielo notturno 🌙, rilassati!" else "Cielo variabile 🌤️, giornata interessante!"
        }
    }

    //Salvataggio della località nel DB
    private fun saveWeatherToHistory() {
        if (lastCity != null && lastTemp != null && lastDesc != null) {
            lifecycleScope.launch (Dispatchers.IO) {
                val dao = WeatherDatabase.getDatabase(this@MainActivity).weatherDao()
                val existingWeather = dao.getByCity(lastCity!!)
                if (existingWeather == null) {
                    dao.insert(WeatherData(
                        city = lastCity!!,
                        temperature = lastTemp!!,
                        description = lastDesc!!,
                        lat = lastlat!!,
                        lon = lastlon!!
                    ))
                } else {
                    // città già presente → messaggio utente
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "ℹ️ ${lastCity} è già nello storico",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

}
