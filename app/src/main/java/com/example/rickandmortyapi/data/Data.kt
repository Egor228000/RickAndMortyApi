package com.example.rickandmortyapi.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys


data class FilterState(
    val status: SnapshotStateList<String> = mutableStateListOf(),
    val species: SnapshotStateList<String> = mutableStateListOf(),
    val gender: SnapshotStateList<String> = mutableStateListOf()
)

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val species: String,
    val status: String,
    val gender: String,
    val image: String
)


@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class CharacterData(
    val id: Int,
    val name: String,
    val species: String,
    val status: String,
    val gender: String,
    val image: String
)

@Serializable
data class CharactersResponse(
    val info: Info,
    val results: List<CharacterData>
)

@Serializable
data class Info(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?
)


@Entity(tableName = "character_details")
data class DetailCharacterEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val species: String,
    val status: String,
    val gender: String,
    val image: String,
    val type: String,
    val originName: String,
    val originUrl: String,
    val locationName: String,
    val locationUrl: String,
    val episodeUrls: String
)

@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class DetailCharacterData(
    val id: Int,
    val name: String,
    val species: String,
    val status: String,
    val gender: String,
    val image: String,
    val type: String,
    val origin: OriginData,
    val location: LocationData,
    val episode: List<String>
)

@Serializable
data class OriginData(
    val name: String,
    val url: String
)

@Serializable
data class LocationData(
    val name: String,
    val url: String
)

@Entity(tableName = "location_details")
data class LocationDetailEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val type: String,
    val dimension: String,
    val residentsUrls: String,
    val url: String,
    val created: String
)

@Serializable
data class LocationDetail(
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String,
    val residents: List<String>,
    val url: String,
    val created: String
)

