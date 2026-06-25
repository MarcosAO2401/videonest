package com.marc.videonest.ui
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.marc.videonest.data.model.Playlist
import com.marc.videonest.data.model.Video
import com.marc.videonest.ui.screens.PlaylistsScreen
import com.marc.videonest.ui.screens.PlayerScreen
import com.marc.videonest.ui.screens.VideosScreen
import com.marc.videonest.ui.theme.VideoNestTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedUrl = extractSharedUrl(intent)
        setContent { VideoNestTheme { VideoNestNavHost(sharedUrl) } }
    }
    private fun extractSharedUrl(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND || intent.type != "text/plain") return null
        val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return null
        return Regex("https://[^\s]+").find(text)?.value
    }
}

@Composable
fun VideoNestNavHost(initialSharedUrl: String?) {
    val navController = rememberNavController()
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var selectedVideo by remember { mutableStateOf<Video?>(null) }
    var pendingUrl by remember { mutableStateOf(initialSharedUrl) }
    NavHost(navController = navController, startDestination = "playlists") {
        composable("playlists") {
            PlaylistsScreen(onPlaylistClick = { playlist ->
                selectedPlaylist = playlist
                navController.navigate("videos")
            })
        }
        composable("videos") {
            val playlist = selectedPlaylist ?: return@composable
            VideosScreen(playlist = playlist, sharedUrl = pendingUrl,
                onBack = { pendingUrl = null; navController.popBackStack() },
                onVideoClick = { video -> selectedVideo = video; navController.navigate("player") })
            LaunchedEffect(Unit) { pendingUrl = null }
        }
        composable("player") {
            val video = selectedVideo ?: return@composable
            PlayerScreen(video = video, onBack = { navController.popBackStack() })
        }
    }
}
