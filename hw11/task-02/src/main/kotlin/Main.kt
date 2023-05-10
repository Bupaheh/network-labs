import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val network = Network(
        numNodes = 4,
        edges = listOf(
            Pair(Pair(0, 1), 1u),
            Pair(Pair(0, 2), 3u),
            Pair(Pair(0, 3), 7u),
            Pair(Pair(1, 2), 1u),
            Pair(Pair(2, 3), 2u)
        )
    )

    with(network) { startNodes() }

    delay(1000)

    for (id in 0 until 4) {
        println(network.getNodeState(id).joinToString())
    }

    network.stop()
}