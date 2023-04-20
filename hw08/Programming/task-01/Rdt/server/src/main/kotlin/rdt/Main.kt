package rdt

import java.io.File
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import kotlin.random.Random
import RdtReceiver

fun main(args: Array<String>) {
    val port = args[0].toInt()
    val serverPort = args[1].toInt()
    val timeout = args[2].toInt()
    val serverAddress = InetAddress.getByName("127.0.0.1")
    val socket = DatagramSocket(port, InetAddress.getByName("127.0.0.1"))

    val receivedData = receiveAll(socket)
    File("result1").writeBytes(receivedData.toByteArray())

    println()

    val chunks = File("file2").readBytes().toList().chunked(70)
    sendAll(chunks, socket, serverAddress, serverPort, timeout)
}