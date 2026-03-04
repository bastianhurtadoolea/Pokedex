package com.example.pokedex

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path


interface PokeApiService {
    @GET("{name}")
    suspend fun getPokemonByName(@Path("name") pokemonName: String): Response<Pokemon>
}