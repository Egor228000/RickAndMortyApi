import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rickandmortyapi.ApiClient
import com.example.rickandmortyapi.CharacterPaginator
import com.example.rickandmortyapi.dao.CharacterRepository
import com.example.rickandmortyapi.data.CharacterData
import com.example.rickandmortyapi.data.FilterState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: CharacterRepository
) : ViewModel() {

    private val _allCharacters = MutableStateFlow<List<CharacterData>>(emptyList())

    private val _listCharacter = MutableStateFlow<List<CharacterData>>(emptyList())
    val listCharacter: StateFlow<List<CharacterData>> = _listCharacter

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState

    private val paginator = CharacterPaginator(
        onLoadUpdated = { _isLoading.value = it },
        onRequest      = { page -> ApiClient.getCharacters(page = page, filters = null, name = _searchQuery.value) },
        onSuccess      = { newPage, _last ->
            val updated = _allCharacters.value + newPage
            _allCharacters.value = updated
            viewModelScope.launch { repository.cacheCharacters(updated) }
        },
        onError        = { Log.e("Paginator", "load error", it) }
    )

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }
    fun clearFilters() {
        _filterState.value = FilterState()
    }
    init {
        viewModelScope.launch {
            repository.getCachedCharacters().takeIf { it.isNotEmpty() }?.let {
                _allCharacters.value = it
            } ?: paginator.loadNextPage()
        }

        viewModelScope.launch {
            combine(_allCharacters, _searchQuery, _filterState) { all, query, f ->
                all.filter { c ->
                    (f.status.isEmpty()  || c.status  in f.status) &&
                            (f.species.isEmpty() || c.species in f.species) &&
                            (f.gender.isEmpty()  || c.gender  in f.gender) &&
                            (query.isBlank()     || c.name.contains(query, ignoreCase = true))
                }
            }.collect { _listCharacter.value = it }
        }
    }

    fun loadNextCharacters() {
        viewModelScope.launch { paginator.loadNextPage() }
    }

    fun resetAll() {
        paginator.reset()
        _allCharacters.value = emptyList()
        viewModelScope.launch {
            repository.clearCache()
            paginator.loadNextPage()
        }
    }

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun updateFilters(newFilters: FilterState) {
        _filterState.value = newFilters
    }


    fun hasInternetConnection(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val active = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(active) ?: return false
        return caps.run {
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        }
    }
}
