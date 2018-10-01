package io.raspberrywallet.server

import com.stasbar.Logger
import io.raspberrywallet.Manager
import io.raspberrywallet.server.Server.Companion.PORT
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch


class Server(private val manager: Manager) {
    companion object {
        const val PORT = 8080
    }

    fun start() {
        val vertx = Vertx.vertx()
        vertx.deployVerticle(MainVerticle(manager)) {
            if (it.succeeded()) {
                Logger.info("Server started at port $PORT")
                Logger.info("Webapp located at /index.html")
            } else {
                Logger.err("Could not start server")
                it.cause().printStackTrace()
            }
        }
    }
}

internal class MainVerticle(private val manager: Manager) : CoroutineVerticle() {
    override suspend fun start() {
        val router = Router.router(vertx)
        acceptCORS(router)
        router.get("/api/ping").coroutineHandler {
            respondJson {
                obj("ping" to manager.ping())
            }
        }
        router.get("/api/modules").coroutineHandler {
            respondJsonArray {
                array(manager.modules)
            }
        }
        router.get("/api/moduleState/:id").coroutineHandler {
            val moduleState = manager.getModuleState(pathParam("id"))
            respondJson {
                obj(
                    "state" to moduleState.name,
                    "message" to moduleState.message)
            }
        }
        router.get("/api/currentAddress").coroutineHandler {
            respondJson {
                obj("currentAddress" to manager.currentReceiveAddress)
            }
        }
        router.get("/api/freshAddress").coroutineHandler {
            respondJson {
                obj("freshAddress" to manager.freshReceiveAddress)
            }
        }
        router.get("/api/estimatedBalance").coroutineHandler {
            respondJson {
                obj("estimatedBalance" to manager.estimatedBalance)
            }
        }
        router.get("/api/availableBalance").coroutineHandler {
            respondJson {
                obj("availableBalance" to manager.availableBalance)
            }
        }
        router.get("/api/cpuTemp").coroutineHandler {
            respondJson {
                obj("cpuTemp" to manager.cpuTemperature)
            }
        }
        router.route("/*").handler(StaticHandler.create("assets"))

        awaitResult<HttpServer> {
            vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(config.getInteger("http.port", PORT), it)
        }
    }
}

fun acceptCORS(router: Router) {
    val allowedHeaders = hashSetOf(
        "x-requested-with",
        "Access-Control-Allow-Origin",
        "origin",
        "Content-Type",
        "accept",
        "X-PINGARUNER")

    val corsHandler = CorsHandler.create("*")
        .allowedHeaders(allowedHeaders)
        .allowedMethod(HttpMethod.GET)

    router.route().handler(corsHandler)
}

/**
 * An extension method for simplifying coroutines usage with Vert.x Web routers
 */
fun Route.coroutineHandler(fn: suspend RoutingContext.() -> Unit) {
    handler { ctx ->
        launch(ctx.vertx().dispatcher()) {
            try {
                Logger.d(ctx.normalisedPath())
                fn(ctx)
            } catch (e: Exception) {
                ctx.fail(e)
            }
        }
    }
}

fun RoutingContext.respondJson(block: Json.() -> JsonObject) {
    response().end(json {
        block().encodePrettily()
    })
}

fun RoutingContext.respondJsonArray(block: Json.() -> JsonArray) {
    response().end(json {
        block().encodePrettily()
    })
}



