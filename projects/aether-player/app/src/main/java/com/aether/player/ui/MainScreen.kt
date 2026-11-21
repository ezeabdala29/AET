package com.aether.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aether.player.model.AudioFolder
import com.aether.player.model.AudiophileSettings
import com.aether.player.model.Track
import kotlinx.coroutines.launch

private val AetherDarkColors = darkColorScheme(
    background = Color(0xFF101114),
    surface = Color(0xFF191B20),
    surfaceVariant = Color(0xFF24272D),
    primary = Color(0xFFFF6B65),
    secondary = Color(0xFFB9C7FF),
    onBackground = Color(0xFFF5F5F6),
    onSurface = Color(0xFFF5F5F6),
    onSurfaceVariant = Color(0xFFAAAEB7)
)

private val AetherLightColors = lightColorScheme(
    background = Color(0xFFF7F7F8),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEEEEF1),
    primary = Color(0xFFE64F55),
    secondary = Color(0xFF5366A8),
    onBackground = Color(0xFF18191C),
    onSurface = Color(0xFF18191C),
    onSurfaceVariant = Color(0xFF737780)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    var darkTheme by rememberSaveable { mutableStateOf(true) }
    MaterialTheme(colorScheme = if (darkTheme) AetherDarkColors else AetherLightColors) {
        MainScreenContent(
            viewModel = viewModel,
            hasPermission = hasPermission,
            onRequestPermission = onRequestPermission,
            onToggleTheme = { darkTheme = !darkTheme },
            darkTheme = darkTheme
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenContent(
    viewModel: MainViewModel,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onToggleTheme: () -> Unit,
    darkTheme: Boolean
) {
    val allTracks by viewModel.allTracks.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentTrack by viewModel.playerEngine.currentTrack.collectAsState()
    val isPlaying by viewModel.playerEngine.isPlaying.collectAsState()
    val settings by viewModel.playerEngine.settings.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val favoriteTrackIds by viewModel.favoriteTrackIds.collectAsState()
    val currentPosition by viewModel.playerEngine.currentPosition.collectAsState()
    val duration by viewModel.playerEngine.duration.collectAsState()
    val shuffleEnabled by viewModel.playerEngine.shuffleEnabled.collectAsState()
    val repeatMode by viewModel.playerEngine.repeatMode.collectAsState()
    val queue by viewModel.playerEngine.queue.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedLibraryTab by remember { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var showNowPlayingExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            viewModel.loadLibrary()
        }
    }

    val filteredTracks = remember(allTracks, searchQuery) {
        if (searchQuery.isBlank()) {
            allTracks
        } else {
            val q = searchQuery.trim().lowercase()
            allTracks.filter {
                it.title.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.album.lowercase().contains(q) ||
                it.folderName.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    if (isSearchActive) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text("Buscar canción, artista o álbum...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 8.dp)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AETHER",
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HI-FI PLAYER",
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                },
                actions = {
                    if (isSearchActive) {
                        IconButton(onClick = {
                            isSearchActive = false
                            viewModel.setSearchQuery("")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar búsqueda", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        IconButton(onClick = { isSearchActive = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = { viewModel.loadLibrary() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Ajustes Audiófilo", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (darkTheme) "Activar modo claro" else "Activar modo oscuro",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column {
                // Mini Player Bar
                currentTrack?.let { track ->
                    NowPlayingBar(
                        track = track,
                        isPlaying = isPlaying,
                        onBarClick = { showNowPlayingExpanded = true },
                        onTogglePlay = { viewModel.togglePlayPause() }
                    )
                }

                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    val tabs = listOf(
                        Triple("Descubrir", Icons.Default.Explore, 0),
                        Triple("Mi música", Icons.Default.LibraryMusic, 1),
                        Triple("Mix", Icons.Default.GraphicEq, 2),
                        Triple("Favoritos", Icons.Default.Favorite, 3)
                    )
                    tabs.forEach { (label, icon, index) ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                if (index == 1) viewModel.selectFolder(null)
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!hasPermission) {
                PermissionRequestBanner(onRequestPermission = onRequestPermission)
            } else if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                when (selectedTab) {
                    0 -> DiscoverScreen(
                        tracks = filteredTracks,
                        currentTrack = currentTrack,
                        onPlayTrack = { index -> viewModel.playTracks(filteredTracks, index) },
                        onOpenLibrary = { selectedTab = 1 }
                    )
                    1 -> {
                        if (selectedFolder != null) {
                            val folderTracks = allTracks.filter { it.folderPath == selectedFolder?.path }
                            FolderDetailView(
                                folder = selectedFolder!!,
                                tracks = folderTracks,
                                onBack = { viewModel.selectFolder(null) },
                                onTrackClick = { track -> viewModel.playTracks(folderTracks, folderTracks.indexOf(track).coerceAtLeast(0)) }
                            )
                        } else {
                            Column {
                                TabRow(selectedTabIndex = selectedLibraryTab) {
                                    Tab(selected = selectedLibraryTab == 0, onClick = { selectedLibraryTab = 0 }, text = { Text("Canciones") })
                                    Tab(selected = selectedLibraryTab == 1, onClick = { selectedLibraryTab = 1 }, text = { Text("Carpetas") })
                                }
                                if (selectedLibraryTab == 0) {
                                    SongsTabWithFastScroll(tracks = filteredTracks, onTrackClick = { track -> viewModel.playTracks(filteredTracks, filteredTracks.indexOf(track).coerceAtLeast(0)) })
                                } else {
                                    FoldersTab(folders = folders, onFolderClick = { viewModel.selectFolder(it) })
                                }
                            }
                        }
                    }
                    2 -> MixScreen(
                        tracks = filteredTracks,
                        queue = queue,
                        shuffleEnabled = shuffleEnabled,
                        onStartMix = { if (filteredTracks.isNotEmpty()) viewModel.playTracks(filteredTracks) },
                        onToggleShuffle = { viewModel.toggleShuffle() },
                        onPlayTracks = { tracksToPlay, index -> viewModel.playTracks(tracksToPlay, index) }
                    )
                    3 -> FavoritesScreen(
                        tracks = filteredTracks.filter { it.id.toString() in favoriteTrackIds },
                        onPlayTrack = { favorites, index -> viewModel.playTracks(favorites, index) }
                    )
                }
            }

            if (showSettingsDialog) {
                AudiophileSettingsDialog(
                    currentSettings = settings,
                    systemAudioCaptureBlocked = viewModel.playerEngine.isSystemAudioCaptureBlocked(),
                    onSave = {
                        viewModel.updateSettings(it)
                        showSettingsDialog = false
                    },
                    onDismiss = { showSettingsDialog = false }
                )
            }

            if (showNowPlayingExpanded && currentTrack != null) {
                NowPlayingExpandedSheet(
                    track = currentTrack!!,
                    isPlaying = isPlaying,
                    bitPerfectConfirmed = viewModel.playerEngine.isBitPerfectOutputConfirmed(),
                    floatOutputEnabled = viewModel.playerEngine.isHighResolutionFloatOutputEnabled(),
                    currentPosition = currentPosition,
                    duration = duration,
                    queueIndex = viewModel.playerEngine.exoPlayer?.currentMediaItemIndex ?: 0,
                    queueSize = queue.size,
                    isFavorite = currentTrack!!.id.toString() in favoriteTrackIds,
                    shuffleEnabled = shuffleEnabled,
                    repeatMode = repeatMode,
                    onTogglePlay = { viewModel.togglePlayPause() },
                    onDismiss = { showNowPlayingExpanded = false },
                    onSeek = { viewModel.seekTo(it) },
                    onNext = { viewModel.skipToNext() },
                    onPrevious = { viewModel.skipToPrevious() },
                    onToggleShuffle = { viewModel.toggleShuffle() },
                    onToggleRepeat = { viewModel.cycleRepeatMode() },
                    onToggleFavorite = { viewModel.toggleFavorite(currentTrack!!.id) },
                    onOpenSettings = { showSettingsDialog = true },
                    onSearchRelated = {
                        viewModel.setSearchQuery(currentTrack!!.artist)
                        viewModel.selectFolder(null)
                        selectedLibraryTab = 0
                        selectedTab = 1
                        showNowPlayingExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DiscoverScreen(
    tracks: List<Track>,
    currentTrack: Track?,
    onPlayTrack: (Int) -> Unit,
    onOpenLibrary: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Tu música, a tu manera", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Tu biblioteca en calidad original", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenLibrary)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("BIBLIOTECA LOCAL", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("${tracks.size} canciones listas para escuchar", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text("Explorá álbumes, artistas y carpetas", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Abrir biblioteca", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Escuchado recientemente", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = onOpenLibrary) { Text("Ver todo") }
            }
        }
        if (currentTrack != null) {
            item {
                TrackCard(
                    track = currentTrack,
                    subtitle = "Continuar escuchando",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { tracks.indexOf(currentTrack).takeIf { it >= 0 }?.let(onPlayTrack) }
                )
            }
        }
        if (tracks.isEmpty()) {
            item { EmptyPlaceholder("Tu biblioteca está vacía. Agregá música en el dispositivo para empezar.") }
        } else {
            item {
                Text("Para vos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(tracks.take(10), key = { it.id }) { track ->
                        TrackCard(
                            track = track,
                            subtitle = track.album,
                            modifier = Modifier.width(170.dp),
                            onClick = { onPlayTrack(tracks.indexOf(track)) }
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun TrackCard(track: Track, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AlbumArt(track = track, modifier = Modifier.fillMaxWidth().aspectRatio(1f), corner = 16.dp)
            Text(track.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle.ifBlank { track.artist }, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AlbumArt(track: Track, modifier: Modifier = Modifier, corner: androidx.compose.ui.unit.Dp = 16.dp) {
    Box(modifier.clip(RoundedCornerShape(corner)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        if (track.albumArtUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(track.albumArtUri).crossfade(true).build(),
                contentDescription = "Portada de ${track.album}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
        }
    }
}

@Composable
private fun MixScreen(
    tracks: List<Track>,
    queue: List<Track>,
    shuffleEnabled: Boolean,
    onStartMix: () -> Unit,
    onToggleShuffle: () -> Unit,
    onPlayTracks: (List<Track>, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Mix", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Una sesión continua con tu biblioteca local", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    Text("Mezclá tu biblioteca", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Reproduce la selección como una cola continua. El modo aleatorio cambia el orden.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = onStartMix, enabled = tracks.isNotEmpty(), shape = RoundedCornerShape(16.dp)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Iniciar mix")
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Datos para mezclar", fontWeight = FontWeight.SemiBold)
                    Text("BPM y tonalidad: no disponibles en los metadatos locales", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Text("Esta versión reproduce sin pausas entre pistas; todavía no aplica fundido cruzado ni análisis armónico.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Cola (${queue.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                FilterChip(selected = shuffleEnabled, onClick = onToggleShuffle, label = { Text("Aleatorio") }, leadingIcon = { Icon(Icons.Default.Shuffle, null) })
            }
        }
        if (queue.isNotEmpty()) {
            items(queue, key = { "queue-${it.id}" }) { track ->
                TrackItem(track = track, onClick = { onPlayTracks(queue, queue.indexOf(track)) })
            }
        } else if (tracks.isEmpty()) {
            item { EmptyPlaceholder("Agregá canciones a tu biblioteca para crear un mix.") }
        } else {
            item { Text("Iniciá un mix para armar una cola desde ${tracks.size} canciones.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun FavoritesScreen(tracks: List<Track>, onPlayTrack: (List<Track>, Int) -> Unit) {
    if (tracks.isEmpty()) {
        EmptyPlaceholder("Todavía no tenés favoritos. Marcá una canción con el corazón desde el reproductor.")
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 12.dp)) {
            item {
                Text("Tus favoritos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
            }
            items(tracks, key = { it.id }) { track ->
                TrackItem(track = track, onClick = { onPlayTrack(tracks, tracks.indexOf(track)) })
            }
        }
    }
}

@Composable
fun SongsTabWithFastScroll(tracks: List<Track>, onTrackClick: (Track) -> Unit) {
    if (tracks.isEmpty()) {
        EmptyPlaceholder(message = "No se encontraron canciones.\nAsegúrate de copiar archivos de música a tu teléfono o cambia el filtro de búsqueda.")
    } else {
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        var isDraggingScrollbar by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(end = 24.dp, bottom = 16.dp)
            ) {
                items(tracks, key = { it.id }) { track ->
                    TrackItem(track = track, onClick = { onTrackClick(track) })
                }
            }

            // Fast Scroll Thumb Indicator
            if (tracks.size > 10) {
                val totalItems = tracks.size
                val firstVisibleItem = listState.firstVisibleItemIndex
                val thumbRatio = firstVisibleItem.toFloat() / totalItems.coerceAtLeast(1)

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(28.dp)
                        .padding(vertical = 12.dp)
                        .pointerInput(totalItems) {
                            detectVerticalDragGestures(
                                onDragStart = { isDraggingScrollbar = true },
                                onDragEnd = { isDraggingScrollbar = false },
                                onDragCancel = { isDraggingScrollbar = false }
                            ) { change, _ ->
                                val positionY = change.position.y.coerceIn(0f, size.height.toFloat())
                                val fraction = positionY / size.height.toFloat()
                                val targetIndex = (fraction * (totalItems - 1)).toInt().coerceIn(0, totalItems - 1)
                                coroutineScope.launch {
                                    listState.scrollToItem(targetIndex)
                                }
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxHeight()
                            .width(3.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(2.dp))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (thumbRatio * 450).dp)
                            .size(if (isDraggingScrollbar) 22.dp else 16.dp)
                            .clip(CircleShape)
                            .background(if (isDraggingScrollbar) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDraggingScrollbar) {
                            val currentLetter = tracks.getOrNull(firstVisibleItem)?.title?.take(1)?.uppercase() ?: ""
                            Text(
                                text = currentLetter,
                                color = MaterialTheme.colorScheme.background,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoldersTab(folders: List<AudioFolder>, onFolderClick: (AudioFolder) -> Unit) {
    if (folders.isEmpty()) {
        EmptyPlaceholder(message = "No se encontraron carpetas con música.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(folders, key = { it.path }) { folder ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFolderClick(folder) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FolderSpecial,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = folder.name,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${folder.trackCount} pistas · ${folder.path}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun FolderDetailView(
    folder: AudioFolder,
    tracks: List<Track>,
    onBack: () -> Unit,
    onTrackClick: (Track) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.onSurface)
            }
            Column {
                Text(text = folder.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "${tracks.size} pistas en carpeta", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(tracks, key = { it.id }) { track ->
                TrackItem(track = track, onClick = { onTrackClick(track) })
            }
        }
    }
}

@Composable
fun TrackItem(track: Track, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album art with fallback
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (track.albumArtUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(track.albumArtUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Carátula",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Default.Audiotrack, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${track.artist} • ${track.album}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = track.audioFormatInfo,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                if (track.lyrics != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(3.dp)
                    ) {
                        Text(
                            text = "LRC",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
        Text(
            text = track.durationFormatted,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun NowPlayingBar(
    track: Track,
    isPlaying: Boolean,
    onBarClick: () -> Unit,
    onTogglePlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBarClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                if (track.albumArtUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(track.albumArtUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BIT-PERFECT NO CONFIRMADO",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = track.audioFormatInfo,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = track.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onTogglePlay) {
                Icon(
                    if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(38.dp)
                )
            }
        }
    }
}

@Composable
fun NowPlayingExpandedSheet(
    track: Track,
    isPlaying: Boolean,
    bitPerfectConfirmed: Boolean,
    floatOutputEnabled: Boolean,
    currentPosition: Long,
    duration: Long,
    queueIndex: Int,
    queueSize: Int,
    isFavorite: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    onTogglePlay: () -> Unit,
    onDismiss: () -> Unit,
    onSeek: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenSettings: () -> Unit,
    onSearchRelated: () -> Unit
) {
    var showLyricsView by remember { mutableStateOf(false) }
    var showCassetteScreen by rememberSaveable(track.id) { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }
    var showTrackDetails by remember { mutableStateOf(false) }
    var showGuide by remember { mutableStateOf(false) }
    var sliderPosition by remember(track.id) { mutableFloatStateOf(currentPosition.toFloat()) }
    var isScrubbing by remember(track.id) { mutableStateOf(false) }

    LaunchedEffect(track.id, currentPosition) {
        if (!isScrubbing) sliderPosition = currentPosition.toFloat()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        if (track.albumArtUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(track.albumArtUri).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().blur(52.dp).alpha(0.25f)
            )
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.95f)))))
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Cerrar reproductor", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(32.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REPRODUCIENDO", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, fontSize = 11.sp)
                    Text("${(queueIndex + 1).coerceAtLeast(1)} de ${queueSize.coerceAtLeast(1)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
                Box {
                    IconButton(onClick = { showOptions = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    DropdownMenu(expanded = showOptions, onDismissRequest = { showOptions = false }) {
                        DropdownMenuItem(text = { Text("Ajustes") }, leadingIcon = { Icon(Icons.Default.Tune, null) }, onClick = { showOptions = false; onOpenSettings() })
                        DropdownMenuItem(text = { Text("Añadir a Marcadores") }, leadingIcon = { Icon(Icons.Default.BookmarkAdd, null) }, onClick = { showOptions = false; onToggleFavorite() })
                        DropdownMenuItem(text = { Text("Buscar relacionados") }, leadingIcon = { Icon(Icons.Default.Search, null) }, onClick = { showOptions = false; onSearchRelated() })
                        DropdownMenuItem(
                            text = { Text("Mostrar pantalla repr. de casete") },
                            leadingIcon = { Icon(Icons.Default.LibraryMusic, null) },
                            trailingIcon = { Checkbox(checked = showCassetteScreen, onCheckedChange = { showCassetteScreen = it }) },
                            onClick = { showCassetteScreen = !showCassetteScreen }
                        )
                        DropdownMenuItem(text = { Text(if (showLyricsView) "Ocultar letras" else "Visualizar letras") }, leadingIcon = { Icon(Icons.Default.Lyrics, null) }, onClick = { showOptions = false; showLyricsView = !showLyricsView })
                        DropdownMenuItem(text = { Text("Info. detallada de la canción") }, leadingIcon = { Icon(Icons.Default.Info, null) }, onClick = { showOptions = false; showTrackDetails = true })
                        DropdownMenuItem(text = { Text("Guía de uso") }, leadingIcon = { Icon(Icons.Default.HelpOutline, null) }, onClick = { showOptions = false; showGuide = true })
                        DropdownMenuItem(text = { Text(if (isFavorite) "Quitar de favoritos" else "Añadir a favoritos") }, leadingIcon = { Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null) }, onClick = { showOptions = false; onToggleFavorite() })
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.4f))

            if (showLyricsView) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
                        .playerSwipeGestures(onLeft = { showLyricsView = true }, onRight = { showLyricsView = false }, onDown = onDismiss)
                        .padding(20.dp)
                ) {
                    if (!track.lyrics.isNullOrBlank()) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Text(
                                    text = track.lyrics,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp,
                                    lineHeight = 28.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Sin archivo .lrc de letras local.\nColoca un archivo con el mismo nombre y extensión .lrc junto a tu música.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else if (showCassetteScreen) {
                CassetteArtwork(track = track, modifier = Modifier.fillMaxWidth(0.88f).aspectRatio(1f))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .playerSwipeGestures(onLeft = { showLyricsView = true }, onRight = { showLyricsView = false }, onDown = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    if (track.albumArtUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(track.albumArtUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Carátula de álbum",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().padding(10.dp).clip(RoundedCornerShape(22.dp))
                        )
                    } else {
                        Icon(Icons.Default.Audiotrack, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(96.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(track.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 21.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${track.artist} — ${track.album}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = if (isFavorite) "Quitar de favoritos" else "Añadir a favoritos", tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (bitPerfectConfirmed) "BIT-PERFECT CONFIRMADO" else "Salida de AETHER · bit-perfect no confirmado" +
                        if (floatOutputEnabled) " · salida float activa" else "",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
                Text(track.audioFormatInfo, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }

            Slider(
                value = sliderPosition.coerceIn(0f, duration.coerceAtLeast(1L).toFloat()),
                onValueChange = { sliderPosition = it; isScrubbing = true },
                onValueChangeFinished = { onSeek(sliderPosition.toLong()); isScrubbing = false },
                valueRange = 0f..duration.coerceAtLeast(1L).toFloat(),
                enabled = duration > 0L
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatPlaybackTime(if (isScrubbing) sliderPosition.toLong() else currentPosition), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                Text(formatPlaybackTime(duration), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleShuffle) { Icon(Icons.Default.Shuffle, "Aleatorio", tint = if (shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
                IconButton(onClick = onPrevious) { Icon(Icons.Default.SkipPrevious, "Anterior", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(34.dp)) }
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                        contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                IconButton(onClick = onNext) { Icon(Icons.Default.SkipNext, "Siguiente", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(34.dp)) }
                IconButton(onClick = onToggleRepeat) {
                    Icon(
                        if (repeatMode == androidx.media3.common.Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        "Repetir",
                        tint = if (repeatMode != androidx.media3.common.Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))
        }
    }

    if (showTrackDetails) {
        AlertDialog(
            onDismissRequest = { showTrackDetails = false },
            title = { Text("Info. detallada de la canción") },
            text = { Text("${track.title}\n${track.artist}\n${track.album}\n${track.audioFormatInfo}\nDuración: ${track.durationFormatted}") },
            confirmButton = { TextButton(onClick = { showTrackDetails = false }) { Text("Cerrar") } }
        )
    }
    if (showGuide) {
        AlertDialog(
            onDismissRequest = { showGuide = false },
            title = { Text("Guía de uso") },
            text = { Text("Deslizá la barra para cambiar de posición. Usá anterior/siguiente para recorrer la cola y el corazón para guardar favoritos. En Mix podés reproducir tu biblioteca como una cola continua.") },
            confirmButton = { TextButton(onClick = { showGuide = false }) { Text("Entendido") } }
        )
    }
}

@Composable
private fun CassetteArtwork(track: Track, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "cassette-reels")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "reel-rotation"
    )
    Box(modifier.clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surface).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(track.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.artist, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                repeat(2) {
                    Box(Modifier.size(76.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(62.dp).graphicsLayer { rotationZ = rotation })
                        Box(Modifier.size(14.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface))
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Text("AETHER · ${track.audioFormatInfo}", color = MaterialTheme.colorScheme.secondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
    }
}

private fun Modifier.playerSwipeGestures(
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onDown: () -> Unit
): Modifier = pointerInput(onLeft, onRight, onDown) {
    var dragX = 0f
    var dragY = 0f
    detectDragGestures(
        onDragStart = { dragX = 0f; dragY = 0f },
        onDragEnd = {
            when {
                kotlin.math.abs(dragX) > kotlin.math.abs(dragY) && dragX < -70f -> onLeft()
                kotlin.math.abs(dragX) > kotlin.math.abs(dragY) && dragX > 70f -> onRight()
                dragY > 90f -> onDown()
                dragY < -90f -> onLeft()
            }
        }
    ) { change, amount ->
        dragX += amount.x
        dragY += amount.y
        change.consume()
    }
}

private fun formatPlaybackTime(positionMs: Long): String {
    val totalSeconds = (positionMs.coerceAtLeast(0L) / 1000L)
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

@Composable
fun PermissionRequestBanner(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Acceso a Biblioteca Musical",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Aether Player necesita permiso para leer los archivos de audio almacenados en tu dispositivo.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.background)
        ) {
            Text(text = "CONCEDER PERMISO", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmptyPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AudiophileSettingsDialog(
    currentSettings: AudiophileSettings,
    systemAudioCaptureBlocked: Boolean,
    onSave: (AudiophileSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var bypassFx by remember { mutableStateOf(systemAudioCaptureBlocked) }
    var floatOutput by remember { mutableStateOf(currentSettings.highResolutionOutput) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Equalizer, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Ajustes Audiófilo (Hi-Fi)", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Prioridad: audio sin crossfade ni DSP de AETHER. Esta versión no puede confirmar Bit-Perfect en Android.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                SettingSwitchRow(
                    title = "Bloquear captura de audio",
                    description = "Evita la captura de audio de la app; no desactiva el mixer ni el DSP del sistema.",
                    checked = bypassFx,
                    onCheckedChange = { bypassFx = it }
                )
                SettingSwitchRow(
                    title = "Salida PCM de punto flotante",
                    description = "Puede preservar mayor precisión interna, pero no equivale a salida Bit-Perfect; desactivala para priorizar PCM entero.",
                    checked = floatOutput,
                    onCheckedChange = { floatOutput = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    currentSettings.copy(
                        bitPerfectMode = true,
                        bypassSystemFx = bypassFx,
                        highResolutionOutput = floatOutput
                    )
                )
            }) {
                Text(text = "APLICAR", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "CANCELAR", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
