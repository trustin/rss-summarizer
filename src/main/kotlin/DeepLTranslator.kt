import com.fasterxml.jackson.databind.JsonNode
import com.linecorp.armeria.client.RestClient
import com.linecorp.armeria.common.HttpHeaderNames
import com.linecorp.armeria.common.MediaType
import com.linecorp.armeria.common.QueryParams
import kotlinx.coroutines.future.await

class DeepLTranslator(private val accessToken: String) {
    suspend fun translate(text: String, targetLanguage: String): String {
        val result = restClient
            .post("https://api-free.deepl.com/v2/translate")
            .header(HttpHeaderNames.AUTHORIZATION, "DeepL-Auth-Key $accessToken")
            .content(
                MediaType.FORM_DATA,
                QueryParams.builder()
                    .add("text", text)
                    .add("target_lang", targetLanguage).toQueryString()
            )
            .execute(JsonNode::class.java)
            .await()

        return result.content().get("translations")?.get(0)?.get("text")?.textValue() ?: ""
    }
}
