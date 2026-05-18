package com.example.pruningapp.domain

// Publiczny kontrakt warstwy pobierania mediów z Wikipedii.
// Implementacja jest ukryta w pakiecie network (modyfikator internal),
// dzięki czemu warstwa UI i repozytorium nigdy nie zależą od szczegółów
// sieciowych (OkHttp, Retrofit, logika fallbacków).
interface WikipediaImageProvider {

    // Zwraca URL zdjęcia lub null jeśli żaden etap fallbacku nie znalazł obrazu.
    // Wywołanie jest bezpieczne wątkowo — działaj wyłącznie na Dispatchers.IO.
    suspend fun fetchImageUrl(polishName: String, latinName: String?): String?
}
