import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import kotlin.random.Random

class RdtSender(
    private val socket: DatagramSocket,
    private val address: InetAddress,
    private val port: Int,
    private val timeout: Int,
    private val lossProb: Double
) {
    private var state: Byte = 0

    fun send(data: ByteArray) {
        socket.soTimeout = timeout
        val bytes = byteArrayOf(state) + data
        val packet = DatagramPacket(bytes, bytes.size, address, port)

        while (true) {
            if (Random.nextDouble() >= lossProb)
                socket.send(packet)

            val buffer = ByteArray(10000)
            val receivePacket = DatagramPacket(buffer, buffer.size)

            try {
                socket.receive(receivePacket)

                if (receivePacket.length == 0) {
                    println("Received empty packet")
                    continue
                }

                val receivedState = receivePacket.data[0]

                if (receivedState == state)
                    break
            } catch (e : SocketTimeoutException) {
                continue
            }
        }

        state = ((state + 1) % 2).toByte()
    }
}