package com.example.pokedex

import com.google.gson.annotations.SerializedName

data class Pokemon(
    val name: String,
    val height: Int,
    val weight: Int,
    val sprites: Sprites,
    val stats: List<StatWrapper>,
    val types: List<TypeWrapper> // <-- AQUÍ RECIBIMOS LOS TIPOS REALES
)

data class Sprites(
    @SerializedName("front_default")
    val frontDefault: String,
    val other: OtherSprites?
)

data class OtherSprites(
    @SerializedName("official-artwork")
    val officialArtwork: OfficialArtwork?
)

data class OfficialArtwork(
    @SerializedName("front_default")
    val frontDefault: String
)

// --- CLASES PARA ESTADÍSTICAS ---
data class StatWrapper(
    @SerializedName("base_stat")
    val baseStat: Int,
    val stat: StatInfo
)
data class StatInfo(
    val name: String
)

// --- NUEVAS CLASES PARA TIPOS ---
data class TypeWrapper(
    val type: TypeInfo
)
data class TypeInfo(
    val name: String
)