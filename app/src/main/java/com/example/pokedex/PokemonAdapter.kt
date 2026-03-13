package com.example.pokedex

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.squareup.picasso.Picasso

class PokemonAdapter(private val pokemonList: List<Pokemon>) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    class PokemonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPokemon: ImageView = view.findViewById(R.id.ivPokemon)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvFlavorText: TextView = view.findViewById(R.id.tvFlavorText)
        val radarChart: RadarChart = view.findViewById(R.id.radarChart) // Conectamos el gráfico
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = pokemonList[position]

        holder.tvName.text = pokemon.name.uppercase()

        // Cargamos la imagen
        val imageUrl = pokemon.sprites.other?.officialArtwork?.frontDefault ?: pokemon.sprites.frontDefault
        Picasso.get().load(imageUrl).into(holder.ivPokemon)

        // Textos del cuadro verde
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

        holder.tvFlavorText.text = infoText

        // Extraemos los stats para el gráfico
        val statsArray = IntArray(6)
        pokemon.stats.forEachIndexed { index, stat -> if(index < 6) statsArray[index] = stat.baseStat }

        // Dibujamos el gráfico para este Pokémon específico
        setupRadarChart(holder.radarChart, statsArray)
    }

    override fun getItemCount() = pokemonList.size

    // Configurador del gráfico dentro del adaptador
    private fun setupRadarChart(radarChart: RadarChart, stats: IntArray) {
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
        radarChart.yAxis.textSize = 10f
        radarChart.yAxis.setAxisMinimum(0f)
        radarChart.yAxis.setAxisMaximum(150f)

        radarChart.description.isEnabled = false
        radarChart.legend.textColor = Color.WHITE
        radarChart.invalidate()
    }

    // Traductor de debilidades
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