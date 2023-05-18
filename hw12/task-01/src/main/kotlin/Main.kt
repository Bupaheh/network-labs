import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

fun generateGraph(numVertices: Int, p: Double = 0.4): List<Pair<Int, Int>> {
    val edges = mutableSetOf<Pair<Int, Int>>()

    for (v in 1 until numVertices) {
        edges.add(Pair(Random.nextInt(0, v), v))
    }

    for (i in 1 until numVertices) {
        for (j in i + 1 until numVertices) {
            if (Random.nextDouble() < p) {
                edges.add(Pair(i, j))
            }
        }
    }

    return edges.toList()
}

fun main() = runBlocking {
    val numVertices = 6
    require(numVertices < 16)
    val edges = generateGraph(numVertices, 0.2)
    val network = Network(
        numNodes = numVertices,
        edges = edges
    )

    launch {
        network.startNodes()
    }

    delay(1000)

    network.stop()
}