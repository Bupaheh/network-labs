import rawhttp.core.RawHttp
import rawhttp.core.RawHttpRequest
import rawhttp.core.body.BytesBody
import rawhttp.core.body.StringBody
import java.io.BufferedWriter
import java.io.File
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException


private const val CRLF = "\r\n"

private fun handleBlacklist(socket: Socket): String {
    val body = """
            <HTML> 
            <HEAD><TITLE>Forbidden</TITLE></HEAD>
            <BODY>Blacklisted</BODY>
            </HTML>
        """.trimIndent()

    RawHttp().parseResponse(
        """
         HTTP/1.1 403 Forbidden
         Content-Type: text/html
         """.trimIndent()
    ).withBody(StringBody(body)).writeTo(socket.getOutputStream())

    return "403"
}

private fun handleNotFound(socket: Socket): String {
    val body = """
            <HTML> 
            <HEAD><TITLE>Not Found</TITLE></HEAD>
            <BODY>404 Not Found</BODY>
            </HTML>
        """.trimIndent()

    RawHttp().parseResponse(
        """
             HTTP/1.1 404 Not Found
             Content-Type: text/html
             """.trimIndent()
    ).withBody(StringBody(body)).writeTo(socket.getOutputStream())

    return "404"
}

private fun handleBadRequest(socket: Socket): String {
    val body = """
            <HTML> 
            <HEAD><TITLE>Bad Request</TITLE></HEAD>
            <BODY>400 Bad Request</BODY>
            </HTML>
        """.trimIndent()

    RawHttp().parseResponse(
        """
             HTTP/1.1 400 Bad Request
             Content-Type: text/html
             """.trimIndent()
    ).withBody(StringBody(body)).writeTo(socket.getOutputStream())

    return "400"
}

private fun handlePostRequest(
    clientSocket: Socket,
    serverSocket: Socket,
    request: RawHttpRequest,
    path: String,
    host: String
): String {
    val rawHttp = RawHttp()
    val body = request.body.get().decodeBody()
    val contentType = request
        .toString()
        .split(CRLF).find {
            it.startsWith("Content-Type:")
        }
        ?.removePrefix("Content-Type:")
        ?: return handleBadRequest(clientSocket)

    rawHttp.parseRequest("""
            POST $path HTTP/1.1
            Host: $host
            Content-Type:$contentType
        """.trimIndent())
        .withBody(BytesBody(body))
        .writeTo(serverSocket.getOutputStream())

    val serverResponse = rawHttp.parseResponse(serverSocket.getInputStream())

    if (serverResponse.statusCode == 404)
        return handleNotFound(clientSocket)

    serverResponse.writeTo(clientSocket.getOutputStream())
    return serverResponse.statusCode.toString()
}

private fun getCacheFilePath(host: String, path: String) = "$cacheDirPath/$host${path}s"

private fun updateCache(serverSocket: Socket, path: String, host: String): Boolean {
    val filePath = getCacheFilePath(host, path)
    File(filePath.replaceAfterLast('/', "")).mkdirs()
    val file = File(filePath)
    val rawHttp = RawHttp()

    val request = if (!file.exists()) {
        rawHttp.parseRequest("""
            GET $path HTTP/1.1
            Host: $host
        """.trimIndent())
    } else {
        val reader = file.bufferedReader()
        val date = reader.readLine()
        reader.close()

        rawHttp.parseRequest("""
            GET $path HTTP/1.1
            Host: $host
            If-Modified-Since:$date
        """.trimIndent())
    }

    request.writeTo(serverSocket.getOutputStream())

    val serverResponse = rawHttp.parseResponse(serverSocket.getInputStream())

    if (serverResponse.statusCode == 304)
        return true

    val date = serverResponse
        .toString()
        .split(CRLF)
        .find {
            it.startsWith("Date:")
        }
        ?.removePrefix("Date:")
        ?: error("No date header")

    val writer = file.bufferedWriter()
    writer.appendLine(date)
    writer.appendLine(serverResponse.eagerly().toString())

    writer.close()

    return false
}

private fun handleGetRequest(
    clientSocket: Socket,
    serverSocket: Socket,
    path: String,
    host: String
): String {
    val rawHttp = RawHttp()

    val isCacheHit = updateCache(serverSocket, path, host)
    val reader = File(getCacheFilePath(host, path)).bufferedReader()

    // skip date
    reader.readLine()

    val response = rawHttp.parseResponse(reader.readText())

    if (response.statusCode == 404)
        return handleNotFound(clientSocket)

    response.writeTo(clientSocket.getOutputStream())

    return if (isCacheHit)
        "304"
    else
        response.statusCode.toString()
}

fun proxy(socket: Socket, blacklist: Set<String>, logger: BufferedWriter) {
    val request = RawHttp().parseRequest(socket.inputStream)
    val path = request.startLine.toString().substringAfter(' ').substringBefore(' ').drop(1)

    logger.appendLine(request.startLine.toString().substringBeforeLast(' '))

    val prefixIndex = path.indexOfFirst { it == '/' }.let {
        if (it == -1)
            path.length
        else
            it
    }
    val host = path.take(prefixIndex)

    if (host in blacklist) {
        logger.appendLine(handleBlacklist(socket))
        return
    }

    val newPath = "/" + path.drop(prefixIndex).removePrefix("/")
    val serverIP = try {
        InetAddress.getByName(host)
    } catch (e: UnknownHostException) {
        logger.appendLine(handleNotFound(socket))
        return
    }
    val serverPort = 80

    Socket(serverIP.hostAddress, serverPort).use { serverSocket ->
        val code = when (request.method) {
            "POST" -> handlePostRequest(socket, serverSocket, request, newPath, host)
            "GET" -> handleGetRequest(socket, serverSocket, newPath, host)
            else -> handleBadRequest(socket)
        }

        logger.appendLine(code)
    }
}