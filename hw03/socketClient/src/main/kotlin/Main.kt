import rawhttp.core.RawHttp
import rawhttp.core.RawHttpRequest
import java.net.Socket

fun main(args: Array<String>) {
    val host = args[0]
    val port = args[1].toInt()
    val filePath = args[2]
    val socket = Socket(host, port)
    val rawHttp = RawHttp()

    rawHttp.parseRequest(
        """
            GET /$filePath HTTP/1.1
            Host: $host:$port
            """.trimIndent()
    ).writeTo(socket.getOutputStream())

    val response = rawHttp.parseResponse(socket.getInputStream())

    println(response.eagerly())

    socket.close()
}