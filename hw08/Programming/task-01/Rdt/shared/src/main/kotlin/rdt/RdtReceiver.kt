import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import kotlin.random.Random

class RdtReceiver(
    private val socket: DatagramSocket,
    private val lossProb: Double
) {
    private var state: Byte = 0

    fun receive(): ByteArray {
        while (true) {
            val buffer = ByteArray(10000)
            val packet = DatagramPacket(buffer, buffer.size)

            socket.soTimeout = 0
            socket.receive(packet)

            if (packet.length == 0)
                continue

            val receivedState = packet.data[0]
            val receivedData = packet.data.copyOfRange(1, packet.length)

            packet.data = byteArrayOf(receivedState)
            packet.length = 1

            if (Random.nextDouble() >= lossProb)
                socket.send(packet)

            if (receivedState == state) {
                state = ((state + 1) % 2).toByte()
                return receivedData
            }
        }
    }
}