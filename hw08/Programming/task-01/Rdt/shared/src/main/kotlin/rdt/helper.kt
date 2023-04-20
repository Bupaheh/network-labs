package rdt

import RdtReceiver
import RdtSender
import java.net.DatagramSocket
import java.net.InetAddress

fun sendAll(
    chunks: List<List<Byte>>,
    socket: DatagramSocket,
    address: InetAddress,
    port: Int,
    timeout: Int,
    lossProb: Double = 0.3
) {
    val sender = RdtSender(socket, address, port, timeout, lossProb)

    for (chunk in chunks + listOf(listOf())) {
        sender.send(chunk.toByteArray())
        println("Sent a chunk")
    }

    println("Sent ${chunks.size} chunks")
}

fun receiveAll(socket: DatagramSocket, lossProb: Double = 0.3): List<Byte> {
    val receiver = RdtReceiver(socket, lossProb)
    val result = mutableListOf<Byte>()
    var cnt = 0

    while (true) {
        val chunk = receiver.receive()

        if (chunk.isEmpty())
            break

        println("Received a chunk")
        cnt++

        result.addAll(chunk.toList())
    }

    println("Received $cnt chunks")

    return result
}