package com.example.rickandmortyapi

import com.example.rickandmortyapi.data.CharacterData
import com.example.rickandmortyapi.data.CharactersResponse
import com.example.rickandmortyapi.data.DetailCharacterData
import com.example.rickandmortyapi.data.FilterState
import com.example.rickandmortyapi.data.LocationDetail
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json

object ApiClient {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun getCharacters(
        page: Int,
        filters: FilterState? = null,
        name: String? = null
    ): CharactersResponse {
        return client.get("https://rickandmortyapi.com/api/character") {
            parameter("page", page)
            name?.takeIf { it.isNotBlank() }?.let { parameter("name", it) }

            filters?.status?.let { parameter("status", it) }
            filters?.gender?.let { parameter("gender", it) }
            filters?.species?.let { parameter("species", it) }
        }.body()
    }

    suspend fun getCharacterById(id: Int): DetailCharacterData =
        client.get("https://rickandmortyapi.com/api/character/$id").body()

    suspend fun getLocationById(id: Int): LocationDetail =
        client.get("https://rickandmortyapi.com/api/location/$id").body()
}


class CharacterPaginator(
    private val onLoadUpdated: (Boolean) -> Unit,
    private val onRequest: suspend (page: Int) -> CharactersResponse,
    private val onSuccess: (List<CharacterData>, Boolean) -> Unit,
    private val onError: (Throwable?) -> Unit
) {
    private var currentPage = 1
    private var totalPages = Int.MAX_VALUE
    private var isMakingRequest = false

    suspend fun loadNextPage() {
        if (isMakingRequest || currentPage > totalPages) return

        isMakingRequest = true
        onLoadUpdated(true)

        try {
            val result = onRequest(currentPage)
            totalPages = result.info.pages
            onSuccess(result.results, currentPage >= totalPages)
            currentPage++
        } catch (e: Exception) {
            onError(e)
        } finally {
            isMakingRequest = false
            onLoadUpdated(false)
        }
    }

    fun reset() {
        currentPage = 1
        totalPages = Int.MAX_VALUE
    }
}
