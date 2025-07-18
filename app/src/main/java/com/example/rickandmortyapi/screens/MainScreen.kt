package com.example.rickandmortyapi.screens

import MainViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rickandmortyapi.R
import com.example.rickandmortyapi.data.CharacterData
import com.example.rickandmortyapi.data.FilterState
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainViewModel: MainViewModel, onNavigateToDetail: (Int) -> Unit) {
    val scope = rememberCoroutineScope()
    var isLoadPullToRefreshState by remember { mutableStateOf(false) }
    val listCharacter by mainViewModel.listCharacter.collectAsStateWithLifecycle()
    val isLoading by mainViewModel.isLoading.collectAsStateWithLifecycle()

    val gridState = rememberLazyGridState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden, skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = bottomSheetState
    )

    val statusList = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() })
    ) { mutableStateListOf<String>() }
    val speciesList = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() })
    ) { mutableStateListOf<String>() }
    val genderList = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() })
    ) { mutableStateListOf<String>() }

    val filterState = remember {
        FilterState(
            status = statusList, species = speciesList, gender = genderList
        )
    }
    var isSearch by remember { mutableStateOf(false) }
    var isSerchname by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current



    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }.distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= listCharacter.size - 4 && !isLoading) {
                    mainViewModel.loadNextCharacters()
                }
            }
    }

    LaunchedEffect(isSearch) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    Scaffold(
        modifier = Modifier
             .nestedScroll(scrollBehavior.nestedScrollConnection)
            .imePadding()
    ) { innerPadding ->
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = {
                Column(
                    modifier = Modifier
                        .height(500.dp)
                        .padding(16.dp)
                ) {
                    FilterSection(filterState)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                mainViewModel.updateFilters(filterState)
                                bottomSheetState.hide()
                            }
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Применить")
                    }
                }
            },
        ) {

            PullToRefreshBox(isRefreshing = isLoadPullToRefreshState, onRefresh = {
                scope.launch {
                    isLoadPullToRefreshState = true
                    delay(1000)
                    mainViewModel.clearSearchQuery()
                    mainViewModel.clearFilters()
                    mainViewModel.resetAll()

                    isLoadPullToRefreshState = false
                }
            }) {
                Column {

                    CenterAlignedTopAppBar(
                        scrollBehavior = scrollBehavior,
                        title = {
                        if (isSearch) {


                            OutlinedTextField(
                                value = isSerchname,
                                onValueChange = {
                                    isSerchname = it
                                    mainViewModel.setSearchQuery(it)
                                },
                                modifier = Modifier
                                    .focusRequester(focusRequester)
                                    .fillMaxWidth()
                                    .padding(end = 16.dp),
                                placeholder = { Text("Поиск по имени...") },
                                leadingIcon = {

                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                scaffoldState.bottomSheetState.expand()

                                            }
                                        }) {
                                        Icon(
                                            painter = painterResource(R.drawable.outline_filter_alt_24),
                                            null
                                        )
                                    }
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            isSearch = false
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                            mainViewModel.clearSearchQuery()
                                            isSerchname = ""
                                        }) {
                                        Icon(
                                            painter = painterResource(R.drawable.baseline_close_24),
                                            null
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(7.dp),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()

                                    }),
                            )
                        } else {
                            Text("Рикнутая лента")
                        }
                    }, actions = {
                        if (isSearch) {

                        } else {
                            IconButton(
                                onClick = { isSearch = true }) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_search_24),
                                    null
                                )
                            }
                        }

                    }, navigationIcon = {
                        if (isSearch) {

                        } else {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        scaffoldState.bottomSheetState.expand()

                                    }
                                }) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_filter_alt_24),
                                    null
                                )
                            }
                        }
                    })

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth(),
                        state = gridState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listCharacter) { character ->
                            CardCharacter(character) {
                                onNavigateToDetail(character.id)
                            }
                        }
                        item {
                            if (isLoading) {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .padding(innerPadding)
                                        .fillMaxSize()
                                        .padding(16.dp),
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSection(
    filterState: FilterState
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Status", style = MaterialTheme.typography.titleMedium)
        AssistChips(
            options = listOf("Alive", "Dead", "unknown"), selectedList = filterState.status
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Species", style = MaterialTheme.typography.titleMedium)
        AssistChips(
            options = listOf("Human", "Alien", "Robot", "Mythological", "unknown"),
            selectedList = filterState.species
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Gender", style = MaterialTheme.typography.titleMedium)
        AssistChips(
            options = listOf("Male", "Female", "Genderless", "unknown"),
            selectedList = filterState.gender
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistChips(
    options: List<String>, selectedList: SnapshotStateList<String>
) {
    FlowRow(
        modifier = Modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option in selectedList
            AssistChip(
                onClick = {
                    if (isSelected) selectedList.remove(option)
                    else selectedList.add(option)
                }, label = { Text(option) }, colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CardCharacter(
    listCharacter: CharacterData, onNavigateToDetail: () -> Unit
) {

    Card(
        modifier = Modifier
            .clickable(onClick = { onNavigateToDetail() })
            .fillMaxWidth()
            .aspectRatio(2f / 3f), shape = RoundedCornerShape(10.dp)

    ) {
        Box {
            CoilImage(
                imageModel = { listCharacter.image },
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                ),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {

                CustomText(
                    text = listCharacter.status,
                    modifier = Modifier.align(Alignment.TopEnd),
                    color = when (listCharacter.status) {
                        "Alive" -> Color(0xFF1AC122)
                        "Dead" -> Color(0xFFC11A1A)
                        else -> Color(0xFF7C7F82)
                    },
                    fontWeight = FontWeight.W600
                )
                CustomText(

                    listCharacter.name, modifier = Modifier
                        .padding(bottom = 46.dp)

                        .align(Alignment.BottomCenter), fontWeight = FontWeight.W800
                )
                CustomText(
                    listCharacter.gender,
                    modifier = Modifier.align(Alignment.BottomStart),
                )
                CustomText(
                    listCharacter.species,
                    modifier = Modifier.align(Alignment.BottomEnd),
                )
            }
        }
    }
}

@Composable
fun CustomText(
    text: String,
    modifier: Modifier = Modifier,
    font: TextUnit = 16.sp,
    color: Color = Color.White,
    fontWeight: FontWeight = FontWeight.W400
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(Color.DarkGray),
        shape = RoundedCornerShape(7.dp)

    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = if (text.length >= 13) text.take(10).plus("...") else text,
                fontSize = font,
                color = color,
                fontWeight = fontWeight
            )
        }
    }
}
