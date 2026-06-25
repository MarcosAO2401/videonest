package com.marc.videonest.ui.screens
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.marc.videonest.data.model.Platform
import com.marc.videonest.data.model.Playlist
import com.marc.videonest.data.model.Video
import com.marc.videonest.ui.PlaylistViewModel
import com.marc.videonest.ui.VideoViewModel
import com.marc.videonest.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(onPlaylistClick: (Playlist) -> Unit, viewModel: PlaylistViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("VideoNest", fontWeight = FontWeight.Bold, color = NestWhite) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = NestBlack)) },
        floatingActionButton = { FloatingActionButton(onClick = { showDialog = true }, containerColor = NestViolet) { Icon(Icons.Default.Add, null, tint = NestWhite) } },
        containerColor = NestBlack
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = NestViolet)
                state.playlists.isEmpty() -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.VideoLibrary, null, tint = NestMuted, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Sin listas todavia", color = NestWhite, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text("Crea tu primera lista con el boton +", color = NestMuted, fontSize = 14.sp)
                }
                else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.playlists, key = { it.id }) { playlist ->
                        var showMenu by remember { mutableStateOf(false) }
                        Card(modifier = Modifier.fillMaxWidth().clickable { onPlaylistClick(playlist) }, colors = CardDefaults.cardColors(containerColor = NestSurface), shape = RoundedCornerShape(16.dp)) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlaylistPlay, null, tint = NestViolet, modifier = Modifier.size(40.dp))
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(playlist.name, color = NestWhite, fontWeight = FontWeight.SemiBold)
                                    Text("Lista de reproduccion", color = NestMuted, fontSize = 12.sp)
                                }
                                Box {
                                    IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null, tint = NestMuted) }
                                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                        DropdownMenuItem(text = { Text("Eliminar", color = NestDanger) }, onClick = { viewModel.deletePlaylist(playlist); showMenu = false }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = NestDanger) })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (showDialog) {
        var text by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showDialog = false },
            title = { Text("Nueva lista", color = NestWhite) },
            text = { OutlinedTextField(value = text, onValueChange = { text = it.take(100) }, label = { Text("Nombre") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NestViolet, cursorColor = NestViolet)) },
            confirmButton = { TextButton(onClick = { if (text.isNotBlank()) { viewModel.createPlaylist(text) {}; showDialog = false } }, enabled = text.isNotBlank()) { Text("Crear", color = NestViolet) } },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancelar", color = NestMuted) } },
            containerColor = NestSurface)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(playlist: Playlist, sharedUrl: String?, onBack: () -> Unit, onVideoClick: (Video) -> Unit, viewModel: VideoViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    var showSave by remember { mutableStateOf(sharedUrl != null) }
    val snackbar = remember { SnackbarHostState() }
    var snackMsg by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(playlist.id) { viewModel.setPlaylist(playlist.id) }
    LaunchedEffect(search) { viewModel.setSearch(search) }
    snackMsg?.let { msg -> LaunchedEffect(msg) { snackbar.showSnackbar(msg); snackMsg = null } }
    Scaffold(
        topBar = { TopAppBar(title = { Column { Text(playlist.name, color = NestWhite, fontWeight = FontWeight.Bold); Text("\${state.videos.size} videos", color = NestMuted, fontSize = 12.sp) } }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = NestWhite) } }, actions = { IconButton(onClick = { showSave = true }) { Icon(Icons.Default.Add, null, tint = NestViolet) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = NestBlack)) },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = NestBlack
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(value = search, onValueChange = { search = it }, placeholder = { Text("Buscar...", color = NestMuted) }, leadingIcon = { Icon(Icons.Default.Search, null, tint = NestMuted) }, modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NestViolet, unfocusedBorderColor = NestElevated, cursorColor = NestViolet))
            when {
                state.isLoading -> Box(Modifier.fillMaxSize()) { CircularProgressIndicator(Modifier.align(Alignment.Center), color = NestViolet) }
                state.videos.isEmpty() -> Box(Modifier.fillMaxSize()) { Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.VideoLibrary, null, tint = NestMuted, modifier = Modifier.size(56.dp)); Spacer(Modifier.height(12.dp)); Text("Lista vacia", color = NestWhite, fontSize = 18.sp, fontWeight = FontWeight.SemiBold); Text("Compartí un video desde otra app", color = NestMuted) } }
                else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.videos, key = { it.id }) { video ->
                        var showMenu by remember { mutableStateOf(false) }
                        Card(modifier = Modifier.fillMaxWidth().clickable { onVideoClick(video) }, colors = CardDefaults.cardColors(containerColor = NestSurface), shape = RoundedCornerShape(12.dp)) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(72.dp, 50.dp).clip(RoundedCornerShape(8.dp)).background(NestElevated), contentAlignment = Alignment.Center) {
                                    if (video.thumbnailUrl != null) AsyncImage(model = video.thumbnailUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    else Icon(Icons.Default.PlayArrow, null, tint = platformColor(video.platform), modifier = Modifier.size(28.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(video.platform.displayName, color = platformColor(video.platform), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Text(video.title, color = NestWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (video.note.isNotBlank()) Text(video.note, color = NestMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Box {
                                    IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null, tint = NestMuted) }
                                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                        DropdownMenuItem(text = { Text("Eliminar", color = NestDanger) }, onClick = { viewModel.deleteVideo(video); showMenu = false }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = NestDanger) })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (showSave) {
        var url by remember { mutableStateOf(sharedUrl ?: "") }
        var note by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showSave = false },
            title = { Text("Guardar video", color = NestWhite) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("URL del video") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NestViolet, cursorColor = NestViolet)); OutlinedTextField(value = note, onValueChange = { note = it.take(200) }, label = { Text("Nota (opcional)") }, maxLines = 2, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NestViolet, cursorColor = NestViolet)) } },
            confirmButton = { TextButton(onClick = { if (url.isNotBlank()) { viewModel.saveVideo(url.trim(), playlist.id, note) { _, msg -> snackMsg = msg }; showSave = false } }, enabled = url.isNotBlank()) { Text("Guardar", color = NestViolet) } },
            dismissButton = { TextButton(onClick = { showSave = false }) { Text("Cancelar", color = NestMuted) } },
            containerColor = NestSurface)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(video: Video, onBack: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(video.title, color = NestWhite, maxLines = 1, overflow = TextOverflow.Ellipsis) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = NestWhite) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = NestBlack)) },
        containerColor = NestBlack
    ) { padding ->
        AndroidView(factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.apply { javaScriptEnabled = false; domStorageEnabled = false; allowFileAccess = false }
                loadUrl(video.url)
            }
        }, modifier = Modifier.padding(padding).fillMaxSize())
    }
}

fun platformColor(platform: Platform) = when(platform) {
    Platform.YOUTUBE -> YoutubeRed
    Platform.TIKTOK -> TikTokCyan
    Platform.INSTAGRAM -> InstagramPurple
    Platform.TWITTER -> TwitterBlue
    Platform.FACEBOOK -> FacebookBlue
    Platform.UNKNOWN -> NestMuted
}
