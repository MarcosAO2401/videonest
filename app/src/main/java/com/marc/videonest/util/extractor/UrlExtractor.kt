package com.marc.videonest.util.extractor
import com.marc.videonest.data.model.Platform
import java.net.URI

data class VideoMetadata(
    val platform: Platform,
    val thumbnailUrl: String?,
    val videoId: String?,
    val sanitizedUrl: String
)

object UrlExtractor {
    private val ALLOWED_DOMAINS = setOf(
        "youtube.com", "youtu.be", "tiktok.com", "instagram.com",
        "twitter.com", "x.com", "facebook.com", "fb.watch"
    )

    fun extract(rawUrl: String): VideoMetadata? {
        val url = rawUrl.trim()
        if (url.length > 2048 || !url.startsWith("https://")) return null
        val host = try { URI(url).host?.lowercase() } catch(e: Exception) { null } ?: return null
        if (ALLOWED_DOMAINS.none { host.endsWith(it) }) return null
        return when {
            host.contains("youtube.com") || host.contains("youtu.be") -> {
                val id = Regex("youtu[.]be/([A-Za-z0-9_-]{11})").find(url)?.groupValues?.get(1)
                    ?: Regex("[?&]v=([A-Za-z0-9_-]{11})").find(url)?.groupValues?.get(1)
                    ?: Regex("shorts/([A-Za-z0-9_-]{11})").find(url)?.groupValues?.get(1)
                val thumb = id?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }
                VideoMetadata(Platform.YOUTUBE, thumb, id, url)
            }
            host.contains("tiktok.com") -> VideoMetadata(Platform.TIKTOK, null, null, url)
            host.contains("instagram.com") -> VideoMetadata(Platform.INSTAGRAM, null, null, url)
            host.contains("twitter.com") || host.contains("x.com") -> VideoMetadata(Platform.TWITTER, null, null, url)
            host.contains("facebook.com") || host.contains("fb.watch") -> VideoMetadata(Platform.FACEBOOK, null, null, url)
            else -> VideoMetadata(Platform.UNKNOWN, null, null, url)
        }
    }
}
