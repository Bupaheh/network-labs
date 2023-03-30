import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.*

fun main(args: Array<String>) {
    val port = args[0].toInt()
    val socket = DatagramSocket(port, InetAddress.getByName("0.0.0.0"))
    val buffer = ByteArray(Long.SIZE_BYTES)
    val packet = DatagramPacket(buffer, buffer.size)

    while (true) {
        socket.receive(packet)

        val currentTime = Date(ByteBuffer.wrap(packet.data).long)
        println(currentTime)
    }
}