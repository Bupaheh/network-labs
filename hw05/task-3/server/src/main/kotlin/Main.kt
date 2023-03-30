import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.ByteBuffer
import java.util.*

fun listAllBroadcastAddresses(): List<InetAddress> {
    val broadcastList = mutableListOf<InetAddress>()

    for (networkInterface in NetworkInterface.getNetworkInterfaces()) {
        if (networkInterface.isLoopback || !networkInterface.isUp)
            continue

        networkInterface.interfaceAddresses
            .mapNotNull { a -> a.broadcast }
            .forEach { broadcastList.add(it) }
    }

    return broadcastList
}

fun main(args: Array<String>) {
    val port = args[0].toInt()
    val broadcastAddress = listAllBroadcastAddresses().firstOrNull() ?: InetAddress.getByName("255.255.255.255")
    val socket = DatagramSocket().apply { broadcast = true }
    val buffer = ByteBuffer.allocate(Long.SIZE_BYTES)
    val packet = DatagramPacket(buffer.array(), Long.SIZE_BYTES, broadcastAddress, port)

    while (true) {
        val date = Date()

        buffer.clear()
        buffer.putLong(date.time)
        packet.data = buffer.array()

        socket.send(packet)
        println("Broadcast: $date")

        Thread.sleep(1000)
    }
}