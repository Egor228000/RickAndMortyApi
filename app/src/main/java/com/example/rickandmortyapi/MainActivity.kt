package com.example.rickandmortyapi

import MainViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.room.Room
import com.example.rickandmortyapi.dao.AppDatabase
import com.example.rickandmortyapi.dao.CharacterRepository
import com.example.rickandmortyapi.navDisplay.MainScreenNav
import com.example.rickandmortyapi.navDisplay.NavDisplayNavigation
import com.example.rickandmortyapi.ui.theme.RickAndMortyApiTheme

class MainActivity : ComponentActivity() {
    private lateinit var mainViewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder(

            applicationContext,
            AppDatabase::class.java,
            "characters_db"
        )
            .fallbackToDestructiveMigration(false)
            .build()
        val repository = CharacterRepository(
            characterDao = db.characterDao(),
            detailDao = db.characterDetailDao(),
            locationDao = db.locationDetailDao()

        )
        mainViewModel = MainViewModel(repository)
        setContent {
            val backStack = rememberNavBackStack(MainScreenNav)
            RickAndMortyApiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavDisplayNavigation(innerPadding, backStack, mainViewModel, repository)
                }
            }
        }
    }
}
