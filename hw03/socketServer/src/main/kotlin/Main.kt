import rawhttp.core.RawHttp
import rawhttp.core.body.BytesBody
import java.io.File
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

fun handleRequest(socket: Socket) {
    val inputStream = socket.getInputStream()
    val outputStream = socket.getOutputStream()
    val rawHttp = RawHttp()
    val request = rawHttp.parseRequest(inputStream)
    val file = File(request.uri.path.drop(1))

    if (file.exists()) {
        val body = file.readBytes()

        rawHttp.parseResponse(
            """
                 HTTP/1.1 200 OK
                 Content-Type: plain/text
                 """.trimIndent()
        ).withBody(BytesBody(body)).writeTo(outputStream)
    } else {
        rawHttp.parseResponse(
            """
                 HTTP/1.1 404 Not Found
                 Content-Type: plain/text
                 """.trimIndent()
        ).writeTo(outputStream)
    }

    socket.close()
}

fun main(args: Array<String>) {
    val server = ServerSocket(0)
    println("Port: ${server.localPort}")

    val threadPool = Executors.newFixedThreadPool(args[0].toInt())

    while (true) {
        val socket = server.accept()
        threadPool.submit { handleRequest(socket) }
    }
}