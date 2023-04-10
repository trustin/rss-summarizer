import com.fasterxml.jackson.databind.JsonNode
import com.linecorp.armeria.client.RestClient
import com.linecorp.armeria.common.HttpHeaderNames
import java.net.URI
import kotlinx.coroutines.future.*
import java.time.Duration

class KagiSummarizer(private val accessToken: String) {
    /**
     * Summarizes the content of the given [URI] into a list of takeaways.
     */
    suspend fun summarize(uri: URI, targetLanguage: String): List<String> {
        val result = restClient
            .get("https://kagi.com/api/v0/summarize")
            .queryParam("url", uri.toASCIIString())
            .queryParam("summary_type", "takeaway")
            .queryParam("target_language", targetLanguage)
            .header(HttpHeaderNames.AUTHORIZATION, "Bot $accessToken")
            .execute(JsonNode::class.java)
            .await()

        val output = result.content().get("data")?.get("output")?.textValue() ?: ""
        return output
            .replace(Regex("(^|\\n)-\\s*"), "$1")
            .split('\n')
    }
}
