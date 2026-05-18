package com.example.pruningapp.ui.components

// Abstrakcja danych wizualnych karty — oddziela komponent MagazineCard
// od konkretnych modeli biznesowych (Plant, EncyclopediaSpecies).
// Dzięki temu MagazineCard może trafić do modułu :core:ui bez żadnych
// zależności na warstwę danych.
interface CardDisplayable {
    val title: String
    val subtitle: String
    val imageUrl: String?
    val category: String
}

// Lekki, niemutowalny kontener implementujący CardDisplayable.
// Ekrany tworzą go przez extension functions zamiast przekazywać
// surowe encje Room lub DTO do komponentów UI.
data class PlantCardItem(
    override val title: String,
    override val subtitle: String,
    override val imageUrl: String?,
    override val category: String
) : CardDisplayable
