package com.example.rickandmortyapi.screens

import MainViewModel
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.rickandmortyapi.ApiClient
import com.example.rickandmortyapi.R
import com.example.rickandmortyapi.dao.CharacterRepository
import com.example.rickandmortyapi.data.DetailCharacterData
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    id: Int,
    onBack: () -> Unit,
    onNavigateToLocationOrigin: (Int) -> Unit,
    onNavigateToLocation: (Int) -> Unit,
    repository: CharacterRepository,
    mainViewModel: MainViewModel

) {
    val context = LocalContext.current
    var character by remember { mutableStateOf<DetailCharacterData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val originId = character?.origin?.url
        ?.substringAfterLast("/")
        ?.toIntOrNull()
    val locationId = character?.location?.url
        ?.substringAfterLast("/")
        ?.toIntOrNull()
    LaunchedEffect(id) {
        isLoading = true
        try {
            character = if (mainViewModel.hasInternetConnection(context)) {
                ApiClient.getCharacterById(id).also {
                    repository.cacheCharacterDetail(it)
                    Log.d("sad", character?.location?.url.toString())

                }
            } else {
                repository.getCharacterDetail(id)
                    ?: throw Exception("No cached data and no network")
            }
        } catch (e: Exception) {
            error = e.localizedMessage ?: "Unknown error"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = character?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painterResource(R.drawable.outline_arrow_back_24),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                error != null -> Text(
                    text = error!!,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )

                character != null -> DetailContent(
                    character!!,
                    onNavigateToLocation = {
                        locationId?.let(onNavigateToLocation)

                    },
                    onNavigateToLocationOrigin = {
                        originId?.let(onNavigateToLocationOrigin)


                    }
                )
            }
        }
    }
}

@Composable
private fun DetailContent(
    character: DetailCharacterData,
    onNavigateToLocationOrigin: () -> Unit,
    onNavigateToLocation: () -> Unit,

    ) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Box {
                CoilImage(
                    imageModel = { character.image },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    imageOptions = ImageOptions(contentScale = ContentScale.Crop)
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                0.7f to Color.Black.copy(alpha = 0.6f)
                            )
                        )
                )
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }
        }
        val iconSpecies: Int = when (character.species) {
            "Human" -> R.drawable.outline_emoji_people_24
            "Alien" -> R.drawable.alien_head_icon_123829
            "Mythological Creature" -> R.drawable.monster_icon_216742
            "Robot" -> R.drawable.outline_robot_24
            "Humanoid" -> R.drawable.humanoid_svgrepo_com
            else -> R.drawable.baseline_hide_image_24//
        }
        val iconGender: Int = when (character.gender) {
            "Female" -> R.drawable.femalegendersymbol_99762
            "Male" -> R.drawable.gender_male_icon_137554
            "Genderless" -> R.drawable.genderless_symbol_icon_205866
            else -> R.drawable.filled_unknown_icon_217176
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusChip(status = character.status)
                InfoChip(painterResource(id = iconSpecies), text = character.species)
                InfoChip(painterResource(iconGender), text = character.gender)
            }
        }

        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(8.dp))
                TwoColumnInfo(
                    label1 = "Origin",
                    value1 = character.origin.name,
                    label2 = "Location",
                    value2 = character.location.name,
                    onNavigateToLocationOrigin = { onNavigateToLocationOrigin() },
                    onNavigateToLocation = { onNavigateToLocation() }
                )
                if (character.type.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    TwoColumnInfo(
                        label1 = "Type",
                        value1 = character.type,
                        label2 = "Episodes",
                        value2 = character.episode.size.toString(),
                        onNavigateToLocationOrigin = { onNavigateToLocationOrigin() },
                        onNavigateToLocation = { onNavigateToLocation() }
                    )

                }
            }
        }
        item {
            Text(
                text = "Episodes",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
        }
        items(character.episode) { url ->
            val epNum = url.substringAfterLast("/").toIntOrNull() ?: url
            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#$epNum", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Episode $epNum",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val color = when (status.lowercase()) {
        "alive" -> Color(0xFF4CAF50)
        "dead" -> Color(0xFFF44336)
        else -> Color(0xFF9E9E9E)
    }
    AssistChip(
        onClick = { /*noop*/ },
        label = { Text(text = status, color = color) },
        leadingIcon = { }
    )
}

@Composable
private fun InfoChip(icon: Painter, text: String) {
    AssistChip(
        onClick = { /*noop*/ },
        label = { Text(text = text) },
        leadingIcon = { Icon(icon, contentDescription = null) }
    )
}

@Composable
private fun TwoColumnInfo(
    label1: String, value1: String,
    label2: String, value2: String,
    onNavigateToLocationOrigin: () -> Unit,
    onNavigateToLocation: () -> Unit,

    ) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label1, style = MaterialTheme.typography.labelLarge)
            Text(
                value1,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.clickable(onClick = { onNavigateToLocationOrigin() })
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label2, style = MaterialTheme.typography.labelLarge)
            Text(
                value2,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.clickable(onClick = { onNavigateToLocation() })
            )
        }
    }
}