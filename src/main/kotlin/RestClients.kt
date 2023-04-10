import com.linecorp.armeria.client.RestClient
import java.time.Duration

internal val restClient: RestClient = RestClient
    .builder()
    .responseTimeout(Duration.ofMinutes(5))
    .build()
