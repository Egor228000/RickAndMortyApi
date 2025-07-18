package com.example.rickandmortyapi.dao

import androidx.room.*
import com.example.rickandmortyapi.data.CharacterData
import com.example.rickandmortyapi.data.CharacterEntity
import com.example.rickandmortyapi.data.DetailCharacterData
import com.example.rickandmortyapi.data.DetailCharacterEntity
import com.example.rickandmortyapi.data.LocationData
import com.example.rickandmortyapi.data.LocationDetail
import com.example.rickandmortyapi.data.LocationDetailEntity
import com.example.rickandmortyapi.data.OriginData
import kotlin.collections.map

@Database(
    entities = [CharacterEntity::class, DetailCharacterEntity::class, LocationDetailEntity::class],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun characterDetailDao(): CharacterDetailDao
    abstract fun locationDetailDao(): LocationDetailDao
}

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters")
    suspend fun getAllCharacters(): List<CharacterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<CharacterEntity>)

    @Query("DELETE FROM characters")
    suspend fun clearAll()


}

class CharacterRepository(
    private val characterDao: CharacterDao,
    private val detailDao: CharacterDetailDao,
    private val locationDao: LocationDetailDao
) {
    suspend fun getCachedCharacters(): List<CharacterData> =
        characterDao.getAllCharacters().map { it.toCharacterData() }

    suspend fun cacheCharacters(characters: List<CharacterData>) {
        characterDao.insertCharacters(characters.map { it.toEntity() })
    }

    suspend fun getCharacterDetail(id: Int): DetailCharacterData? =
        detailDao.getCharacterDetailById(id)?.toDetailData()

    suspend fun cacheCharacterDetail(detail: DetailCharacterData) {
        detailDao.insertCharacterDetail(detail.toEntity())
    }

    suspend fun getCachedLocation(id: Int): LocationDetail? =
        locationDao.getLocationById(id)?.toDetail()

    suspend fun cacheLocation(detail: LocationDetail) =
        locationDao.insertLocation(detail.toEntity())

    suspend fun clearCache() {
        characterDao.clearAll()
    }

}

@Dao
interface CharacterDetailDao {
    @Query("SELECT * FROM character_details WHERE id = :id")
    suspend fun getCharacterDetailById(id: Int): DetailCharacterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacterDetail(detail: DetailCharacterEntity)
}
@Dao
interface LocationDetailDao {
    @Query("SELECT * FROM location_details WHERE id = :id")
    suspend fun getLocationById(id: Int): LocationDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(detail: LocationDetailEntity)
}

fun LocationDetail.toEntity(): LocationDetailEntity =
    LocationDetailEntity(
        id = id,
        name = name,
        type = type,
        dimension = dimension,
        residentsUrls = residents.joinToString(","),
        url = url,
        created = created
    )

fun LocationDetailEntity.toDetail(): LocationDetail =
    LocationDetail(
        id = id,
        name = name,
        type = type,
        dimension = dimension,
        residents = if (residentsUrls.isBlank()) emptyList() else residentsUrls.split(","),
        url = url,
        created = created
    )

fun CharacterEntity.toCharacterData(): CharacterData =
    CharacterData(id, name, species, status, gender, image)

fun CharacterData.toEntity(): CharacterEntity =
    CharacterEntity(id, name, species, status, gender, image)

fun DetailCharacterData.toEntity(): DetailCharacterEntity =
    DetailCharacterEntity(
        id = id,
        name = name,
        species = species,
        status = status,
        gender = gender,
        image = image,
        type = type,
        originName = origin.name,
        originUrl  = origin.url,
        locationName = location.name,
        locationUrl  = location.url,
        episodeUrls = episode.joinToString(",")
    )

fun DetailCharacterEntity.toDetailData(): DetailCharacterData =
    DetailCharacterData(
        id = id,
        name = name,
        species = species,
        status = status,
        gender = gender,
        image = image,
        type = type,
        origin   = OriginData(originName, originUrl),
        location = LocationData(locationName, locationUrl),
        episode  = episodeUrls.split(",")
    )
