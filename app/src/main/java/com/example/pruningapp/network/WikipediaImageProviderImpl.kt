package com.example.pruningapp.network

import android.content.Context
import android.util.Log
import com.example.pruningapp.domain.WikipediaImageProvider
import com.example.pruningapp.remote.WikipediaApiService
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import retrofit2.HttpException

// Implementacja jest internal — żadna warstwa poza tym pakietem nie może
// zinstancjonować tej klasy bezpośrednio. Jedynym punktem dostępu jest
// interfejs WikipediaImageProvider tworzony przez fabrykę w App.kt.
internal class WikipediaImageProviderImpl(context: Context) : WikipediaImageProvider {

    private val api = WikipediaApiService.instance
    private val fallbacks: List<FallbackEntry> = loadFallbacks(context)

    private data class FallbackEntry(
        @SerializedName("keyword") val keyword: String,
        @SerializedName("latinName") val latinName: String
    )

    private fun loadFallbacks(context: Context): List<FallbackEntry> = try {
        val json = context.assets.open("wiki_fallbacks.json").bufferedReader().readText()
        val type = object : TypeToken<List<FallbackEntry>>() {}.type
        Gson().fromJson(json, type)
    } catch (e: Exception) {
        Log.w(TAG, "Nie można wczytać wiki_fallbacks.json: ${e.message}")
        emptyList()
    }

    // 5-etapowy silnik wyszukiwania obrazów — każdy etap jest próbowany
    // sekwencyjnie; sukces na dowolnym etapie przerywa dalsze wyszukiwanie.
    override suspend fun fetchImageUrl(polishName: String, latinName: String?): String? {
        Log.d(TAG, "Szukanie obrazu: '$polishName' (łacina: $latinName)")

        // Etap 1 — mapowanie botaniczne: łacińska nazwa → EN Wikipedia
        if (latinName != null) {
            val normalizedLatin = latinName.replace("×", "x")
            fetch(normalizedLatin, EN_WIKI)?.let { return it }
        }

        // Etap 2 — lokalizacja: pełna polska nazwa → PL Wikipedia
        fetch(polishName, PL_WIKI)?.let { return it }

        // Etap 3 — normalizacja: usunięcie nawiasów i znaków specjalnych → PL Wikipedia
        val normalized = BRACKETS_REGEX.replace(polishName, "").trim()
        if (normalized != polishName) {
            fetch(normalized, PL_WIKI)?.let { return it }
        }

        // Etap 4 — redukcja frazy: tylko pierwszy człon (rodzaj) → PL Wikipedia
        val genus = normalized.split(" ").first()
        if (genus != normalized) {
            fetch(genus, PL_WIKI)?.let { return it }
        }

        // Etap 5 — generyczny fallback: słownik wiki_fallbacks.json → EN Wikipedia
        val fallbackLatin = fallbacks
            .firstOrNull { polishName.contains(it.keyword, ignoreCase = true) }
            ?.latinName
        if (fallbackLatin != null) {
            fetch(fallbackLatin, EN_WIKI)?.let { return it }
        }

        Log.w(TAG, "Brak obrazu dla '$polishName' po wszystkich etapach.")
        return null
    }

    private suspend fun fetch(title: String, endpoint: String): String? {
        return try {
            val response = api.getPageImages(url = endpoint, titles = title)
            val url = response.query?.pages?.values
                ?.firstOrNull { (it.pageId ?: -1) > 0 }
                ?.thumbnail?.source
            if (url != null) Log.d(TAG, "Znaleziono obraz '$title': $url")
            url
        } catch (e: HttpException) {
            handleHttpError(e, title, endpoint)
        } catch (e: Exception) {
            Log.e(TAG, "Błąd pobierania '$title': ${e.message}")
            null
        }
    }

    // Anti-throttling: odczyt nagłówka Retry-After i inteligentne wstrzymanie.
    // Po oczekiwaniu wykonywana jest jedna próba ponowna przed ostatecznym poddaniem się.
    private suspend fun handleHttpError(e: HttpException, title: String, endpoint: String): String? {
        if (e.code() != 429) {
            Log.e(TAG, "HTTP ${e.code()} dla '$title'")
            return null
        }
        val retryAfterSec = e.response()?.headers()?.get("Retry-After")?.toLongOrNull() ?: DEFAULT_RETRY_DELAY_SEC
        Log.w(TAG, "Rate limit (429) dla '$title'. Czekam ${retryAfterSec}s...")
        delay(retryAfterSec * 1_000)
        return try {
            val response = api.getPageImages(url = endpoint, titles = title)
            response.query?.pages?.values
                ?.firstOrNull { (it.pageId ?: -1) > 0 }
                ?.thumbnail?.source
        } catch (e2: Exception) {
            Log.e(TAG, "Ponowna próba nieudana dla '$title': ${e2.message}")
            null
        }
    }

    companion object {
        private const val TAG = "WikipediaImageProvider"
        private const val EN_WIKI = "https://en.wikipedia.org/w/api.php"
        private const val PL_WIKI = "https://pl.wikipedia.org/w/api.php"
        private const val DEFAULT_RETRY_DELAY_SEC = 60L
        private val BRACKETS_REGEX = Regex("""\(.*?\)""")

        // Fabryka ukrywa klasę implementacyjną przed resztą modułu.
        fun create(context: Context): WikipediaImageProvider =
            WikipediaImageProviderImpl(context)
    }
}
