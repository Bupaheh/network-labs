import java.io.File
import java.lang.Exception
import java.net.ServerSocket
import java.net.SocketException

const val logFilePath = "log.log"
const val blacklistFile = "blacklist.txt"
const val cacheDirPath = "cache"

fun parseBlacklist(path: String): Set<String> {
    return File(path).readLines().map { line ->
        line.filterNot { it.isWhitespace() }
    }.toSet()
}

fun main() {
    val server = ServerSocket(0)
    println("localhost:${server.localPort}")

    Thread {
        val logger = File(logFilePath).bufferedWriter()
        val blacklist = parseBlacklist(blacklistFile)
        File(cacheDirPath).mkdir()

        while (true) {
            try {
                server.accept().use { socket ->
                    proxy(socket, blacklist, logger)
                }
            } catch (e: SocketException) {
                break
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        }

        logger.close()
    }.start()

    println("To exit input \"q\"")
    while (true) {
        val input = readlnOrNull() ?: continue

        if (input == "q") {
            server.close()
            break
        }
    }
}