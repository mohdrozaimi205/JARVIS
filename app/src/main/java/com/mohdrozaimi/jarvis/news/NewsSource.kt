package com.mohdrozaimi.jarvis.news

import com.mohdrozaimi.jarvis.data.Http
import okhttp3.OkHttpClient
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

data class Headline(val title: String, val source: String)

data class Feed(val name: String, val url: String)

/** Suapan berita lalai — tukar di sini jika mahu sumber lain. */
object NewsFeeds {
    val DEFAULT = listOf(
        Feed("Malaysia", "https://news.google.com/rss?hl=ms&gl=MY&ceid=MY:ms"),
        Feed("Dunia", "https://feeds.bbci.co.uk/news/world/rss.xml"),
    )
}

class NewsSource(
    private val feeds: List<Feed> = NewsFeeds.DEFAULT,
    private val client: OkHttpClient = Http.client,
) {
    suspend fun top(perFeed: Int = 3): List<Headline> = feeds.flatMap { f ->
        runCatching { parseRss(Http.get(f.url, client), f.name, perFeed) }.getOrDefault(emptyList())
    }
}

fun parseRss(xml: String, source: String, max: Int): List<Headline> = try {
    val factory = DocumentBuilderFactory.newInstance()
    runCatching { factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) }
    val doc = factory.newDocumentBuilder().parse(InputSource(StringReader(xml)))
    val items = doc.getElementsByTagName("item")
    (0 until items.length)
        .mapNotNull { i ->
            (items.item(i) as? Element)
                ?.getElementsByTagName("title")?.item(0)?.textContent
                ?.let(::cleanTitle)
                ?.takeIf { it.isNotBlank() }
        }
        .take(max)
        .map { Headline(it, source) }
} catch (e: Exception) {
    emptyList()
}

fun cleanTitle(s: String): String = s
    .replace("&amp;", "&")
    .replace("&quot;", "\"")
    .replace("&#39;", "'")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace(Regex("\\s+"), " ")
    .trim()
