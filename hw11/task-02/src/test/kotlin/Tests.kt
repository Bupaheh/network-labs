import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class Tests {
    private val numNodes = 4
    private val edges = listOf(
        Pair(Pair(0, 1), 1u),
        Pair(Pair(0, 2), 3u),
        Pair(Pair(0, 3), 7u),
        Pair(Pair(1, 2), 1u),
        Pair(Pair(2, 3), 2u)
    )
    private val expected = listOf(
        listOf(0u, 1u, 2u, 4u),
        listOf(1u, 0u, 1u, 3u),
        listOf(2u, 1u, 0u, 2u),
        listOf(4u, 3u, 2u, 0u)
    )

    private fun getNetworkState(network: Network) = List(network.numNodes) { network.getNodeState(it).toList() }

    @Test
    fun regularTest() = runBlocking {
        val network = Network(numNodes, edges)

        with(network) { startNodes() }

        delay(1000)

        assertEquals(expected, getNetworkState(network))

        network.stop()
    }

    @Test
    fun decreaseEdgeCostTest() = runBlocking {
        val network = Network(numNodes, edges)

        with(network) { startNodes() }

        delay(1000)

        assertEquals(expected, getNetworkState(network))

        network.updateEdgeCost(2, 3, 1u)

        delay(1000)

        assertEquals(
            listOf(
                listOf(0u, 1u, 2u, 3u),
                listOf(1u, 0u, 1u, 2u),
                listOf(2u, 1u, 0u, 1u),
                listOf(3u, 2u, 1u, 0u)
            ),
            getNetworkState(network)
        )

        network.stop()
    }

    @Test
    fun increaseEdgeCostTest() = runBlocking {
        val network = Network(numNodes, edges)

        with(network) { startNodes() }

        delay(1000)

        assertEquals(expected, getNetworkState(network))

        network.updateEdgeCost(2, 3, 3u)

        delay(1000)

        assertEquals(
            listOf(
                listOf(0u, 1u, 2u, 5u),
                listOf(1u, 0u, 1u, 4u),
                listOf(2u, 1u, 0u, 3u),
                listOf(5u, 4u, 3u, 0u)
            ),
            getNetworkState(network)
        )

        network.stop()
    }
}