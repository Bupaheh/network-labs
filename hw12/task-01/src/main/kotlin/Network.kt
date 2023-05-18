import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

data class TableEntry(val source: String, val destination: String, val nextHop: String, val distance: Int)

class Network(val numNodes: Int, val edges: List<Pair<Int, Int>>) {
    private val infinity = 16
    private val queues = Array<Channel<Pair<Int, Array<TableEntry>>>>(numNodes) { Channel(UNLIMITED) }
    private val stopFlag = AtomicBoolean(false)
    private val nodeIps = arrayListOf(
        "176.11.1.61",
        "176.209.38.92",
        "176.156.116.128",
        "176.31.179.132",
        "176.9.179.31",
        "176.241.194.59",
        "176.13.226.255",
        "176.109.116.207",
        "176.52.145.144",
        "176.210.199.17",
        "176.154.245.210",
        "176.134.96.167",
        "176.159.40.175",
        "176.230.195.103",
        "176.61.128.182"
    )

    suspend fun startNodes() = coroutineScope {
        println("Network of $numNodes vertices:")
        println(edges.map { (f, s) -> Pair(nodeIps[f], nodeIps[s]) }.joinToString(postfix = "\n", separator = "\n"))

        for (id in 0 until numNodes) {
            launch {
                Node(id).run()
            }
        }
    }

    fun stop() {
        stopFlag.set(true)
    }

    private inner class Node(private val id: Int) {
        private val neighbors = (0 until numNodes)
            .filter { edges.contains(Pair(id, it)) || edges.contains(Pair(it, id)) }
            .toMutableSet()
        private val lastUpdate = HashMap<Int, Long>()
        private val removedThreshold = 50000
        private var table = Array(numNodes) {
            when (it) {
                id -> TableEntry(nodeIps[id], nodeIps[id], nodeIps[id], 0)
                in neighbors -> TableEntry(nodeIps[id], nodeIps[it], nodeIps[it], 1)
                else -> TableEntry(nodeIps[id], nodeIps[it], "-1", infinity)
            }
        }

        suspend fun run() {
            neighbors.forEach { lastUpdate[it] = System.currentTimeMillis() }
            printTable("Initial")

            while (!stopFlag.get()) {
                var newTable = table.clone()

                while (true) {
                    val updates = queues[id].tryReceive().getOrNull() ?: break
                    val (neighbor, update) = updates

                    neighbors.add(neighbor)
                    lastUpdate[neighbor] = System.currentTimeMillis()

                    newTable = tableUpdate(newTable, neighbor, update)
                }

                neighbors
                    .filter { System.currentTimeMillis() - lastUpdate[it]!! > removedThreshold }
                    .forEach { newTable[it] = TableEntry(nodeIps[id], nodeIps[it], "-1", infinity) }

                if (!newTable.contentEquals(table)) {
                    table = newTable
                    printTable("Updated")
                }

                neighbors.forEach { queues[it].send(Pair(id, table)) }

                delay(100)
            }

            printTable("Final")
        }

        private fun tableUpdate(table: Array<TableEntry>, otherId: Int, otherTable: Array<TableEntry>) =
            Array(numNodes) { node ->
                when {
                    node == id -> TableEntry(nodeIps[id], nodeIps[id], nodeIps[id], 0)
                    node == otherId -> TableEntry(nodeIps[id], nodeIps[node], nodeIps[node], 1)
                    otherTable[node].nextHop == nodeIps[id] -> table[node]
                    table[node].nextHop == nodeIps[otherId] ->
                        TableEntry(
                            table[node].source,
                            table[node].destination,
                            table[node].nextHop,
                            otherTable[node].distance + 1
                        )
                    table[node].distance > otherTable[node].distance + 1 ->
                        TableEntry(
                            table[node].source,
                            table[node].destination,
                            nodeIps[otherId],
                            otherTable[node].distance + 1
                        )
                    else -> table[node]
                }
            }

        private fun String.normalizeString() = padEnd(18, ' ')

        private fun printTable(prefix: String) {
            val tablePrint = StringBuilder()
                .appendLine("$prefix table of router ${nodeIps[id]}:")
                .appendLine(
                    "[Source]".normalizeString() +
                            " ${"[Destination]".normalizeString()}" +
                            " ${"[Next Hop]".normalizeString()}" +
                            " [Distance]"
                )

            table.filterIndexed { node, _ -> node != id }
                .forEach { entry ->
                    tablePrint.appendLine(
                        entry.source.normalizeString() +
                            " ${entry.destination.normalizeString()}" +
                            " ${entry.nextHop.normalizeString()}" +
                            " ${if (entry.distance >= infinity) Int.MAX_VALUE else entry.distance }"
                    )
                }

            println(tablePrint)
        }
    }
}