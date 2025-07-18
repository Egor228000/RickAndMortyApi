package com.example.rickandmortyapi.screens

import MainViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rickandmortyapi.ApiClient
import com.example.rickandmortyapi.R
import com.example.rickandmortyapi.dao.CharacterRepository
import com.example.rickandmortyapi.data.DetailCharacterData
import com.example.rickandmortyapi.data.LocationDetail
import com.skydoves.landscapist.coil.CoilImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    id: Int,
    onBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    mainViewModel: MainViewModel,
    repository: CharacterRepository
) {
    var location by remember { mutableStateOf<LocationDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(id) {
        if (mainViewModel.hasInternetConnection(context)) {
            val loc = ApiClient.getLocationById(id)
            repository.cacheLocation(loc)
            location = loc
        } else {
            location = repository.getCachedLocation(id)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = location?.name ?: "Location") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_24),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when {
                    !isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    error != null -> Text(
                        text = "Error: $error",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    location != null -> LocationContent(
                        location = location!!,
                        onNavigateToDetail = onNavigateToDetail
                    )
                }
            }
        }
    )
}

@Composable
fun LocationContent(
    location: LocationDetail,
    onNavigateToDetail: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Type: ${location.type}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Dimension: ${location.dimension}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Residents", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(location.residents) { url ->
                val residentId = url.substringAfterLast("/").toIntOrNull() ?: return@items
                CharacterImageCard(
                    id = residentId,
                    onNavigateToDetail = onNavigateToDetail
                )
            }
        }
    }
}

@Composable
fun CharacterImageCard(
    id: Int,
    onNavigateToDetail: (Int) -> Unit
) {
    val character by produceState<DetailCharacterData?>(initialValue = null, key1 = id) {
        value = try {
            ApiClient.getCharacterById(id)
        } catch (e: Exception) {
            null
        }
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .size(width = 200.dp, height = 380.dp)
            .clickable { onNavigateToDetail(id) }
    ) {
        if (character != null) {
            CoilImage(
                imageModel = { character!!.image },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        }
    }
}
