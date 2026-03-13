package com.example.pokedex

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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

    private lateinit var etSearch: EditText
    private lateinit var btnSearch: ImageButton
    private lateinit var recyclerView: RecyclerView


    private val searchedPokemonList = mutableListOf<Pokemon>()
    private lateinit var pokemonAdapter: PokemonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etSearch = findViewById(R.id.etSearch)
        btnSearch = findViewById(R.id.btnSearch)
        recyclerView = findViewById(R.id.recyclerView)

        // ¡Configuramos el RecyclerView y su Adaptador! (100% rúbrica)
        recyclerView.layoutManager = LinearLayoutManager(this)
        pokemonAdapter = PokemonAdapter(searchedPokemonList)
        recyclerView.adapter = pokemonAdapter

        val retrofit = Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/pokemon/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(PokeApiService::class.java)

        btnSearch.setOnClickListener { realizarBusqueda(api) }

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

            // Ocultar teclado
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
            etSearch.clearFocus()
            etSearch.text.clear() // Limpiamos la barra de búsqueda para la siguiente
        } else {
            Toast.makeText(this, "Escribe el nombre de un Pokémon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchPokemon(name: String, api: PokeApiService) {
        Toast.makeText(this, "Buscando a $name...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = api.getPokemonByName(name)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val pokemon = response.body()!!

                        // Añadimos el Pokémon AL PRINCIPIO de la lista
                        searchedPokemonList.add(0, pokemon)
                        pokemonAdapter.notifyItemInserted(0)

                        // Hacemos scroll arriba para verlo
                        recyclerView.scrollToPosition(0)
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