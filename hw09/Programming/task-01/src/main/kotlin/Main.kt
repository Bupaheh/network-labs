import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface

fun getIp(): String {
    DatagramSocket().use { datagramSocket ->
        datagramSocket.connect(InetAddress.getByName("8.8.8.8"), 12345)
        return datagramSocket.localAddress.hostAddress
    }
}

fun getSubnetMask(ip: String): String? {
    val networkInterface = NetworkInterface.getByInetAddress(InetAddress.getByName(ip))

    var prefixLen: Int? = null

    for (address in networkInterface.interfaceAddresses) {
        if (address.address.hostAddress == ip)
            prefixLen = address.networkPrefixLength.toInt()
    }

    if (prefixLen == null)
        return prefixLen

    val shift = -0x1L shl 32 - prefixLen
    val oct1 = (shift and 0xff000000 shr 24) and 0xff
    val oct2 = (shift and 0x00ff0000 shr 16) and 0xff
    val oct3 = (shift and 0x0000ff00 shr 8) and 0xff
    val oct4 = (shift and 0x000000ff) and 0xff

    return "$oct1.$oct2.$oct3.$oct4"
}

fun main() {
    val address = getIp()

    println(address)
    println(getSubnetMask(address))
}