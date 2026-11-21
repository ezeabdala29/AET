package com.aether.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aether.player.model.AudioFolder
import com.aether.player.model.AudiophileSettings
import com.aether.player.model.Track

// Dark Audiophile Palette (Aether Dark theme)
val BgDeepDark = Color(0xFF0A0C10)
val SurfaceDark = Color(0xFF131722)
val SurfaceHighlight = Color(0xFF1C2233)
val CyanAccent = Color(0xFF00E5FF)
val GoldAccent = Color(0xFFFFD700)
val TextMuted = Color(0xFF8E9BAE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val allTracks by viewModel.allTracks.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentTrack by viewModel.playerEngine.currentTrack.collectAsState()
    val isPlaying by viewModel.playerEngine.isPlaying.collectAsState()
    val settings by viewModel.playerEngine.settings.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            viewModel.loadLibrary()
        }
    }

    Scaffold(
        containerColor = BgDeepDark,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = Color.White
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AETHER",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = CyanAccent,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HI-FI PLAYER",
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = GoldAccent
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadLibrary() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar", tint = TextMuted)
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Ajustes Audiófilo", tint = CyanAccent)
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
                        bitPerfect = settings.bitPerfectMode,
                        onTogglePlay = { viewModel.togglePlayPause() }
                    )
                }

                NavigationBar(
                    containerColor = SurfaceDark,
                    contentColor = TextMuted
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            viewModel.selectFolder(null)
                        },
                        icon = { Icon(Icons.Default.MusicNote, contentDescription = "Canciones") },
                        label = { Text("Canciones (${allTracks.size})") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanAccent,
                            selectedTextColor = CyanAccent,
                            indicatorColor = SurfaceHighlight
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Carpetas") },
                        label = { Text("Carpetas (${folders.size})") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanAccent,
                            selectedTextColor = CyanAccent,
                            indicatorColor = SurfaceHighlight
                        )
                    )
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
                    CircularProgressIndicator(color = CyanAccent)
                }
            } else {
                when (selectedTab) {
                    0 -> SongsTab(tracks = allTracks, onTrackClick = { viewModel.playTrack(it) })
                    1 -> {
                        if (selectedFolder != null) {
                            val folderTracks = allTracks.filter { it.folderPath == selectedFolder?.path }
                            FolderDetailView(
                                folder = selectedFolder!!,
                                tracks = folderTracks,
                                onBack = { viewModel.selectFolder(null) },
                                onTrackClick = { viewModel.playTrack(it) }
                            )
                        } else {
                            FoldersTab(
                                folders = folders,
                                onFolderClick = { viewModel.selectFolder(it) }
                            )
                        }
                    }
                }
            }

            if (showSettingsDialog) {
                AudiophileSettingsDialog(
                    currentSettings = settings,
                    onSave = {
                        viewModel.updateSettings(it)
                        showSettingsDialog = false
                    },
                    onDismiss = { showSettingsDialog = false }
                )
            }
        }
    }
}

@Composable
fun SongsTab(tracks: List<Track>, onTrackClick: (Track) -> Unit) {
    if (tracks.isEmpty()) {
        EmptyPlaceholder(message = "No se encontraron canciones en el almacenamiento.\nAsegúrate de copiar archivos de música a tu teléfono.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(tracks, key = { it.id }) { track ->
                TrackItem(track = track, onClick = { onTrackClick(track) })
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
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
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
                            tint = GoldAccent,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = folder.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${folder.trackCount} pistas · ${folder.path}",
                                color = TextMuted,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
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
                .background(SurfaceDark)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
            }
            Column {
                Text(text = folder.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "${tracks.size} pistas en carpeta", color = CyanAccent, fontSize = 12.sp)
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
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceHighlight),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Audiotrack, contentDescription = null, tint = CyanAccent)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${track.artist} • ${track.album}",
                color = TextMuted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = track.audioFormatInfo,
                    color = GoldAccent,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            text = track.durationFormatted,
            color = TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun NowPlayingBar(
    track: Track,
    isPlaying: Boolean,
    bitPerfect: Boolean,
    onTogglePlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceHighlight),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (bitPerfect) {
                        Surface(
                            color = GoldAccent.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "BIT-PERFECT",
                                color = GoldAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = track.audioFormatInfo,
                        color = CyanAccent,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = track.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onTogglePlay) {
                Icon(
                    if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(38.dp)
                )
            }
        }
    }
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
        Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Acceso a Biblioteca Musical",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Aether Player necesita permiso para leer los archivos de audio almacenados en tu dispositivo.",
            color = TextMuted,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = BgDeepDark)
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
            color = TextMuted,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun AudiophileSettingsDialog(
    currentSettings: AudiophileSettings,
    onSave: (AudiophileSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var bitPerfect by remember { mutableStateOf(currentSettings.bitPerfectMode) }
    var bypassFx by remember { mutableStateOf(currentSettings.bypassSystemFx) }
    var floatOutput by remember { mutableStateOf(currentSettings.highResolutionOutput) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Equalizer, contentDescription = null, tint = GoldAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Ajustes Audiófilo (Hi-Fi)", color = Color.White, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingSwitchRow(
                    title = "Modo Bit-Perfect Direct",
                    description = "Desactiva remuestreo de software y conversiones de tono/velocidad de Android",
                    checked = bitPerfect,
                    onCheckedChange = { bitPerfect = it }
                )
                SettingSwitchRow(
                    title = "Bypass Mixer / Efectos DSP",
                    description = "Impide la captura y mezcla con sonidos de fondo del sistema",
                    checked = bypassFx,
                    onCheckedChange = { bypassFx = it }
                )
                SettingSwitchRow(
                    title = "Pipeline 32-bit Float AudioSink",
                    description = "Mantiene precisión de punto flotante para preservar rango dinámico completo",
                    checked = floatOutput,
                    onCheckedChange = { floatOutput = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    currentSettings.copy(
                        bitPerfectMode = bitPerfect,
                        bypassSystemFx = bypassFx,
                        highResolutionOutput = floatOutput
                    )
                )
            }) {
                Text(text = "APLICAR", color = CyanAccent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "CANCELAR", color = TextMuted)
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
            Text(text = title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = description, color = TextMuted, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyanAccent,
                checkedTrackColor = SurfaceHighlight
            )
        )
    }
}
