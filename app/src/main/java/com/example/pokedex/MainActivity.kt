package com.example.pokedex

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: PokemonAdapter
    private val pokemonList = mutableListOf<Pokemon>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSearch = findViewById<ImageButton>(R.id.btnSearch)
        val etPokemonName = findViewById<EditText>(R.id.etPokemonName)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        adapter = PokemonAdapter(pokemonList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Acción de la lupa manual
        btnSearch.setOnClickListener {
            val name = etPokemonName.text.toString().trim().lowercase()
            if (name.isNotEmpty()) {
                searchPokemon(name)
            }
        }

        etPokemonName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                btnSearch.performClick() // Simula que se toca la lupa
                true
            } else {
                false
            }
        }
    }

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/pokemon/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun searchPokemon(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = getRetrofit().create(PokeApiService::class.java).getPokemonByName(name)
                val pokemon = call.body()

                withContext(Dispatchers.Main) {
                    if (call.isSuccessful && pokemon != null) {
                        pokemonList.clear() // <-- MAGIA NUEVA: Borra la pantalla anterior
                        pokemonList.add(pokemon) // Agrega al nuevo Pokémon
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@MainActivity, "Pokémon no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}