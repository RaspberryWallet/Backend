package io.raspberrywallet.server

import io.raspberrywallet.mock.ManagerMock
import io.raspberrywallet.server.Server.Companion.PORT
import io.vertx.core.Vertx
import io.vertx.ext.unit.TestOptions
import io.vertx.ext.unit.TestSuite
import io.vertx.ext.unit.report.ReportOptions
import io.vertx.kotlin.core.json.get
import org.junit.jupiter.api.Test


internal class MainVerticleTest {
    private val testOptions = TestOptions()
        .addReporter(ReportOptions().setTo("console"))
        .setTimeout(1000)
    val host = "localhost"

    @Test
    fun test() {
        val vertx = Vertx.vertx()
        val suite = TestSuite.create("API endpoints")
        suite.before { context ->
            val manager = ManagerMock()
            val verticle = MainVerticle(manager)
            vertx.deployVerticle(verticle, context.asyncAssertSuccess())
        }.test("ping") { context ->
            val async = context.async()
            val client = vertx.createHttpClient()

            val req = client.get(PORT, host, "/api/ping")
            req.exceptionHandler {
                context.fail(it.message)
            }
            req.handler { res ->
                context.assertEquals(200, res.statusCode())

                res.bodyHandler {
                    val responseJson = it.toJsonObject()
                    context.assertEquals(responseJson["ping"], "pong")
                }
                async.complete()
            }
            req.end()
        }.test("modules") { context ->
            val async = context.async()
            val client = vertx.createHttpClient()

            val req = client.get(PORT, host, "/api/modules")
            req.exceptionHandler {
                context.fail(it.message)
            }
            req.handler { res ->
                context.assertEquals(200, res.statusCode())

                res.bodyHandler {
                    val responseJson = it.toJsonArray()
                    responseJson.forEach(::println)
                }
                async.complete()
            }
            req.end()
        }.test("currentAddress") { context ->
            val async = context.async()
            val client = vertx.createHttpClient()

            val req = client.get(PORT, host, "/api/currentAddress")
            req.exceptionHandler {
                context.fail(it.message)
            }
            req.handler { res ->
                context.assertEquals(200, res.statusCode())
                res.bodyHandler {
                    val address: String = it.toJsonObject()["currentAddress"]
                    context.put<String>("lastAddress", address)
                    println(it.toJsonObject())
                }
                async.complete()
            }
            req.end()
        }.test("freshAddress", 2) { context ->
            val async = context.async()
            val client = vertx.createHttpClient()

            val req = client.get(PORT, host, "/api/freshAddress")
            req.exceptionHandler {
                context.fail(it.message)
            }
            req.handler { res ->
                context.assertEquals(200, res.statusCode())
                res.bodyHandler {
                    val address: String = it.toJsonObject()["freshAddress"]
                    val lastAddress = context.get<String>("lastAddress")
                    context.assertNotEquals(address, lastAddress)
                    context.put<String>("lastAddress", address)

                    println(it.toJsonObject())
                }
                async.complete()
            }
            req.end()
        }.test("freshAddress", 3) { context ->
            val async = context.async()
            val client = vertx.createHttpClient()

            val req = client.get(PORT, host, "/api/freshAddress")
            req.exceptionHandler {
                context.fail(it.message)
            }
            req.handler { res ->
                context.assertEquals(200, res.statusCode())
                res.bodyHandler {
                    val address: String = it.toJsonObject()["freshAddress"]
                    val lastAddress = context.get<String>("lastAddress")
                    context.assertNotEquals(address, lastAddress)
                    context.put<String>("lastAddress", address)

                    println(it.toJsonObject())
                }
                async.complete()
            }
            req.end()
        }
        suite.run(testOptions).awaitSuccess()
    }
}
