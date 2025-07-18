package com.example.rickandmortyapi.navDisplay

import MainViewModel
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.navEntryDecorator
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.example.rickandmortyapi.dao.CharacterRepository
import com.example.rickandmortyapi.screens.DetailScreen
import com.example.rickandmortyapi.screens.LocationScreen
import com.example.rickandmortyapi.screens.MainScreen
import kotlinx.serialization.Serializable


@Serializable
data object MainScreenNav : NavKey

@Serializable
data class DetailScreenNav(val id: Int = 0) : NavKey

@Serializable
data class LocationScreenNav(val id: Int = 0) : NavKey


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavDisplayNavigation(
    innerPadding: PaddingValues,
    backStack: NavBackStack,
    mainViewModel: MainViewModel,
    repository: CharacterRepository
) {
    val localNavSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope> =
        compositionLocalOf {
            throw IllegalStateException(
                "Unexpected access to LocalNavSharedTransitionScope. You must provide a " +
                        "SharedTransitionScope from a call to SharedTransitionLayout() or " +
                        "SharedTransitionScope()"
            )
        }
    val sharedEntryInSceneNavEntryDecorator = navEntryDecorator { entry ->
        with(localNavSharedTransitionScope.current) {
            Box(
                Modifier
                    .sharedElement(
                        rememberSharedContentState(entry.key),
                        animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                    ),
            ) {
                entry.content(entry.key)
            }
        }
    }
    val twoPaneStrategy = remember { TwoPaneSceneStrategy<Any>() }
    SharedTransitionLayout {
        CompositionLocalProvider(localNavSharedTransitionScope provides this) {
            NavDisplay(
                backStack = backStack,
                onBack = { keysToRemove -> repeat(keysToRemove) { backStack.removeLastOrNull() } },
                entryDecorators = listOf(
                    sharedEntryInSceneNavEntryDecorator,
                    rememberSceneSetupNavEntryDecorator(),
                    rememberSavedStateNavEntryDecorator(),
                ),
                sceneStrategy = twoPaneStrategy,
                entryProvider = entryProvider {
                    entry<MainScreenNav>(metadata = TwoPaneScene.twoPane()) {
                        MainScreen(
                            mainViewModel,
                            onNavigateToDetail = { id ->

                                backStack.add(DetailScreenNav(id = id))
                            }
                        )
                    }
                    entry<DetailScreenNav>(metadata = TwoPaneScene.twoPane()) { id ->
                        DetailScreen(
                            id.id,
                            onBack = { backStack.removeLastOrNull() },
                            onNavigateToLocationOrigin = { id ->
                                backStack.add(LocationScreenNav(id))
                            },
                            onNavigateToLocation = { id ->
                                backStack.add(LocationScreenNav(id))
                            },
                            repository = repository,
                            mainViewModel
                        )
                    }
                    entry<LocationScreenNav>() { id ->
                        LocationScreen(
                            id.id,
                            onBack = { backStack.removeLastOrNull() },
                            onNavigateToDetail = { id ->
                                backStack.add(DetailScreenNav(id = id))

                            },
                            mainViewModel,
                            repository
                        )
                    }
                }
            )
        }
    }
}