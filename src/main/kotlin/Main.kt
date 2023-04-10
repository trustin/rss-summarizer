import com.linecorp.armeria.client.WebClient
import com.rometools.rome.feed.synd.SyndContentImpl
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.SyndFeedOutput
import com.rometools.rome.io.XmlReader
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import java.io.File
import java.net.URI


suspend fun main(args: Array<String>) {
    val apiKeyDir = "${System.getProperty("user.home")}/APIKeys"
    val kagiAccessToken = File("$apiKeyDir/kagi_key.txt").readText().trim()
    val deeplAccessToken = File("$apiKeyDir/deepl_key.txt").readText().trim()
    val feedUri = if (args.size > 0) {
        args[0]
    } else {
        "https://blog.cloudflare.com/rss/"
    }

    coroutineScope {
        val cache = Cache()
        val res = WebClient.of().get(feedUri).aggregate().await()
        if (!res.status().isSuccess) {
            throw Exception("Failed to fetch an RSS feed: $feedUri")
        }
        val feed = SyndFeedInput().build(XmlReader(res.content().toInputStream(), true))
        println(feed.title)

        feed.entries.forEach { entry ->
            entry.description.value = cache.get(entry.link)?.let {
                println("> ${entry.title} (cached)")
                it
            } ?: summarizeAndTranslate(kagiAccessToken, deeplAccessToken, cache, entry)
        }

        SyndFeedOutput().output(feed, File("summary_feed.rss"), true)
    }
}

private suspend fun summarizeAndTranslate(
    kagiAccessToken: String,
    deeplAccessToken: String,
    cache: Cache,
    entry: SyndEntry
): String {
    println("> ${entry.title}")

    val uriToSummarize = uriToSummarize(entry)

    val takeaways =
        KagiSummarizer(kagiAccessToken)
        .summarize(URI.create(uriToSummarize), "KO")

    var content = findContent(entry)
    if (content != null) {
        println(">> Translating ..")
        val translatedContentValue = DeepLTranslator(deeplAccessToken).translate(content.value, "KO")
        content.value = """
            <h3>AI summary:</h3>
            <ul>${takeaways.map { "<li>$it</li>" }.joinToString("\n")}</ul>
            <h3>Original content:</h3>
            ${content.value}
            <h3>AI translation:</h3>
            $translatedContentValue""".trimIndent()
    } else {
        println(">> Skipped translation (no content)")
        val newContent = SyndContentImpl()
        newContent.value = """
            <h3>AI summary:</h3>
            <ul>${takeaways.map { "<li>$it</li>" }.joinToString("\n")}</ul>
        """.trimIndent()
        entry.contents.add(newContent)
        content = newContent
    }

    cache.put(entry.link, content.value)
    return content.value
}

private fun uriToSummarize(entry: SyndEntry): String {
    val maybePodcastUri = entry.enclosures.firstOrNull {
        it.type.startsWith("audio/") || it.type.startsWith("video/")
    }?.url

    val uriToSummarize = if (maybePodcastUri != null) {
        println(">> Summarizing podcast ..")
        maybePodcastUri
    } else {
        println(">> Summarizing ..")
        entry.link
    }
    return uriToSummarize
}

private fun findContent(entry: SyndEntry) = entry.contents.firstOrNull {
    it.value?.trim()?.isNotEmpty() == true
}
