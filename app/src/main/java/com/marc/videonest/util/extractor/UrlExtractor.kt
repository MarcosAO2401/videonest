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
        "twitter.com", "x.com", "facebook.com", "fb.watch", "vm.tiktok.com"
    )

    fun extract(rawUrl: String): VideoMetadata? {
        val url = sanitize(rawUrl) ?: return null
        val host = extractHost(url) ?: return null
        if (!isAllowedDomain(host)) return null
        return when {
            host.contains("youtube.com") || host.contains("youtu.be") -> extractYouTube(url)
            host.contains("tiktok.com") -> VideoMetadata(Platform.TIKTOK, null, null, url)
            host.contains("instagram.com") -> VideoMetadata(Platform.INSTAGRAM, null, null, url)
            host.contains("twitter.com") || host.contains("x.com") -> VideoMetadata(Platform.TWITTER, null, null, url)
            host.contains("facebook.com") || host.contains("fb.watch") -> VideoMetadata(Platform.FACEBOOK, null, null, url)
            else -> VideoMetadata(Platform.UNKNOWN, null, null, url)
        }
    }

    private fun sanitize(url: String): String? {
        val t = url.trim()
        if (t.length > 2048 || !t.startsWith("https://")) return null
        return t
    }

    private fun extractHost(url: String): String? = try { URI(url).host?.lowercase() } catch(e: Exception) { null }
    private fun isAllowedDomain(host: String): Boolean = ALLOWED_DOMAINS.any { host.endsWith(it) }

    private fun extractYouTube(url: String): VideoMetadata {
        val id = Regex("youtu\.be/([A-Za-z0-9_-]{11})").find(url)?.groupValues?.get(1)
            ?: Regex("[?&]v=([A-Za-z0-9_-]{11})").find(url)?.groupValues?.get(1)
            ?: Regex("shorts/([A-Za-z0-9_-]{11})").find(url)?.groupValues?.get(1)
        val thumb = id?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }
        return VideoMetadata(Platform.YOUTUBE, thumb, id, url)
    }
}
