import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter
import java.lang.StringBuilder
import java.net.InetAddress
import java.net.Socket
import java.util.Base64
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

fun encodeTo64(message: String): String {
    val bytes = message.toByteArray()
    return Base64.getEncoder().encodeToString(bytes)
}

fun send(output: PrintWriter, message: String) {
    println(message)
    output.println(message)
}

fun send(output: PrintWriter, message: String, input: BufferedReader) {
    send(output, message)
    println(input.readLine())
}

fun main(args: Array<String>) {
    val socket = Socket(InetAddress.getByName("mail.spbu.ru"), 25)
    val output = PrintWriter(socket.getOutputStream(), true)
    val input = socket.getInputStream().bufferedReader()

    val smtpUsername = args[0]
    val smtpPassword = args[1]
    val toEmail = args[2]

    println(input.readLine())

    send(output, "HELO ${InetAddress.getLocalHost().hostName}", input)
    send(output, "AUTH LOGIN", input)
    send(output, encodeTo64(smtpUsername), input)
    send(output, encodeTo64(smtpPassword), input)
    send(output, "MAIL FROM: $smtpUsername", input)
    send(output, "RCPT TO: $toEmail", input)
    send(output, "DATA", input)

    val data = StringBuilder()
    data.appendLine("From: $smtpUsername")
    data.appendLine("To: $toEmail")
    data.appendLine("Subject: Test message")
    data.appendLine("some data")
    data.appendLine("some data 2")

    send(output, data.toString())
    send(output, ".", input)
    send(output, "QUIT", input)

    println(input.readLine())

    socket.close()
}