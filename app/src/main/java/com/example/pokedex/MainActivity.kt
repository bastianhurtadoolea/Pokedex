package com.example.pokedex

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var btnSearch: ImageButton
    private lateinit var ivPokemon: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvFlavorText: TextView
    private lateinit var radarChart: RadarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Conectar vistas
        etSearch = findViewById(R.id.etSearch)
        btnSearch = findViewById(R.id.btnSearch)
        ivPokemon = findViewById(R.id.ivPokemon)
        tvName = findViewById(R.id.tvName)
        tvFlavorText = findViewById(R.id.tvFlavorText)
        radarChart = findViewById(R.id.radarChart)

        // 2. Configurar Retrofit para conectar a PokeAPI
        val retrofit = Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/pokemon/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(PokeApiService::class.java)

        // Inicializamos el radar vacío
        setupRadarChart(intArrayOf(0, 0, 0, 0, 0, 0))

        // 3. Acción al presionar el botón verde (Lupa)
        btnSearch.setOnClickListener {
            realizarBusqueda(api)
        }

        // 4. Acción al presionar "Enter" o "Buscar" en el teclado
        etSearch.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                realizarBusqueda(api)
                true
            } else {
                false
            }
        }
    }

    private fun realizarBusqueda(api: PokeApiService) {
        val query = etSearch.text.toString().trim().lowercase()
        if (query.isNotEmpty()) {
            searchPokemon(query, api)

            // Ocultar el teclado automáticamente para ver los resultados
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
            etSearch.clearFocus()

        } else {
            Toast.makeText(this, "Escribe el nombre de un Pokémon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchPokemon(name: String, api: PokeApiService) {
        tvName.text = "Buscando..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getPokemonByName(name)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        updateUI(response.body()!!)
                    } else {
                        Toast.makeText(this@MainActivity, "Pokémon no encontrado", Toast.LENGTH_SHORT).show()
                        tvName.text = "Error"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                    tvName.text = "Sin conexión"
                }
            }
        }
    }

    private fun updateUI(pokemon: Pokemon) {
        val imageUrl = pokemon.sprites.other?.officialArtwork?.frontDefault ?: pokemon.sprites.frontDefault
        Picasso.get().load(imageUrl).into(ivPokemon)
        tvName.text = pokemon.name.uppercase()

        val alturaMetros = pokemon.height / 10.0
        val pesoKg = pokemon.weight / 10.0

        val typesString = pokemon.types.joinToString(" / ") { it.type.name.uppercase() }

        val mainType = pokemon.types.firstOrNull()?.type?.name ?: "normal"
        val (fortalezas, debilidades) = getStrengthsAndWeaknesses(mainType)

        val infoText = """
            ALTURA: $alturaMetros m | PESO: $pesoKg kg
            
            TIPO: $typesString
            
            FUERTE CONTRA: 
            $fortalezas
            
            DÉBIL CONTRA: 
            $debilidades
        """.trimIndent()

        tvFlavorText.text = infoText

        val statsArray = IntArray(6)
        pokemon.stats.forEachIndexed { index, stat -> if(index < 6) statsArray[index] = stat.baseStat }
        setupRadarChart(statsArray)
    }

    private fun setupRadarChart(stats: IntArray) {
        val labels = arrayOf("HP", "Atk", "Def", "SpA", "SpD", "Spd")
        val entries = ArrayList<RadarEntry>()
        for (i in 0..5) entries.add(RadarEntry(stats[i].toFloat()))

        val dataSet = RadarDataSet(entries, "Stats")
        dataSet.color = Color.rgb(255, 100, 100)
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.argb(150, 255, 100, 100)
        dataSet.valueTextColor = Color.WHITE

        radarChart.data = RadarData(dataSet)

        radarChart.xAxis.textColor = Color.WHITE
        radarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        radarChart.yAxis.textColor = Color.WHITE
        radarChart.yAxis.textSize = 12f
        radarChart.yAxis.setAxisMinimum(0f)
        radarChart.yAxis.setAxisMaximum(150f)

        radarChart.description.isEnabled = false
        radarChart.invalidate()
    }

    private fun getStrengthsAndWeaknesses(type: String): Pair<String, String> {
        return when (type.lowercase()) {
            "fire" -> Pair("Planta, Bicho, Hielo", "Agua, Tierra, Roca")
            "water" -> Pair("Fuego, Tierra, Roca", "Eléctrico, Planta")
            "grass" -> Pair("Agua, Tierra, Roca", "Fuego, Hielo, Volador, Bicho")
            "electric" -> Pair("Agua, Volador", "Tierra")
            "normal" -> Pair("Ninguno", "Lucha")
            "fighting" -> Pair("Normal, Roca, Acero, Hielo, Siniestro", "Volador, Psíquico, Hada")
            "flying" -> Pair("Lucha, Bicho, Planta", "Roca, Eléctrico, Hielo")
            "poison" -> Pair("Planta, Hada", "Tierra, Psíquico")
            "ground" -> Pair("Fuego, Eléctrico, Veneno, Roca", "Agua, Planta, Hielo")
            "rock" -> Pair("Fuego, Hielo, Volador, Bicho", "Agua, Planta, Lucha, Tierra")
            "bug" -> Pair("Planta, Psíquico, Siniestro", "Fuego, Volador, Roca")
            "ghost" -> Pair("Psíquico, Fantasma", "Fantasma, Siniestro")
            "steel" -> Pair("Hielo, Roca, Hada", "Fuego, Lucha, Tierra")
            "psychic" -> Pair("Lucha, Veneno", "Bicho, Fantasma, Siniestro")
            "ice" -> Pair("Planta, Tierra, Volador, Dragón", "Fuego, Lucha, Roca, Acero")
            "dragon" -> Pair("Dragón", "Hielo, Dragón, Hada")
            "dark" -> Pair("Psíquico, Fantasma", "Lucha, Bicho, Hada")
            "fairy" -> Pair("Lucha, Dragón, Siniestro", "Veneno, Acero")
            else -> Pair("Variados", "Variados")
        }
    }
}