import com.google.common.hash.Hashing
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Instant

class Cache(private val directory: Path =
    Path.of(System.getProperty("user.home"), ".cache", "rss-summarizer")) {

    init {
        Files.createDirectories(directory)
    }

    fun get(uri: String): String? {
        val path = cachePath(uri)
        if (!Files.exists(path)) {
            return null
        }

        val value = Files.readAllBytes(path)
        // Touch the file so that it's not deleted by a cache cleaner.
        Files.setLastModifiedTime(path, FileTime.from(Instant.now()))

        return value.toString(StandardCharsets.UTF_8)
    }

    fun put(uri: String, value: String) {
        val path = cachePath(uri)
        Files.createDirectories(path.parent)
        Files.write(path, value.toByteArray())
    }

    private fun cachePath(uri: String): Path {
        val hash = Hashing.sha256().hashString(uri, StandardCharsets.UTF_8).toString()
        val first = hash.substring(0, 2)
        val subDir = directory.resolve(first)
        return subDir.resolve(hash)
    }
}
