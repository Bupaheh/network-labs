import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.random.Random

fun main(args: Array<String>) {
    val port = args[0].toInt()
    val socket = DatagramSocket(port, InetAddress.getByName("127.0.0.1"))
    val buffer = ByteArray(10000)

    while (true) {
        val packet = DatagramPacket(buffer, buffer.size)
        socket.receive(packet)

        if (Random.nextDouble() <= 0.2)
            continue

        val msg = String(packet.data.copyOf(packet.length))

        println("Received: $msg")

        val response = msg.uppercase().encodeToByteArray()
        val responsePacket = DatagramPacket(response, response.size, packet.address, packet.port)
        socket.send(responsePacket)
    }
}