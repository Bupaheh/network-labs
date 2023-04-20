package rdt

import RdtSender
import java.io.File
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*
import kotlin.random.Random

fun main(args: Array<String>) {
    val port = args[0].toInt()
    val serverPort = args[1].toInt()
    val timeout = args[2].toInt()
    val serverAddress = InetAddress.getByName("127.0.0.1")
    val socket = DatagramSocket(port, InetAddress.getByName("127.0.0.1"))
    val chunks = File("file1").readBytes().toList().chunked(250)

    sendAll(chunks, socket, serverAddress, serverPort, timeout)

    println()

    val receivedData = receiveAll(socket)
    File("result2").writeBytes(receivedData.toByteArray())
}