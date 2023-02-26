package com.example.plugins

import com.example.models.Product
import com.example.models.ProductBody
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

const val iconsDir = "icons"

val productStorage = HashMap<Int, Product>()
var lastId = 0

fun Application.configureRouting() {
    File(iconsDir).deleteRecursively()
    File(iconsDir).mkdir()

    routing {
        get("/") {
            if (productStorage.isNotEmpty()) {
                call.respond(productStorage.values)
            } else {
                call.respondText("No products found", status = HttpStatusCode.OK)
            }
        }

        get("{id?}") {
            val id = call.parameters["id"]?.toInt() ?: return@get call.respondText(
                "Missing id",
                status = HttpStatusCode.BadRequest
            )
            val product =
                productStorage[id] ?: return@get call.respondText(
                    "No product with id $id",
                    status = HttpStatusCode.NotFound
                )
            call.respond(product)
        }

        post {
            val productBody = call.receive<ProductBody>()
            val id = lastId++
            val product = Product(id, productBody.name, productBody.description)

            productStorage[id] = product
            call.respondText("Product stored correctly", status = HttpStatusCode.Created)
        }

        put("{id?}") {
            val id = call.parameters["id"]?.toInt() ?: return@put call.respond(HttpStatusCode.BadRequest)
            val productBody = call.receive<ProductBody>()
            val product = Product(id, productBody.name, productBody.description)

            if (!productStorage.containsKey(id)) {
                return@put call.respondText(
                    "No product with id $id",
                    status = HttpStatusCode.NotFound
                )
            }

            productStorage[id] = product
            call.respondText("Product updated correctly", status = HttpStatusCode.Accepted)
        }

        delete("{id?}") {
            val id = call.parameters["id"]?.toInt() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (productStorage.remove(id) != null) {
                call.respondText("Product removed correctly", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not Found", status = HttpStatusCode.NotFound)
            }
        }

        post("/icon/{id?}") {
            val id = call.parameters["id"]?.toInt() ?: return@post call.respond(HttpStatusCode.BadRequest)
            val imageStream = call.receiveStream()

            if (!productStorage.containsKey(id)) {
                return@post call.respondText(
                    "No product with id $id",
                    status = HttpStatusCode.NotFound
                )
            }

            File("$iconsDir/$id.png").writeBytes(withContext(Dispatchers.IO) {
                imageStream.readAllBytes()
            })

            call.respondText("Product icon stored correctly", status = HttpStatusCode.Created)
        }

        get("/icon/{id?}") {
            val id = call.parameters["id"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val imageFile = File("$iconsDir/$id.png")

            if (!productStorage.containsKey(id)) {
                return@get call.respondText(
                    "No product with id $id",
                    status = HttpStatusCode.NotFound
                )
            }

            if (!imageFile.exists()) {
                return@get call.respondText(
                    "No icon with id $id",
                    status = HttpStatusCode.NotFound
                )
            }

            call.respondFile(imageFile)
        }

    }
}
