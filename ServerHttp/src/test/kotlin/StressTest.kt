
import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.ext.web.client.WebClientOptions
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch

class StressTest {

    @Test
    fun dos() {
        val vertx = Vertx.vertx()
        val options = WebClientOptions()
        val client = WebClient.create(vertx, options)
        val countDownLatch = CountDownLatch(1)
        client.get(8080, "localhost", "/")
            .followRedirects(true)
            .send { asyncResult ->
                if (asyncResult.succeeded()) {
                    println(asyncResult.result().bodyAsString())
                }else{
                    println(asyncResult.cause().message)
                }
                countDownLatch.countDown()
            }
        countDownLatch.await()
    }
}
