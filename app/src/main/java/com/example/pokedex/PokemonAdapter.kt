package com.example.pokedex

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class PokemonAdapter(private val pokemonList: List<Pokemon>) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    class PokemonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPokemon: ImageView = view.findViewById(R.id.ivPokemon)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvInfo: TextView = view.findViewById(R.id.tvInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = pokemonList[position]
        holder.tvName.text = pokemon.name.uppercase()
        holder.tvInfo.text = "Altura: ${pokemon.height} | Peso: ${pokemon.weight}"

        // ¡Buscamos la imagen HD! Si no está, usamos la normal de respaldo
        val imageUrl = pokemon.sprites.other?.officialArtwork?.frontDefault ?: pokemon.sprites.frontDefault

        // Hacemos que la imagen se expanda para llenar el espacio
        holder.ivPokemon.scaleType = ImageView.ScaleType.FIT_CENTER

        Picasso.get().load(imageUrl).into(holder.ivPokemon)
    }

    override fun getItemCount() = pokemonList.size
}