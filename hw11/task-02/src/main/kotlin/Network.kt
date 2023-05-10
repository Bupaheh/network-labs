import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.min

class Network(val numNodes: Int, edges: List<Pair<Pair<Int, Int>, UInt>>) {
    private val edges = ConcurrentHashMap<Pair<Int, Int>, UInt>()
    private val infinity = UInt.MAX_VALUE / 2u
    private val queues = Array<Channel<Pair<Int, Array<UInt>>>>(numNodes) { Channel(UNLIMITED) }
    private val stopFlag = AtomicBoolean(false)
    private val states = Collections.synchronizedList(ArrayList(List(numNodes) { Array(numNodes) { 0u } }))

    private fun Pair<Int, Int>.sort(): Pair<Int, Int> =
        if (second < first) Pair(second, first) else Pair(first, second)

    init {
        for ((uv, cost) in edges) {
            require(cost < infinity)
            require(uv.first in 0 until numNodes)
            require(uv.second in 0 until numNodes)

            this.edges[uv.sort()] = cost
        }
    }

    fun getEdgeCost(u: Int, v: Int): UInt {
        if (u == v) return 0u

        return edges.getOrDefault(Pair(u, v).sort(), infinity)
    }

    fun updateEdgeCost(u: Int, v: Int, cost: UInt) {
        require(cost < infinity)

        edges.replace(Pair(u, v).sort(), cost)
    }

    fun getNodeState(id: Int): Array<UInt> = states[id]

    fun CoroutineScope.startNodes() {
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
        private var nodeState = Array(numNodes) { infinity }
        private val neighbors = (0 until numNodes).filter { getEdgeCost(id, it) < infinity && it != id }
        private val neighborStates = HashMap<Int, Array<UInt>>()

        suspend fun run() {
            while (!stopFlag.get()) {
                val updates = queues[id].tryReceive().getOrNull()

                if (updates != null) {
                    val (neighbor, update) = updates

                    require(neighbor in neighbors)

                    neighborStates[neighbor] = update
                }

                val newState = nodeStateUpdate()

                if (!newState.contentEquals(nodeState)) {
                    nodeState = newState

                    states[id] = nodeState
                    neighbors.forEach { queues[it].send(Pair(id, nodeState)) }
                }

                delay(10)
            }
        }

        private fun nodeStateUpdate() =
            Array(numNodes) { node ->
                var dist = getEdgeCost(id, node)

                for ((neighbor, neighborState) in neighborStates) {
                    dist = min(dist, getEdgeCost(id, neighbor) + neighborState[node])
                }

                dist
            }
    }
}