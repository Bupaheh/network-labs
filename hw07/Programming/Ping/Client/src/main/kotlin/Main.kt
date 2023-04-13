import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.*
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    val pingNum = 10
    val port = args[0].toInt()
    val serverPort = args[1].toInt()
    val socket = DatagramSocket(port, InetAddress.getByName("127.0.0.1"))

    socket.soTimeout = 1000

    val rtts = mutableListOf<Long>()

    repeat(pingNum) {
        val msg = "Ping ${it + 1} ${Date()}".encodeToByteArray()
        val packet = DatagramPacket(msg, msg.size, InetAddress.getByName("127.0.0.1"), serverPort)

        val response: String
        var isReceived = false

        val rtt = measureTimeMillis {
            socket.send(packet)

            response = try {
                socket.receive(packet)
                isReceived = true
                String(packet.data)
            } catch (e : SocketTimeoutException) {
                "Request timed out"
            }
        }

        println("$rtt ms\t\t$response")

        if (isReceived)
            rtts.add(rtt)
    }

    val packetLoss = "%.2f".format((pingNum - rtts.size) * 100.0 / pingNum)

    println()
    println("$packetLoss% packet loss")
    println("min: ${rtts.min()}")
    println("max: ${rtts.max()}")
    println("avg: ${rtts.sum().toDouble() / rtts.size}")
}